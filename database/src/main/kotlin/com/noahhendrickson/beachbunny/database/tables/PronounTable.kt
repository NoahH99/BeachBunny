package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.insertAndGetId
import org.jetbrains.exposed.dao.id.IntIdTable

object PronounTable : IntIdTable() {

    val value = varchar("value", 25).uniqueIndex()

}

fun insertPronounAndGetId(value: String) =
    PronounTable.insertAndGetId({ PronounTable.value eq value }) {
        it[PronounTable.value] = value
    }

