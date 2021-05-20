package com.noahhendrickson.beachbunny.bot.database

import com.noahhendrickson.beachbunny.database.tables.GuildTable
import dev.kord.core.entity.Guild
import org.jetbrains.exposed.sql.select

val Guild.prefix: String?
    get() {
        return GuildTable
            .slice(GuildTable.prefix)
            .select { GuildTable.guildSnowflake eq this@prefix.id.value }
            .firstOrNull()?.get(GuildTable.prefix)
    }