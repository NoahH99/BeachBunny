package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.insertAndGetId
import com.noahhendrickson.beachbunny.database.models.Introduction
import com.noahhendrickson.beachbunny.database.models.Pronoun
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

object IntroductionTable : IntIdTable() {

    val name = varchar("name", 2000)
    val nameEmoji = varchar("name_emoji", 15)

    val aboutMeResult = varchar("about_me_result", 500)

    val userId = reference("user_id", UserTable.id)
    val guildId = reference("guild_id", GuildTable.id)

}

fun Introduction.insert() {
    val userId = insertUserAndGetId(userSnowflake)
    val guildId = insertGuildAndGetId(guildSnowflake)

    val introductionExpression: SqlExpressionBuilder.() -> Op<Boolean> =
        { (IntroductionTable.userId eq userId) and (IntroductionTable.guildId eq guildId) }

    val introductionId = IntroductionTable.insertAndGetId(introductionExpression) {
        it[name] = this@insert.name
        it[nameEmoji] = this@insert.nameEmoji
        it[aboutMeResult] = this@insert.aboutMeResult

        it[IntroductionTable.userId] = userId
        it[IntroductionTable.guildId] = guildId
    }

    for (i in pronouns.indices) {
        val pronoun = pronouns.elementAt(i)
        val pronounId = insertPronounAndGetId(pronoun.value)

        val xrefExpression: SqlExpressionBuilder.() -> Op<Boolean> =
            { (IntroductionPronounXref.introductionId eq introductionId) and (IntroductionPronounXref.pronounId eq pronounId) }

        IntroductionPronounXref.insertAndGetId(xrefExpression) {
            it[IntroductionPronounXref.introductionId] = introductionId
            it[IntroductionPronounXref.pronounId] = pronounId
            it[pronounEmoji] = Pronoun.unicodes.elementAt(i + 1)
        }
    }

    (PronounTable leftJoin IntroductionPronounXref)
        .select { IntroductionPronounXref.introductionId eq introductionId }
        .forEach { result ->
            val pronounId = result[PronounTable.id]
            val pronoun = result[PronounTable.value]

            if (pronoun !in pronouns.map { it.value })
                IntroductionPronounXref.deleteWhere {
                    (IntroductionPronounXref.introductionId eq introductionId) and (IntroductionPronounXref.pronounId eq pronounId)
                }
        }
}
