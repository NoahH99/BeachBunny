package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildTable : IntIdTable() {

    val guildSnowflake = long("guild_snowflake")
    val prefix = varchar("prefix", 15).default("!")
    val logChannelSnowflake = long("log_channel_snowflake").nullable().default(null)

}
