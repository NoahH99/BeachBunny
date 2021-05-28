package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.util.insertAndGetId
import org.jetbrains.exposed.dao.id.IntIdTable

object GuildTable : IntIdTable() {

    const val defaultPrefix = "bb!"

    val guildSnowflake = long("guild_snowflake").uniqueIndex()

    val prefix = varchar("prefix", 15).default(defaultPrefix)
    val logChannelSnowflake = long("log_channel_snowflake").nullable().default(null)
    val introductionChannelSnowflake = long("introduction_channel_snowflake").nullable().default(null)
    val greeterChannelSnowflake = long("greeter_channel_snowflake").nullable().default(null)

}

fun insertGuildAndGetId(
    guildSnowflake: Long,
    prefix: String = GuildTable.defaultPrefix,
    logChannelSnowflake: Long? = null,
    introductionChannelSnowflake: Long? = null,
    greeterChannelSnowflake: Long? = null,
) = GuildTable.insertAndGetId({ GuildTable.guildSnowflake eq guildSnowflake }) {
    it[GuildTable.guildSnowflake] = guildSnowflake
    it[GuildTable.prefix] = prefix
    it[GuildTable.logChannelSnowflake] = logChannelSnowflake
    it[GuildTable.introductionChannelSnowflake] = introductionChannelSnowflake
    it[GuildTable.greeterChannelSnowflake] = greeterChannelSnowflake
}
