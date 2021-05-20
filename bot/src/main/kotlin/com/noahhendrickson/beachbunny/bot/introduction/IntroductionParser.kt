package com.noahhendrickson.beachbunny.bot.introduction

import com.noahhendrickson.beachbunny.bot.dialogflow.DialogflowAssistant
import com.noahhendrickson.beachbunny.bot.dialogflow.detectIntentTextAsync
import com.noahhendrickson.beachbunny.database.models.Pronoun
import com.noahhendrickson.beachbunny.database.models.toPronoun
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class IntroductionParser(content: String) {

    private val contentSplit = content.split("\n")

    private val nameRegex by lazy {
        "^(?:Preferred)?\\s?(?:Nick)?Name/?(?:Preferred)?\\s?(?:Nick)?(?:Name)?:\\s?(\\w[a-z ]*).*$"
            .toRegex(RegexOption.IGNORE_CASE)
    }

    private val pronounLineRegex by lazy { "^Pronouns?:\\s?(.+)$".toRegex(RegexOption.IGNORE_CASE) }

    private val pronounRegex by lazy {
        buildString {
            append("(\\w+)")
            repeat(9) { append("[/ ]*(\\w+)?") }
        }.toRegex()
    }

    private val aboutYouRegex by lazy { "^About You:\\s?(.+)\$".toRegex(RegexOption.IGNORE_CASE) }

    fun parseName(): String? {
        contentSplit.forEach {
            if (it.matches(nameRegex))
                return nameRegex.find(it)!!.groupValues[1].trimEnd()
        }

        return null
    }

    fun parsePronouns(): Set<Pronoun> {
        val pronouns: MutableSet<Pronoun> = mutableSetOf()

        contentSplit.forEach { line ->
            if (line.matches(pronounLineRegex)) {
                val possiblePronouns = pronounLineRegex.find(line)!!.groupValues[1]
                val match = pronounRegex.find(possiblePronouns) ?: return pronouns

                match.groupValues.drop(1).forEach {
                    if (it.isNotEmpty()) pronouns.add(it.toPronoun())
                }

                return pronouns
            }
        }

        return pronouns
    }

    suspend fun parseAboutYou(assistant: DialogflowAssistant): String {
        contentSplit.forEach {
            if (it.matches(aboutYouRegex)) {
                val message = aboutYouRegex.find(it)!!.groupValues[1]

                return assistant.detectIntentTextAsync(message)?.await()?.fulfillmentText ?: ""
            }
        }

        return ""
    }

    operator fun component1() = this
    operator fun component2() = parseName()
    operator fun component3() = parsePronouns()
}
