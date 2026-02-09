package cin

class CharFrequencyTable private constructor(
    private val rankMap: Map<Char, Int>
) {
    val size: Int get() = rankMap.size

    fun getRank(char: Char): Int {
        return rankMap[char] ?: Int.MAX_VALUE
    }

    fun sortCandidates(candidates: List<Char>): List<Char> {
        return candidates.sortedWith(compareBy { getRank(it) })
    }

    companion object {
        val EMPTY = CharFrequencyTable(emptyMap())

        fun parse(content: String): CharFrequencyTable {
            val rankMap = mutableMapOf<Char, Int>()
            var rank = 0
            for (line in content.lineSequence()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
                val char = trimmed[0]
                if (!rankMap.containsKey(char)) {
                    rankMap[char] = rank
                    rank++
                }
            }
            return CharFrequencyTable(rankMap)
        }
    }
}
