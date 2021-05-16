package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.noahhendrickson.beachbunny.bot.extensions.GreeterExtension
import com.noahhendrickson.beachbunny.database.Database
import dev.kord.common.entity.PresenceStatus
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

@PrivilegedIntent
suspend fun main() {
    val token = System.getenv()["BOT_TOKEN"] ?: throw IllegalArgumentException("No token provided!")

    Database.init()

    val bot = ExtensibleBot(token) {
        intents {
            +Intent.Guilds
            +Intent.GuildMembers
            +Intent.GuildVoiceStates
            +Intent.GuildMessages
            +Intent.DirectMessagesReactions
        }

        presence {
            playing("the same song for 6 weeks")
            status = PresenceStatus.Online
        }

        extensions {
            add(::GreeterExtension)
        }
    }

    bot.start()
}
