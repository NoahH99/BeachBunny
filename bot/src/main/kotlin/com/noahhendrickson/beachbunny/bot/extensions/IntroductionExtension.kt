package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.noahhendrickson.beachbunny.bot.database.insertOrUpdate
import com.noahhendrickson.beachbunny.bot.database.introductionChannelSnowflake
import com.noahhendrickson.beachbunny.bot.dialogflow.createDialogflowAssistant
import com.noahhendrickson.beachbunny.bot.isNotBot
import com.noahhendrickson.beachbunny.bot.util.IntroductionParser
import com.noahhendrickson.beachbunny.bot.util.addBranding
import com.noahhendrickson.beachbunny.bot.util.getTopRoleWithColor
import com.noahhendrickson.beachbunny.database.models.Introduction
import com.noahhendrickson.beachbunny.database.models.Pronoun
import com.noahhendrickson.beachbunny.database.tables.insertOrUpdate
import com.noahhendrickson.beachbunny.database.tables.selectName
import com.noahhendrickson.beachbunny.database.tables.selectPronoun
import dev.kord.common.Color
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.createRole
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

private val logger = KotlinLogging.logger {}

class IntroductionExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Introduction"

    override suspend fun setup() {
        val assistant = createDialogflowAssistant {
            credentials { IntroductionExtension::class.java.getResourceAsStream("/token.json") }
        }

        event<MessageCreateEvent> {
            check(::isNotBot)

            check {
                val id = it.getGuild()?.introductionChannelSnowflake ?: return@check false
                inChannel(Snowflake(id)).invoke(it)
            }

            action {
                val member = event.member ?: return@action
                val guild = event.getGuild() ?: return@action
                val (parser, name, pronouns) = IntroductionParser(event.message.content)

                if (name == null && pronouns.isEmpty()) return@action

                with(member) member@{
                    Introduction(
                        id.value,
                        guildId.value,
                        name ?: username,
                        nameEmoji = "\u2705",
                        pronouns,
                        parser.parseAboutYou(assistant)
                    ).apply {
                        sendTo(this@member)?.also {
                            insertOrUpdate(it.id.value, member.insertOrUpdate(), guild.insertOrUpdate())
                        }
                    }
                }
            }
        }

        event<ReactionAddEvent> {
            check(::noGuild)
            check(::isNotBot)

            action {
                with(event) {
                    kord.handleIntroductionReaction(
                        userId.value,
                        messageId.value,
                        emoji,
                        addReaction = true
                    )
                }
            }
        }

        event<ReactionRemoveEvent> {
            check(::noGuild)
            check(::isNotBot)

            action {
                with(event) {
                    kord.handleIntroductionReaction(
                        userId.value,
                        messageId.value,
                        emoji,
                        addReaction = false
                    )
                }
            }
        }
    }
}

private suspend fun Introduction.sendTo(member: Member): Message? {
    return member.getDmChannelOrNull()?.createEmbed {
        author {
            name = member.username
            icon = member.avatar.url
        }

        color = member.getTopRoleWithColor()?.color

        description = buildString {
            append("Thanks for introducing yourself to the **${member.getGuildOrNull()?.name}** community!\n\n")
            append("We detected a name and/or pronoun(s) from your messages. ")
            append("Feel free to get yourself some sweet server customizations!\n\n")

            if (name.isNotEmpty()) {
                append("If you'd like to change your nickname in the server ")
                append("to **$name**, react with $nameEmoji\n\n")
            }

            pronouns.forEachIndexed { i, pronoun ->
                append("**Pronoun:** ${pronoun.value}\n")
                append("**Reaction:** ${Pronoun.unicodes.elementAt(i)}\n\n")
            }

            if (aboutMeResult.isNotEmpty()) append(aboutMeResult)
        }

        addBranding()
    }?.apply {
        if (name.isNotEmpty()) addReaction("\u2705")
        repeat(pronouns.size) { addReaction(Pronoun.unicodes.elementAt(it)) }
    }
}

private suspend fun Kord.handleIntroductionReaction(
    userSnowflake: Long,
    messageSnowflake: Long,
    emoji: ReactionEmoji,
    addReaction: Boolean
) {
    if (emoji !is ReactionEmoji.Unicode) return

    val unicode = emoji.name

    newSuspendedTransaction {
        val name = selectName(userSnowflake, messageSnowflake, unicode)
        val pronoun = selectPronoun(userSnowflake, messageSnowflake, unicode)

        name?.apply {
            getGuild(Snowflake(name.first))?.apply {
                getMemberOrNull(Snowflake(name.second))?.apply {
                    edit { nickname = if (addReaction) name.third else null }

                    getDmChannelOrNull()?.apply {
                        if (addReaction) createMessageAndDelete("You now have the nickname **${name.third}**.")
                        else createMessageAndDelete("You no longer have a nickname.")
                    }
                }
            }
        }

        pronoun?.apply {
            getGuild(Snowflake(pronoun.first))?.apply guild@{
                getMemberOrNull(Snowflake(pronoun.second))?.apply member@{
                    val role = this@guild.roles.firstOrNull { it.name == pronoun.third }

                    if (role != null) {
                        if (addReaction) addRole(role.id)
                        else removeRole(role.id)

                        getDmChannelOrNull()?.apply {
                            if (addReaction) createMessageAndDelete("You now have the **${role.name}** role.")
                            else createMessageAndDelete("You no longer have the **${role.name}** role.")
                        }
                    } else {
                        if (addReaction)
                            createRole {
                                this.name = pronoun.third
                                color = Color(0xD6CF89)
                                permissions = Permissions()
                            }.apply role@{
                                this@member.apply {
                                    addRole(this@role.id)
                                    getDmChannelOrNull()
                                        ?.createMessageAndDelete("You now have the **${this@role.name}** role.")

                                }
                            }
                    }
                }
            }
        }
    }
}

suspend fun DmChannel.createMessageAndDelete(content: String) {
    val message = createMessage(content)
    delay(7000)
    message.delete()
}
