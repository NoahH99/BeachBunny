package com.noahhendrickson.beachbunny.database.models

data class Introduction(
    val userSnowflake: Long,
    val guildSnowflake: Long,
    private val _name: String,
    val nameEmoji: String,
    val pronouns: Set<Pronoun>,
    private val _aboutMeResult: String
) {

    val name = if (_name.length > 2000) _name.substring(0, 1990) + " (...)" else _name

    val aboutMeResult = if (_aboutMeResult.length > 500) _aboutMeResult.substring(0, 490) + " (...)" else _aboutMeResult

}
