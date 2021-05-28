package com.noahhendrickson.beachbunny.bot.database

import com.noahhendrickson.beachbunny.database.tables.GuildTable
import com.noahhendrickson.beachbunny.database.tables.insertGuildAndGetId
import dev.kord.core.behavior.GuildBehavior
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun GuildBehavior.insertOrUpdate() = transaction {
    insertGuildAndGetId(
        this@insertOrUpdate.id.value,
        prefix,
        logChannelSnowflake,
        introductionChannelSnowflake,
        greeterChannelSnowflake
    )
}

val GuildBehavior.prefix: String
    get() {
        return transaction {
            GuildTable
                .slice(GuildTable.prefix)
                .select { GuildTable.guildSnowflake eq this@prefix.id.value }
                .firstOrNull()
                ?.get(GuildTable.prefix)
        } ?: GuildTable.defaultPrefix
    }

val GuildBehavior.logChannelSnowflake: Long?
    get() {
        return transaction {
            GuildTable
                .slice(GuildTable.logChannelSnowflake)
                .select { GuildTable.guildSnowflake eq this@logChannelSnowflake.id.value }
                .firstOrNull()
                ?.get(GuildTable.logChannelSnowflake)
        }
    }

val GuildBehavior.introductionChannelSnowflake: Long?
    get() {
        return transaction {
            GuildTable
                .slice(GuildTable.introductionChannelSnowflake)
                .select { GuildTable.guildSnowflake eq this@introductionChannelSnowflake.id.value }
                .firstOrNull()
                ?.get(GuildTable.introductionChannelSnowflake)
        }
    }

val GuildBehavior.greeterChannelSnowflake: Long?
    get() {
        return transaction {
            GuildTable
                .slice(GuildTable.greeterChannelSnowflake)
                .select { GuildTable.guildSnowflake eq this@greeterChannelSnowflake.id.value }
                .firstOrNull()
                ?.get(GuildTable.greeterChannelSnowflake)
        }
    }
