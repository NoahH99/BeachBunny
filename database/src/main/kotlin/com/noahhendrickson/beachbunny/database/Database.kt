package com.noahhendrickson.beachbunny.database

import com.noahhendrickson.beachbunny.database.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {

    private val url = System.getenv()["DATABASE_URL"]
        ?: throw IllegalArgumentException("No DATABASE_URL environment variable found!")

    private val user = System.getenv()["DATABASE_USER"]
        ?: throw IllegalArgumentException("No DATABASE_USER environment variable found!")

    private val password = System.getenv()["DATABASE_PASSWORD"]
        ?: throw IllegalArgumentException("No DATABASE_PASSWORD environment variable found!")

    private val tables = arrayOf(
        GuildTable,
        IntroductionPronounXref,
        IntroductionTable,
        PronounTable,
        RoleTable,
        UserTable
    )

    fun init(dropTables: Boolean = false): Database {
        val database = Database.connect(
            url = "jdbc:postgresql://$url",
            driver = "org.postgresql.Driver",
            user = user,
            password = password,
        )

        transaction {
            if (dropTables) SchemaUtils.drop(*tables)

            SchemaUtils.createMissingTablesAndColumns(*tables)
        }

        return database
    }
}
