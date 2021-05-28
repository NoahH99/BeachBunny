package com.noahhendrickson.beachbunny.bot.extensions

import com.gitlab.kordlib.kordx.emoji.Emojis
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.noahhendrickson.beachbunny.bot.database.ignore
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent

class GreeterExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Greeter"

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check(inChannel(Snowflake(642128851308118028)))

            action {
                val message = event.message

                if (message.author?.ignore == false)
                    message.addReaction(Emojis.wave.code)
            }
        }
    }
}
