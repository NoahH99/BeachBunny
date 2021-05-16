package com.noahhendrickson.beachbunny.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object IntroductionPronounXref : IntIdTable() {

    val introductionId = reference("introduction_id", IntroductionTable.id, onDelete = ReferenceOption.CASCADE)
    val pronounId = reference("pronoun_id", PronounTable.id, onDelete = ReferenceOption.CASCADE)

}
