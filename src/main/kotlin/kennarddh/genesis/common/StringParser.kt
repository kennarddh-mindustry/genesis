package kennarddh.genesis.common

class UnterminatedStringException(message: String) : Exception(message)

class InvalidEscapedCharacterException(message: String) : Exception(message)

class InvalidStringParsingException(message: String) : Exception(message)

class StringParser {
    companion object {
        private val escapedCharactersMap = mapOf('n' to "\n", '\"' to '\"', '\\' to "\\")

        fun parse(input: String) =
            iterator {
                var isEscaping = false
                var isInQuote = false
                var isReadyForNext = true

                val output = StringBuilder()

                // TODO: Refactor if else hell
                for (char in input) {
                    if (isEscaping) {
                        if (escapedCharactersMap.contains(char)) {
                            output.append(escapedCharactersMap[char])

                            isEscaping = false
                        } else {
                            throw InvalidEscapedCharacterException("$char is not a valid character to escape")
                        }
                    } else if (char == '\\') {
                        isEscaping = true
                    } else if (char == ' ' && output.isEmpty()) {
                        isReadyForNext = true
                    } else if (char == ' ' && !isInQuote) {
                        isReadyForNext = true

                        yield(output.toString())

                        output.clear()
                    } else if (char == '"' && !isInQuote && output.isEmpty()) {
                        if (!isReadyForNext && output.isEmpty()) {
                            throw InvalidStringParsingException("Parameter must be separated by space")
                        } else {
                            isInQuote = true
                        }
                    } else if (char == '"') {
                        if (isInQuote) {
                            isInQuote = false
                            isReadyForNext = false

                            yield(output.toString())

                            output.clear()
                        } else {
                            throw InvalidStringParsingException("Cannot use double quote without being escaped other than for starting quoted string")
                        }
                    } else if (char == ' ' && output.isEmpty()) {
                        continue
                    } else {
                        if (!isReadyForNext && output.isEmpty()) {
                            throw InvalidStringParsingException("Parameter must be separated by space")
                        } else {
                            output.append(char)
                        }
                    }
                }

                if (isEscaping) {
                    throw InvalidEscapedCharacterException("No character provided after escape character")
                } else if (output.isNotBlank() && isInQuote) {
                    throw UnterminatedStringException("Double quoted string $output is not terminated")
                } else if (output.isNotBlank()) {
                    yield(output.toString())
                }
            }

        fun parseToArray(input: String): Array<String> {
            val parsed = parse(input)

            val output: MutableList<String> = mutableListOf()

            parsed.forEach {
                output.add(it)
            }

            return output.toTypedArray()
        }
    }
}