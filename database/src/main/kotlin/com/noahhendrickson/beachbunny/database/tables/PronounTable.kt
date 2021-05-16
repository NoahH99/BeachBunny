package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object PronounTable : IntIdTable() {

    val value = varchar("value", 25)
    val roleSnowflake = reference("role_snowflake", RoleTable.id)

}
