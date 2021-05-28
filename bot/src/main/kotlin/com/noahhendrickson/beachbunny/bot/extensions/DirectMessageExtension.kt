package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.noahhendrickson.beachbunny.bot.isNotBot
import com.noahhendrickson.beachbunny.bot.util.Conversation
import com.noahhendrickson.beachbunny.bot.util.IntroductionParser
import com.noahhendrickson.beachbunny.bot.util.addBranding
import com.noahhendrickson.beachbunny.database.models.Pronoun
import dev.kord.common.Color
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.createRole
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class DirectMessageExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Direct Message"

    private val conversations: MutableMap<Snowflake, Conversation> = HashMap()

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check(::noGuild)
            check(::isNotBot)

            action {
                val guild = event.kord.getGuild(Snowflake(642128459929485312)) ?: return@action
                val user = event.message.author ?: return@action
                val member = guild.getMember(user.id)
                val channel = user.getDmChannelOrNull() ?: return@action

                if (!user.inConversation) {
                    channel.beginConversation(user)
                    return@action
                }

                val conversation = conversations[user.id] ?: return@action

                val pronouns = IntroductionParser("Pronouns: ${event.message.content}").parsePronouns()

                if (pronouns.isEmpty()) {
                    channel.createMessageAndDelete("No valid pronouns supplied.")
                    return@action
                }

                if (conversation.state == Conversation.State.ADD) {
                    conversations.remove(user.id)

                    pronouns.forEach { pronoun ->
                        val role = guild.roles.firstOrNull { it.name == pronoun.value }

                        if (role != null) {
                            member.addRole(role.id)
                            channel.createMessageAndDelete("You now have the **${role.name}** role.")
                        } else {
                            guild.createRole {
                                this.name = pronoun.value
                                color = Color(0xD6CF89)
                                permissions = Permissions()
                            }.apply {
                                member.addRole(id)
                                channel.createMessageAndDelete("You now have the **${name}** role.")
                            }
                        }
                    }

                    return@action
                } else if (conversation.state == Conversation.State.REMOVE) {
                    conversations.remove(user.id)

                    for (pronoun in pronouns) {
                        val role = guild.roles.firstOrNull { it.name == pronoun.value }

                        if (role != null) {
                            member.removeRole(role.id)
                            channel.createMessageAndDelete("You no longer have the **${role.name}** role.")
                        } else {
                            channel.createMessageAndDelete("That is not a valid pronoun. Terminating process.")
                            break
                        }
                    }

                    return@action
                }
            }
        }

        event<ReactionAddEvent> {
            check(::noGuild)
            check(::isNotBot)

            action {
                val user = event.getUserOrNull() ?: return@action
                val channel = user.getDmChannelOrNull() ?: return@action
                val reaction = event.emoji.name

                if (user.inConversation) {
                    when (reaction) {
                        Pronoun.unicodes.elementAt(0) -> {
                            conversations[user.id]?.apply {
                                conversations.replace(user.id, copy(state = Conversation.State.ADD))
                            }

                            channel.createMessage("Please type the pronoun you would like to add.")
                        }

                        Pronoun.unicodes.elementAt(1) -> {
                            conversations[user.id]?.apply {
                                conversations.replace(user.id, copy(state = Conversation.State.REMOVE))
                            }

                            val guild = event.kord.getGuild(Snowflake(642128459929485312))

                            if (guild != null) {
                                val member = guild.getMemberOrNull(user.id)

                                if (member != null) {
                                    val role = guild.getRoleOrNull(Snowflake(775396195677372457))

                                    if (role != null) {
                                        val pronouns: String = member.roles
                                            .filter { it.rawPosition < role.rawPosition }
                                            .toList()
                                            .joinToString("\n") { "> ${it.name}" }

                                        channel.createMessage("**Your current pronouns are:**\n $pronouns")
                                    }
                                }
                            }

                            channel.createMessage("Please type the pronoun you would like to remove.")

                        }

                        Pronoun.unicodes.elementAt(2) -> {
                            conversations.remove(user.id)

                            val guild = event.kord.getGuild(Snowflake(642128459929485312))

                            if (guild != null) {
                                val member = guild.getMemberOrNull(user.id)

                                if (member != null) {
                                    val role = guild.getRoleOrNull(Snowflake(775396195677372457))

                                    if (role != null) {
                                        val pronouns: String = member.roles
                                            .filter { it.rawPosition < role.rawPosition }
                                            .toList()
                                            .joinToString("\n") { "> ${it.name}" }

                                        channel.createMessage("**Your current pronouns are:**\n $pronouns")
                                        channel.createMessage("Respond again for more options")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val UserBehavior.inConversation: Boolean
        get() = conversations.contains(id)

    private suspend fun DmChannel.beginConversation(user: User) {
        val message = createEmbed {
            title = "Select an action"

            field {
                name = "Add Pronouns"
                value = "Reaction: ${Pronoun.unicodes.elementAt(0)}"
                inline = true
            }

            field {
                name = EmbedBuilder.ZERO_WIDTH_SPACE
                inline = true
            }

            field {
                name = "Remove Pronouns"
                value = "Reaction: ${Pronoun.unicodes.elementAt(1)}"
                inline = true
            }

            field {
                name = "List Pronouns"
                value = "Reaction: ${Pronoun.unicodes.elementAt(2)}"
            }

            addBranding()
        }.also {
            it.addReaction(Pronoun.unicodes.elementAt(0))
            it.addReaction(Pronoun.unicodes.elementAt(1))
            it.addReaction(Pronoun.unicodes.elementAt(2))
        }

        conversations[user.id] = Conversation(user.id, message.id, Conversation.State.NONE)
    }
}
