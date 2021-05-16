package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object IntroductionTable : IntIdTable() {

    val userSnowflake = reference("user_snowflake", UserTable.userSnowflake)
    val guildSnowflake = reference("guild_snowflake", GuildTable.guildSnowflake)

    val name = varchar("name", 2000)
    val aboutMeResult = varchar("about_me_result", 200)

}
