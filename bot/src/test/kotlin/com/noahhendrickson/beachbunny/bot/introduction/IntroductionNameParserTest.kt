package com.noahhendrickson.beachbunny.bot.introduction

import com.noahhendrickson.beachbunny.bot.util.IntroductionParser
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class IntroductionNameParserTest {

    private val name = "John"

    @Test
    fun `Returns just the parsed name when creating IntroductionParser with just a name supplied`() {
        val parser = IntroductionParser("Name: $name")

        val actual = parser.parseName()

        Assertions.assertThat(actual).isEqualTo(name)
    }

    @Test
    fun `Returns just the parsed name when creating IntroductionParser with a name supplied and extra info in parentheses`() {
        val parser = IntroductionParser("Name: $name (This is extra information)")

        val actual = parser.parseName()

        Assertions.assertThat(actual).isEqualTo(name)
    }

    @Test
    fun `Returns just the parsed name when creating IntroductionParser when name is equal to null`() {
        val parser = IntroductionParser("Name: null")

        val expected = "null"
        val actual = parser.parseName()

        Assertions.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Returns just the parsed name when creating IntroductionParser with all fields supplied in proper order`() {
        val content = """
                |Name: $name
                |Pronouns: He/Him They/Them
                |How you found BB: This is filler text.
                |Favorite BB track: This is filler text.
                |About you: This is filler text.
            """.trimMargin()

        val parser = IntroductionParser(content)

        val actual = parser.parseName()

        Assertions.assertThat(actual).isEqualTo(name)
    }

    @Test
    fun `Returns just the parsed name when creating IntroductionParser with all fields supplied in improper order`() {
        val content = """
                |How you found BB: This is filler text.
                |Pronouns: He/Him They/Them
                |Favorite BB track: This is filler text.
                |Name: $name
                |About you: This is filler text.
            """.trimMargin()

        val parser = IntroductionParser(content)

        val actual = parser.parseName()

        Assertions.assertThat(actual).isEqualTo(name)
    }

    @Test
    fun `Returns null when creating IntroductionParser with no content provided`() {
        val parser = IntroductionParser("")

        val actual = parser.parseName()

        Assertions.assertThat(actual).isNull()
    }

    @Test
    fun `Returns null when creating IntroductionParser with no name supplied`() {
        val content = """
                |How you found BB: This is filler text.
                |Pronouns: He/Him They/Them
                |Favorite BB track: This is filler text.
                |About you: This is filler text.
            """.trimMargin()

        val parser = IntroductionParser(content)

        val actual = parser.parseName()

        Assertions.assertThat(actual).isNull()
    }
}
