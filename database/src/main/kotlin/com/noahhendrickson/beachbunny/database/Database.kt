package com.noahhendrickson.beachbunny.database

import com.noahhendrickson.beachbunny.database.tables.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

object Database {

    private val path = System.getenv()["DATABASE_PATH"] ?: throw IllegalArgumentException("No database path found!")

    private val tables = arrayOf(
        GuildTable,
        IntroductionPronounXref,
        IntroductionTable,
        PronounTable,
        RoleTable,
        UserTable
    )

    fun init(dropTables: Boolean = false) {
        Database.connect(
            url = "jdbc:sqlite:$path/beach-bunny-dev.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            if (dropTables) SchemaUtils.drop(*tables)

            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }
}

fun <Key : Comparable<Key>, T : IdTable<Key>> T.insertAndGetId(
    expresion: SqlExpressionBuilder.() -> Op<Boolean>,
    body: T.(UpdateBuilder<*>) -> Unit,
): EntityID<Key> {
    val select = select(expresion)

    return when (select.count()) {
        1L -> {
            update(expresion, body = body)
            select.first()[this.id]
        }
        0L -> insertIgnoreAndGetId(body)!!
        else -> throw UnsupportedOperationException()
    }
}
