package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.insertAndGetId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object GuildTable : IntIdTable() {

    const val defaultPrefix = "bb!"

    val guildSnowflake = long("guild_snowflake").uniqueIndex()

    val prefix = varchar("prefix", 15).default(defaultPrefix)
    val logChannelSnowflake = long("log_channel_snowflake").nullable().default(null)

}

fun insertGuildAndGetId(
    guildSnowflake: Long,
    prefix: String = GuildTable.defaultPrefix,
    logChannelSnowflake: Long? = null,
) = GuildTable.insertAndGetId({ GuildTable.guildSnowflake eq guildSnowflake }) {
    it[GuildTable.guildSnowflake] = guildSnowflake
    it[GuildTable.prefix] = prefix
    it[GuildTable.logChannelSnowflake] = logChannelSnowflake
}
