package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.insertAndGetId
import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable() {

    val userSnowflake = long("user_snowflake").uniqueIndex()

    val bot = bool("bot").default(false)
    val admin = bool("admin").default(false)
    val ignore = bool("ignore").default(false)

}

fun insertUserAndGetId(
    userSnowflake: Long,
    bot: Boolean = false,
    admin: Boolean = false,
    ignore: Boolean = false,
) = UserTable.insertAndGetId({ UserTable.userSnowflake eq userSnowflake }) {
    it[UserTable.userSnowflake] = userSnowflake
    it[UserTable.bot] = bot
    it[UserTable.admin] = admin
    it[UserTable.ignore] = ignore
}
