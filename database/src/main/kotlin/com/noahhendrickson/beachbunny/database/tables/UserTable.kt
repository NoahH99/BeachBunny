package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertIgnore

object UserTable : IntIdTable() {

    val userSnowflake = long("user_snowflake")
    val isAdmin = bool("is_admin").default(false)

}

fun insertUser(id: Long) {
    UserTable.insertIgnore { it[userSnowflake] = id }
}
