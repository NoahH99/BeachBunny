package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.models.Introduction
import com.noahhendrickson.beachbunny.database.models.Pronoun
import com.noahhendrickson.beachbunny.database.models.toPronoun
import com.noahhendrickson.beachbunny.database.util.insertAndGetId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object IntroductionTable : IntIdTable() {

    val name = varchar("name", 2000)
    val nameEmoji = varchar("name_emoji", 15)
    val aboutMeResult = varchar("about_me_result", 500)

    val messageSnowflake = long("message_snowflake").uniqueIndex()

    val userId = reference("user_id", UserTable.id)
    val guildId = reference("guild_id", GuildTable.id)

}

fun Introduction.insertOrUpdate(messageSnowflake: Long, userId: EntityID<Int>, guildId: EntityID<Int>) {
    transaction {
        val introductionExpression: SqlExpressionBuilder.() -> Op<Boolean> =
            { (IntroductionTable.userId eq userId) and (IntroductionTable.guildId eq guildId) }

        IntroductionTable.insertAndGetId(introductionExpression) {
            it[name] = this@insertOrUpdate.name
            it[nameEmoji] = this@insertOrUpdate.nameEmoji
            it[aboutMeResult] = this@insertOrUpdate.aboutMeResult

            it[IntroductionTable.messageSnowflake] = messageSnowflake

            it[IntroductionTable.userId] = userId
            it[IntroductionTable.guildId] = guildId
        }

        for (i in pronouns.indices) {
            val pronoun = pronouns.elementAt(i)
            val pronounId = insertPronounAndGetId(pronoun.value)

            val xrefExpression: SqlExpressionBuilder.() -> Op<Boolean> =
                { (UserPronounXref.userId eq userId) and (UserPronounXref.pronounId eq pronounId) }

            UserPronounXref.insertAndGetId(xrefExpression) {
                it[UserPronounXref.userId] = userId
                it[UserPronounXref.pronounId] = pronounId
                it[pronounEmoji] = Pronoun.unicodes.elementAt(i)
            }
        }

        (PronounTable leftJoin UserPronounXref)
            .select { UserPronounXref.userId eq userId }
            .forEach { result ->
                val pronounId = result[PronounTable.id]
                val pronoun = result[PronounTable.value].toPronoun()

                if (pronoun !in pronouns)
                    UserPronounXref.deleteWhere {
                        (UserPronounXref.userId eq userId) and (UserPronounXref.pronounId eq pronounId)
                    }
            }
    }
}
