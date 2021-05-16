package com.noahhendrickson.beachbunny.database

import com.noahhendrickson.beachbunny.database.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object Database {

    private val path = System.getenv()["DATABASE_PATH"] ?: throw IllegalArgumentException("No database path found!")

    private val tables = arrayOf(GuildTable, IntroductionTable, PronounTable, RoleTable, UserTable)

    fun init(dropTables: Boolean = false) {
        Database.connect(
            url = "jdbc:sqlite:$path/beach-bunny-dev.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            addLogger(StdOutSqlLogger)

            if (dropTables) SchemaUtils.drop(*tables)

            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }
}
