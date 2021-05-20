package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.noahhendrickson.beachbunny.bot.database.prefix
import com.noahhendrickson.beachbunny.bot.extensions.*
import com.noahhendrickson.beachbunny.database.Database
import com.noahhendrickson.beachbunny.database.tables.GuildTable
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.PresenceStatus
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@KordPreview
@PrivilegedIntent
@ExperimentalStdlibApi
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

        commands {
            prefix { newSuspendedTransaction { getGuild()?.prefix ?: GuildTable.defaultPrefix } }
        }

        presence {
            playing("syncing data...")
            status = PresenceStatus.DoNotDisturb
        }

        extensions {
            sentry = false
            help = false

            add(::AdminExtension)
            add(::GreeterExtension)
            add(::IntroductionExtension)
            add(::StaffExtension)
            add(::SyncExtension)
        }
    }

    bot.start()
}
