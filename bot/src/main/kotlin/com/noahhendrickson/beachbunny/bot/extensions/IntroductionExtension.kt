package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.checks.noGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.noahhendrickson.beachbunny.bot.dialogflow.createDialogflowAssistant
import com.noahhendrickson.beachbunny.bot.isNotBot
import com.noahhendrickson.beachbunny.bot.util.IntroductionParser
import com.noahhendrickson.beachbunny.bot.util.getColor
import com.noahhendrickson.beachbunny.database.models.Introduction
import com.noahhendrickson.beachbunny.database.models.Pronoun
import com.noahhendrickson.beachbunny.database.tables.insert
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
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

class IntroductionExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Introduction"

    override suspend fun setup() {
        val assistant = createDialogflowAssistant {
            credentials { IntroductionExtension::class.java.getResourceAsStream("/token.json") }
        }

        event<MessageCreateEvent> {
            check(::isNotBot)
            check(inChannel(Snowflake(642215940469161984))) // This is for development purposes only.

            action {
                val member = event.member ?: return@action

                val (parser, name, pronouns) = IntroductionParser(event.message.content)
                if (name == null && pronouns.isEmpty()) return@action

                with(member) {
                    val introduction = Introduction(
                        userSnowflake = id.value,
                        guildSnowflake = guildId.value,
                        _name = name ?: username,
                        nameEmoji = Pronoun.unicodes.first(),
                        pronouns = pronouns,
                        _aboutMeResult = parser.parseAboutYou(assistant)
                    )

                    transaction { introduction.insert() }
                    introduction.sendTo(this)
                }
            }
        }

        event<ReactionAddEvent> {
            check(::noGuild)
            check(::isNotBot)

            action { event.kord.handleIntroductionReaction(event.userId.value, event.emoji, addReaction = true) }
        }

        event<ReactionRemoveEvent> {
            check(::noGuild)
            check(::isNotBot)

            action { event.kord.handleIntroductionReaction(event.userId.value, event.emoji, addReaction = false) }
        }
    }
}

private suspend fun Introduction.sendTo(member: Member) {
    member.getDmChannelOrNull()
        ?.createEmbed {
            author {
                name = member.username
                icon = member.avatar.url
            }

            color = member.getColor()

            description = buildString {
                append("Thanks for introducing yourself to the Beach Bunny community!\n\n")
                append("We detected a name and/or pronoun(s) from your messages. ")
                append("Feel free to get yourself some sweet server customizations!\n\n")

                if (name.isNotEmpty()) {
                    append("If you'd like to change your nickname in the server ")
                    append("to **$name**, react with $nameEmoji\n\n")
                }

                pronouns.forEachIndexed { i, pronoun ->
                    append("**Pronoun:** ${pronoun.value}\n")
                    append("**Reaction:** ${Pronoun.unicodes.elementAt(i + 1)}\n\n")
                }

                if (aboutMeResult.isNotEmpty()) append(aboutMeResult)
            }
        }?.apply {
            val reactions = pronouns.size + if (name.isNotEmpty()) 1 else 0
            repeat(reactions) { addReaction(Pronoun.unicodes.elementAt(it)) }
        }
}

private suspend fun Kord.handleIntroductionReaction(
    userSnowflake: Long,
    emoji: ReactionEmoji,
    addReaction: Boolean
) {
    if (emoji !is ReactionEmoji.Unicode) return

    val unicode = emoji.name

    newSuspendedTransaction {
        val name = selectName(userSnowflake, unicode)
        val pronoun = selectPronoun(userSnowflake, unicode)

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
                            else createMessageAndDelete("You no longer have the ${role.name} role.")
                        }
                    } else {
                        if (addReaction)
                            createRole {
                                this.name = pronoun.third
                                color = Color(0xD6CF89)
                                permissions = Permissions()
                            }.apply role@{
                                this@member.apply {
                                    addRole(id)
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

private suspend fun DmChannel.createMessageAndDelete(content: String) {
    val message = createMessage(content)
    delay(7000)
    message.delete()
}
