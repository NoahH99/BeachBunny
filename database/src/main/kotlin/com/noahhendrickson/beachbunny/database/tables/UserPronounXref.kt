package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object UserPronounXref : IntIdTable() {

    val userId = reference("user_id", UserTable.id)
    val pronounId = reference("pronoun_id", PronounTable.id)

    val pronounEmoji = varchar("pronoun_emoji", 15)

}

fun selectName(userSnowflake: Long, messageSnowflake: Long, unicode: String): Triple<Long, Long, String>? {
    val select = IntroductionTable
        .innerJoin(UserTable)
        .innerJoin(GuildTable)
        .slice(GuildTable.guildSnowflake, UserTable.userSnowflake, IntroductionTable.name)
        .select {
            (UserTable.userSnowflake eq userSnowflake)
                .and(IntroductionTable.messageSnowflake eq messageSnowflake)
                .and(IntroductionTable.nameEmoji eq unicode)
        }.firstOrNull()

    return select?.let {
        Triple(
            it[GuildTable.guildSnowflake],
            it[UserTable.userSnowflake],
            it[IntroductionTable.name]
        )
    }
}

fun selectPronoun(userSnowflake: Long, messageSnowflake: Long, unicode: String): Triple<Long, Long, String>? {
    val select =
        IntroductionTable.join(UserPronounXref, JoinType.INNER) { IntroductionTable.userId eq UserPronounXref.userId }
            .join(UserTable, JoinType.INNER) { IntroductionTable.userId eq UserTable.id }
            .innerJoin(PronounTable)
            .innerJoin(GuildTable)
            .slice(GuildTable.guildSnowflake, UserTable.userSnowflake, PronounTable.value)
            .select {
                (UserTable.userSnowflake eq userSnowflake)
                    .and(IntroductionTable.messageSnowflake eq messageSnowflake)
                    .and(UserPronounXref.pronounEmoji eq unicode)
            }.firstOrNull()

    return select?.let {
        Triple(
            it[GuildTable.guildSnowflake],
            it[UserTable.userSnowflake],
            it[PronounTable.value]
        )
    }
}
