package com.noahhendrickson.beachbunny.bot.introduction

import com.noahhendrickson.beachbunny.database.models.Pronoun
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class IntroductionPronounParserTest {

    @Suppress("unused")
    private val unused = Pronoun.pronouns // Don't delete this or a NPE will be thrown in the tests.

    @Test
    fun `Returns a set of one parsed pronoun when creating IntroductionParser with one pronoun supplied`() {
        val parser = IntroductionParser("Pronouns: He")

        val expected = setOf(Pronoun.He)
        val actual = parser.parsePronouns()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns a set of parsed pronouns when creating IntroductionParser with 3 pronouns supplied`() {
        val parser = IntroductionParser("Pronouns: He/Him/They")

        val expected = setOf(Pronoun.He, Pronoun.Him, Pronoun.They)
        val actual = parser.parsePronouns()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns a set of parsed pronouns when creating IntroductionParser with 4 pronouns supplied`() {
        val parser = IntroductionParser("Pronouns: She/Her/They/Them")

        val expected = setOf(Pronoun.She, Pronoun.Her, Pronoun.They, Pronoun.Them)
        val actual = parser.parsePronouns()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns a set of pronouns when creating IntroductionParser with all fields supplied in proper order`() {
        val content = """
                |Name: John
                |Pronouns: He/Him They/Them
                |How you found BB: This is filler text.
                |Favorite BB track: This is filler text.
                |About you: This is filler text.
            """.trimMargin()

        val parser = IntroductionParser(content)

        val expected = setOf(Pronoun.He, Pronoun.Him, Pronoun.They, Pronoun.Them)
        val actual = parser.parsePronouns()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns a set of pronouns when creating IntroductionParser with all fields supplied in improper order`() {
        val content = """
                |How you found BB: This is filler text.
                |Name: John
                |Favorite BB track: This is filler text.
                |Pronouns: She/They
                |About you: This is filler text.
            """.trimMargin()

        val parser = IntroductionParser(content)

        val expected = setOf(Pronoun.She, Pronoun.They)
        val actual = parser.parsePronouns()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns a set of Other pronouns when creating IntroductionParser with no valid pronouns 1`() {
        val parser = IntroductionParser("Pronouns: Fe/Fem")

        val expected = setOf(Pronoun.Other("Fe"), Pronoun.Other("Fem")).map { it.value }
        val actual = parser.parsePronouns().map { it.value }

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns a set of Other pronouns when creating IntroductionParser with no valid pronouns 2`() {
        val parser = IntroductionParser("Pronouns: Ve/Ver")

        val expected = setOf(Pronoun.Other("Ve"), Pronoun.Other("Ver")).map { it.value }
        val actual = parser.parsePronouns().map { it.value }

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns an empty set when creating IntroductionParser with no content provided`() {
        val parser = IntroductionParser("")

        val actual = parser.parsePronouns()

        assertThat(actual).isEmpty()
    }

    @Test
    fun `Returns an empty set when creating IntroductionParser with no pronouns supplied`() {
        val content = """
                |Name: John
                |How you found BB: This is filler text.
                |Favorite BB track: This is filler text.
                |About you: This is filler text.
            """.trimMargin()

        val parser = IntroductionParser(content)

        val actual = parser.parsePronouns()

        assertThat(actual).isEmpty()
    }
}
