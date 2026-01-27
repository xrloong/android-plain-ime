package cin

class CINParser {
    /**
     * 解析 CIN 格式的輸入法查表檔
     *
     * @param content CIN 檔案內容
     * @return 解析結果，包含字符到編碼的映射與編碼到候選字的映射
     * @throws CINParseException 當檔案格式錯誤時
     */
    fun parse(content: String): CINParseResult {
        if (content.isBlank()) {
            throw CINParseException("CIN 檔案內容為空")
        }

        val lines = content.lines()
        val charDefs = mutableListOf<CharDef>()
        val metadata = mutableMapOf<String, String>()
        val keyNameMap = mutableMapOf<Char, String>()
        var inCharDefSection = false
        var inKeynameSection = false

        for ((lineNum, line) in lines.withIndex()) {
            val trimmed = line.trim()

            // 跳過空行和註解
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue
            }

            when {
                trimmed.startsWith("%keyname") -> {
                    inCharDefSection = false
                    inKeynameSection = trimmed.contains("begin")
                    continue
                }
                trimmed.startsWith("%chardef") -> {
                    inCharDefSection = trimmed.contains("begin")
                    inKeynameSection = false
                    continue
                }
                trimmed.startsWith("%ename") -> {
                    metadata["ename"] = trimmed.substringAfter("%ename").trim()
                }
                trimmed.startsWith("%cname") -> {
                    metadata["cname"] = trimmed.substringAfter("%cname").trim()
                }
                trimmed.startsWith("%tname") -> {
                    metadata["tname"] = trimmed.substringAfter("%tname").trim()
                }
                trimmed.startsWith("%sname") -> {
                    metadata["sname"] = trimmed.substringAfter("%sname").trim()
                }
                trimmed.startsWith("%encoding") -> {
                    metadata["encoding"] = trimmed.substringAfter("%encoding").trim()
                }
                trimmed.startsWith("%selkey") -> {
                    metadata["selkey"] = trimmed.substringAfter("%selkey").trim()
                }
                trimmed.startsWith("%space_style") -> {
                    metadata["space_style"] = trimmed.substringAfter("%space_style").trim()
                }
                trimmed.startsWith("%") -> {
                    // 其他控制指令
                    if (trimmed.startsWith("%endkey") || trimmed.contains("end")) {
                        inCharDefSection = false
                        inKeynameSection = false
                    }
                    continue
                }
                inCharDefSection -> {
                    // 解析字符定義行：編碼 字符（使用 Tab 或空白分隔）
                    val parts = trimmed.split(Regex("[\\s\t]+"), limit = 2)
                    if (parts.size != 2) {
                        throw CINParseException("第 ${lineNum + 1} 行格式錯誤：'$trimmed'，應為 '編碼 字符'")
                    }
                    val code = parts[0]
                    val char = parts[1]

                    if (code.isEmpty()) {
                        throw CINParseException("第 ${lineNum + 1} 行編碼為空")
                    }
                    if (char.isEmpty()) {
                        throw CINParseException("第 ${lineNum + 1} 行字符為空")
                    }

                    charDefs.add(CharDef(code, char[0]))
                }
                inKeynameSection -> {
                    // 解析 keyname 定義：鍵 字根（使用 Tab 或空白分隔）
                    val parts = trimmed.split(Regex("[\\s\t]+"), limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].firstOrNull()
                        val rootLabel = parts[1]
                        if (key != null && rootLabel.isNotEmpty()) {
                            keyNameMap[key] = rootLabel
                        }
                    }
                }
            }
        }

        if (charDefs.isEmpty()) {
            throw CINParseException("未找到任何字符定義，請確認檔案包含 %chardef 區段")
        }

        // 建立映射表
        return buildMappings(charDefs, metadata, keyNameMap)
    }

    private fun buildMappings(
        charDefs: List<CharDef>,
        metadata: Map<String, String>,
        keyNameMap: Map<Char, String>
    ): CINParseResult {
        val charToCode = mutableMapOf<Char, String>()
        val codeToCandidates = mutableMapOf<String, MutableList<Char>>()

        for (charDef in charDefs) {
            // 字符到編碼的映射（一個字符可能有多個編碼，這裡保存第一個）
            if (!charToCode.containsKey(charDef.char)) {
                charToCode[charDef.char] = charDef.code
            }

            // 編碼到候選字的映射（一個編碼可能對應多個字符）
            codeToCandidates.getOrPut(charDef.code) { mutableListOf() }.add(charDef.char)
        }

        return CINParseResult(
            charToCode = charToCode,
            codeToCandidates = codeToCandidates.mapValues { it.value.toList() },
            metadata = metadata,
            keyNameMap = keyNameMap
        )
    }

    private data class CharDef(val code: String, val char: Char)
}

data class CINParseResult(
    val charToCode: Map<Char, String>,
    val codeToCandidates: Map<String, List<Char>>,
    val metadata: Map<String, String> = emptyMap(),
    val keyNameMap: Map<Char, String> = emptyMap()
) {
    /**
     * 根據編碼查詢候選字
     */
    fun getCandidates(code: String): List<Char> {
        return codeToCandidates[code] ?: emptyList()
    }

    /**
     * 根據字符查詢編碼
     */
    fun getCode(char: Char): String? {
        return charToCode[char]
    }

    /**
     * 根據鍵查詢字根標籤
     */
    fun getRootLabel(key: Char): String? {
        return keyNameMap[key.lowercaseChar()]
    }

    /**
     * 總共的字符定義數量
     */
    val totalChars: Int
        get() = charToCode.size

    /**
     * 輸入法英文名稱
     */
    val englishName: String
        get() = metadata["ename"] ?: "Unknown"

    /**
     * 輸入法中文名稱
     */
    val chineseName: String
        get() = metadata["cname"] ?: metadata["tname"] ?: "未知"

    /**
     * 輸入法顯示名稱 (優先級: sname > cname > tname > ename)
     */
    val displayName: String
        get() = metadata["sname"] ?: metadata["cname"] ?: metadata["tname"] ?: metadata["ename"] ?: "未知"

    /**
     * 選字鍵
     */
    val selectionKeys: String
        get() = metadata["selkey"] ?: "1234567890"
}
