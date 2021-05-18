package com.noahhendrickson.beachbunny.bot.extensions

import com.gitlab.kordlib.kordx.emoji.Emojis
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.noahhendrickson.beachbunny.bot.dialogflow.createDialogflowAssistant
import com.noahhendrickson.beachbunny.bot.introduction.IntroductionParser
import com.noahhendrickson.beachbunny.bot.isNotBot
import com.noahhendrickson.beachbunny.database.models.Introduction
import com.noahhendrickson.beachbunny.database.models.Pronoun
import com.noahhendrickson.beachbunny.database.tables.insert
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

class IntroductionExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Introduction"

    override suspend fun setup() {
        val assistant = createDialogflowAssistant {
            credentials { IntroductionExtension::class.java.getResourceAsStream("/token.json") }
        }

        event<MessageCreateEvent> {
            check { isNotBot(it) }
            check(inChannel(Snowflake(842174438564167711))) // This is for development purposes only.

            action {
                val member = event.member ?: return@action

                val parser = IntroductionParser(event.message.content)
                val introduction = Introduction(
                    userSnowflake = member.id.value,
                    guildSnowflake = member.guildId.value,
                    _name = parser.parseName() ?: member.username,
                    nameEmoji = Pronoun.unicodes.first(),
                    pronouns = parser.parsePronouns(),
                    _aboutMeResult = parser.parseAboutYou(assistant)
                )

                transaction { introduction.insert() }
            }
        }
    }
}
