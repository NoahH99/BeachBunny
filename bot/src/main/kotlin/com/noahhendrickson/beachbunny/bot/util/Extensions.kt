package com.noahhendrickson.beachbunny.bot.util

import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList

fun EmbedBuilder.addBranding() {
    footer { text = "Developed by NoahH99" }
}

suspend fun Member.getTopRoleWithColor(): Role? {
    return roles.filter { it.color.rgb != 0 }.toList().maxByOrNull { it.rawPosition }
}
