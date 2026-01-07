package table

import cin.CINParseResult

class TableCache {
    private val cache = mutableMapOf<String, CINParseResult>()

    /**
     * 儲存查表資料到快取
     */
    fun put(key: String, data: CINParseResult) {
        cache[key] = data
    }

    /**
     * 從快取取得查表資料
     */
    fun get(key: String): CINParseResult? {
        return cache[key]
    }

    /**
     * 清除指定的快取
     */
    fun remove(key: String) {
        cache.remove(key)
    }

    /**
     * 清除所有快取
     */
    fun clear() {
        cache.clear()
    }

    /**
     * 檢查快取是否存在
     */
    fun contains(key: String): Boolean {
        return cache.containsKey(key)
    }
}
