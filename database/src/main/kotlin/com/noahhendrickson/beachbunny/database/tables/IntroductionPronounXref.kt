package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object IntroductionPronounXref : IntIdTable() {

    val introductionId = reference("introduction_id", IntroductionTable.id, onDelete = ReferenceOption.CASCADE)
    val pronounId = reference("pronoun_id", PronounTable.id)

    val pronounEmoji = varchar("pronoun_emoji", 15)

}

fun selectName(userSnowflake: Long, unicode: String): Triple<Long, Long, String>? {
    val select =
        (IntroductionTable innerJoin IntroductionPronounXref innerJoin UserTable innerJoin GuildTable innerJoin PronounTable)
            .slice(GuildTable.guildSnowflake, UserTable.userSnowflake, IntroductionTable.name)
            .select { (UserTable.userSnowflake eq userSnowflake) and (IntroductionTable.nameEmoji eq unicode) }
            .firstOrNull()

    return select?.let {
        Triple(
            it[GuildTable.guildSnowflake],
            it[UserTable.userSnowflake],
            it[IntroductionTable.name]
        )
    }
}

fun selectPronoun(userSnowflake: Long, unicode: String): Triple<Long, Long, String>? {
    val select =
        (IntroductionTable innerJoin IntroductionPronounXref innerJoin UserTable innerJoin GuildTable innerJoin PronounTable)
            .slice(GuildTable.guildSnowflake, UserTable.userSnowflake, PronounTable.value)
            .select { (UserTable.userSnowflake eq userSnowflake) and (IntroductionPronounXref.pronounEmoji eq unicode) }
            .firstOrNull()


    return select?.let {
        Triple(
            it[GuildTable.guildSnowflake],
            it[UserTable.userSnowflake],
            it[PronounTable.value]
        )
    }
}
