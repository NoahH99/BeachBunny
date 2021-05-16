package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable() {

    val userSnowflake = long("user_snowflake")

}
