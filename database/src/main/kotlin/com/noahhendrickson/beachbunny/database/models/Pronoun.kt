package com.noahhendrickson.beachbunny.database.models

sealed class Pronoun(val value: String) {

    object He : Pronoun("He")

    object Him : Pronoun("Him")

    object His : Pronoun("His")

    object She : Pronoun("She")

    object Her : Pronoun("Her")

    object Hers : Pronoun("Hers")

    object They : Pronoun("They")

    object Them : Pronoun("Them")

    object Theirs : Pronoun("Theirs")

    class Other(value: String) : Pronoun(value)

    override fun equals(other: Any?): Boolean {
        if (other !is Pronoun) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        val pronouns = setOf(He, Him, His, She, Her, Hers, They, Them, Theirs)
        val unicodes = setOf(
            "\uD83C\uDDE6",
            "\uD83C\uDDE7",
            "\uD83C\uDDE8",
            "\uD83C\uDDE9",
            "\uD83C\uDDEA",
            "\uD83C\uDDEB",
            "\uD83C\uDDEC",
            "\uD83C\uDDED",
            "\uD83C\uDDEE",
            "\uD83C\uDDEF",
            "\uD83C\uDDF0"
        )
    }
}

fun String.toPronoun(): Pronoun {
    return Pronoun.pronouns.firstOrNull {
        it.value.equals(this, ignoreCase = true)
    } ?: Pronoun.Other(capitalize())
}
