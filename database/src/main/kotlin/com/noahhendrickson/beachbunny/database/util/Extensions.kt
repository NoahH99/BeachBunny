package com.noahhendrickson.beachbunny.database.util

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

fun <Key : Comparable<Key>, T : IdTable<Key>> T.insertAndGetId(
    expresion: SqlExpressionBuilder.() -> Op<Boolean>,
    body: T.(UpdateBuilder<*>) -> Unit,
): EntityID<Key> {
    val select = slice(id).select(expresion)

    return when (select.count()) {
        1L -> {
            update(expresion, body = body)
            select.first()[id]
        }
        0L -> insertIgnoreAndGetId(body)!!
        else -> throw UnsupportedOperationException()
    }
}
