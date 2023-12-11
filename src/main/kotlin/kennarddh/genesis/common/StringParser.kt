package kennarddh.genesis.common

class UnterminatedStringException(message: String) : Exception(message)

class InvalidEscapedCharacterException(message: String) : Exception(message)

open class StringParserToken

class SkipToken : StringParserToken()
data class StringToken(val value: String) : StringParserToken()

class StringParser {
    companion object {
        private val escapedCharactersMap = mapOf('n' to "\n", '\"' to "\"", '\\' to "\\", '*' to "*")

        fun parse(input: String): Iterator<StringParserToken> =
            iterator {
                var isEscaping = false
                var isInQuote = false

                val output = StringBuilder()

                for (char in input) {
                    if (isEscaping) {
                        if (escapedCharactersMap.contains(char)) {
                            output.append(escapedCharactersMap[char])

                            isEscaping = false

                            continue
                        } else {
                            throw InvalidEscapedCharacterException("$char is not a valid character to be escaped")
                        }
                    }

                    when (char) {
                        '\\' -> isEscaping = true
                        '"' -> isInQuote = !isInQuote
                        '*' -> yield(SkipToken())
                        ' ' -> {
                            if (!isInQuote) {
                                if (output.isNotEmpty()) {
                                    yield(StringToken(output.toString()))
                                    output.clear()
                                }
                            } else
                                output.append(char)
                        }

                        else -> output.append(char)
                    }
                }

                if (isEscaping) {
                    throw InvalidEscapedCharacterException("No character provided after escape character")
                } else if (isInQuote) {
                    throw UnterminatedStringException("Double quoted string $output is not terminated")
                } else if (output.isNotBlank()) {
                    yield(StringToken(output.toString()))
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
}