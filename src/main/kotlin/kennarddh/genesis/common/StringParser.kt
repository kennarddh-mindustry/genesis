package kennarddh.genesis.common

class UnterminatedStringException(message: String) : Exception(message)

class InvalidEscapedCharacterException(message: String) : Exception(message)

class StringParser {
    companion object {
        private val escapedCharactersMap = mapOf('n' to "\n", '\"' to '\"', '\\' to "\\")

        fun parse(input: String) =
            iterator {
                var isEscaping = false
                var isInQuote = false

                val output = StringBuilder()

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
                    } else if (char == '"' && !isInQuote) {
                        isInQuote = true
                    } else if (char == '"' && isInQuote) {
                        isInQuote = false

                        yield(output.toString())

                        output.clear()
                    } else if (char == ' ' && !isInQuote) {
                        yield(output.toString())

                        output.clear()
                    } else {
                        output.append(char)
                    }
                }

                if (isEscaping) {
                    throw InvalidEscapedCharacterException("No character provided after escape character")
                } else if (output.isNotBlank() && isInQuote) {
                    throw UnterminatedStringException("String $output is not ended by a double quote")
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