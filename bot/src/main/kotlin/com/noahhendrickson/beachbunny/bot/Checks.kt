package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.checks.userFor
import dev.kord.core.event.Event

suspend fun <T: Event> isNotBot(event: T): Boolean {
    val user = userFor(event)

    return when {
        user == null -> false
        user.asUserOrNull()?.isBot == true -> false
        else -> true
    }
}
