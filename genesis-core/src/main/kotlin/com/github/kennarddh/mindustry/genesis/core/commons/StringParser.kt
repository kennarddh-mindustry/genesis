package com.github.kennarddh.mindustry.genesis.core.commons

class UnterminatedStringException(message: String) : Exception(message)

class InvalidEscapedCharacterException(message: String) : Exception(message)

open class StringParserToken(open val toBeParsed: String)

data class SkipToken(override val toBeParsed: String) : StringParserToken(toBeParsed)
data class StringToken(val value: String, override val toBeParsed: String) : StringParserToken(toBeParsed)

object StringParser {
    private val escapedCharactersMap = mapOf('n' to "\n", '\"' to "\"", '\\' to "\\", '*' to "*")

    fun parse(input: String): Iterator<StringParserToken> =
        iterator {
            var isEscaping = false
            var isInQuote = false

            var toBeParsed = input

            val output = buildString {
                while (toBeParsed.isNotEmpty()) {
                    val char = toBeParsed[0]

                    toBeParsed = toBeParsed.drop(1)

                    if (isEscaping) {
                        if (escapedCharactersMap.contains(char)) {
                            append(escapedCharactersMap[char])

                            isEscaping = false

                            continue
                        } else {
                            throw InvalidEscapedCharacterException("$char is not a valid character to be escaped")
                        }
                    }

                    when (char) {
                        '\\' -> isEscaping = true
                        '"' -> isInQuote = !isInQuote
                        '*' -> {
                            if (!isInQuote && isEmpty())
                                yield(SkipToken(toBeParsed))
                            else
                                append(char)
                        }

                        ' ' -> {
                            if (!isInQuote) {
                                if (isNotEmpty()) {
                                    yield(StringToken(toString(), toBeParsed))
                                    clear()
                                }
                            } else
                                append(char)
                        }

                        else -> append(char)
                    }
                }
            }

            if (isEscaping) {
                throw InvalidEscapedCharacterException("No character provided after escape character")
            } else if (isInQuote) {
                throw UnterminatedStringException("Double quoted string $output is not terminated")
            } else if (output.isNotBlank()) {
                yield(StringToken(output, ""))
            }
        }

    fun parseToArray(input: String): Array<StringParserToken> {
        val parsed = parse(input)

        val output: MutableList<StringParserToken> = mutableListOf()

        parsed.forEach {
            output.add(it)
        }

        return output.toTypedArray()
    }
}