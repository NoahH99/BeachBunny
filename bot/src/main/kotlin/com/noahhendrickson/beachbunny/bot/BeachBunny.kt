package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.noahhendrickson.beachbunny.bot.extensions.GreeterExtension
import com.noahhendrickson.beachbunny.bot.extensions.IntroductionExtension
import com.noahhendrickson.beachbunny.bot.extensions.SyncExtension
import com.noahhendrickson.beachbunny.database.Database
import dev.kord.common.entity.PresenceStatus
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

@PrivilegedIntent
@ExperimentalStdlibApi
suspend fun main() {
    val token = System.getenv()["BOT_TOKEN"] ?: throw IllegalArgumentException("No token provided!")

    Database.init(dropTables = true)

    val bot = ExtensibleBot(token) {
        intents {
            +Intent.Guilds
            +Intent.GuildMembers
            +Intent.GuildVoiceStates
            +Intent.GuildMessages
            +Intent.DirectMessagesReactions
        }

        presence {
            playing("syncing data...")
            status = PresenceStatus.DoNotDisturb
        }

        extensions {
            add(::GreeterExtension)
            add(::IntroductionExtension)
            add(::SyncExtension)
        }
    }

    bot.start()
}
