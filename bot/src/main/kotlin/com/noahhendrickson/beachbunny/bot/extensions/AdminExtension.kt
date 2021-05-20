package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.stringList
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import com.noahhendrickson.beachbunny.database.util.format
import dev.kord.common.annotation.KordPreview
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.system.measureTimeMillis

@KordPreview
@ExperimentalStdlibApi
class AdminExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Admin"

    override suspend fun setup() {
        command(::SqlArguments) {
            name = "sql"
            description = "Run SQL statements again the database."

            action {
                if (user?.id?.value != 216709975307845633L) return@action

                newSuspendedTransaction {
                    var response = "No Result"

                    val time = measureTimeMillis {
                        val query = arguments.query.joinToString(" ")

                        try {
                            exec(query) { response = it.format() }
                        } catch (e: Exception) {
                            response = e.localizedMessage
                        }
                    }

                    message.respond("```\n$response\n```*Operation took ${time}ms*")
                }
            }
        }
    }

    class SqlArguments : Arguments() {
        val query by stringList("query", "")
    }
}
