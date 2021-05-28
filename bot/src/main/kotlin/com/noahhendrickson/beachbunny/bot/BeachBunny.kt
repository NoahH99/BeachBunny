package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.noahhendrickson.beachbunny.bot.database.prefix
import com.noahhendrickson.beachbunny.bot.extensions.*
import com.noahhendrickson.beachbunny.database.Database
import com.noahhendrickson.beachbunny.database.tables.GuildTable
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
            +Intent.DirectMessages
            +Intent.DirectMessagesReactions
        }

        commands {
            prefix { getGuild()?.prefix ?: GuildTable.defaultPrefix }
        }

        presence {
            playing("syncing data...")
            status = PresenceStatus.DoNotDisturb
        }

        extensions {
            sentry = false
            help = false

            add(::AdminExtension)
            add(::DirectMessageExtension)
            add(::GreeterExtension)
            add(::IntroductionExtension)
            add(::SyncExtension)

//            add(::AdminExtension)
//            add(::GreeterExtension)
//            add(::IntroductionExtension)
//            add(::DirectMessageExtension)
//            add(::SyncExtension)
        }
    }

    bot.start()
}
