package cin

import cin.CINParseException

class CINParser {
    fun parse(content: String): CINParseResult {
        // TODO: implement CIN parsing logic
        throw CINParseException("Not implemented")
    }
}

data class CINParseResult(
    val charToCode: Map<Char, String>,
    val codeToCandidates: Map<String, List<Char>>
)
