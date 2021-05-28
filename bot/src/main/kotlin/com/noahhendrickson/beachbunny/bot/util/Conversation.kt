package com.noahhendrickson.beachbunny.bot.util

import dev.kord.common.entity.Snowflake

data class Conversation(val userId: Snowflake, var messageId: Snowflake, var state: State) {

    enum class State {
        NONE, ADD, REMOVE, LIST
    }
}
