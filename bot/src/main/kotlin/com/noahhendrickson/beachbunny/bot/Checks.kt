package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.checks.userFor
import com.noahhendrickson.beachbunny.bot.database.isStaff
import dev.kord.core.event.Event
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T : Event> isNotBot(event: T): Boolean {
    val user = userFor(event)

    return when {
        user == null -> false
        user.asUserOrNull()?.isBot == true -> false
        else -> true
    }
}

suspend fun <T : Event> isStaff(event: T): Boolean {
    val user = userFor(event) ?: return false
    return newSuspendedTransaction { user.isStaff }
}
