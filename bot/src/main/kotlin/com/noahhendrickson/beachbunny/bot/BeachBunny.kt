package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.noahhendrickson.beachbunny.bot.database.prefix
import com.noahhendrickson.beachbunny.bot.extensions.AdminExtension
import com.noahhendrickson.beachbunny.bot.extensions.GreeterExtension
import com.noahhendrickson.beachbunny.bot.extensions.IntroductionExtension
import com.noahhendrickson.beachbunny.bot.extensions.SyncExtension
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

    val database = Database.init(dropTables = true)

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
            add { AdminExtension(it, database) }
            add(::GreeterExtension)
            add(::IntroductionExtension)
            add(::SyncExtension)
        }
    }

    bot.start()
}
