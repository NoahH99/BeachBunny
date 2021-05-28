package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.stringList
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.noahhendrickson.beachbunny.bot.database.admin
import com.noahhendrickson.beachbunny.database.util.format
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.system.measureTimeMillis

class AdminExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Admin"

    override suspend fun setup() {
        command(::SqlArguments) {
            name = "sql"
            description = "Run SQL statements again the database."

            action {
                if (user?.admin == false) return@action
                message.respond(runSql(arguments.query.joinToString(" ")))
            }
        }
    }

    class SqlArguments : Arguments() {
        val query by stringList("query", "")
    }
}

private suspend fun runSql(query: String): String {
    return newSuspendedTransaction {
        var response = "No Result"

        val time = measureTimeMillis {
            try {
                exec(query) { response = it.format() }
            } catch (e: Exception) {
                response = e.localizedMessage
            }
        }

        "```\n$response\n```*Operation took ${time}ms*"
    }
}
