package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object RoleTable : IntIdTable() {

    val roleSnowflake = long("role_snowflake")
    val guildSnowflake = reference("guild_snowflake", GuildTable.guildSnowflake)

}
