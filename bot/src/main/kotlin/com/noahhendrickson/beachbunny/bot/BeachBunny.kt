package com.noahhendrickson.beachbunny.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot

suspend fun main() {
    val token = System.getenv()["BOT_TOKEN"] ?: throw IllegalArgumentException("No token provided!")

    val bot = ExtensibleBot(token)

    bot.start()
}
