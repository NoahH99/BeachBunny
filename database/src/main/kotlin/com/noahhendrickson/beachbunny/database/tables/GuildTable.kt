package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertIgnore

object GuildTable : IntIdTable() {

    val guildSnowflake = long("guild_snowflake")
    val prefix = varchar("prefix", 15).default("!")
    val logChannelSnowflake = long("log_channel_snowflake").nullable().default(null)

}

fun insertGuild(id: Long) {
    GuildTable.insertIgnore { it[guildSnowflake] = id }
}
