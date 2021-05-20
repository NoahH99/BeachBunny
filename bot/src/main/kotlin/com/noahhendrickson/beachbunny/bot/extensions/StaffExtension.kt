package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.noahhendrickson.beachbunny.bot.isStaff

class StaffExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Staff"

    override suspend fun setup() {
        command {
            name = "config"
            description = "Returns all the configuration values."

            check(::isStaff)

            action {}
        }
    }
}
