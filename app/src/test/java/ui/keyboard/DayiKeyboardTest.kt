package ui.keyboard

import org.junit.Assert.*
import org.junit.Test

class DayiKeyboardTest {
    @Test
    fun testDayiHasExactly40Keys() {
        val dayiKeys = setOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            ',', '.', '/', ';'
        )
        assertEquals("Must have exactly 40 keys", 40, dayiKeys.size)
    }

    @Test
    fun testKeyNameResolverIncludesAllDayiKeys() {
        val dayiKeyNameMap = mapOf(
            '0' to "Gold", '1' to "Word", '2' to "Cow", '3' to "Eye", '4' to "Four",
            '5' to "King", '6' to "Gate", '7' to "Field", '8' to "Rice", '9' to "Foot",
            'a' to "Person", 'b' to "Horse", 'c' to "Seven", 'd' to "Sun", 'e' to "One",
            'f' to "Earth", 'g' to "Hand", 'h' to "Bird", 'i' to "Wood", 'j' to "Moon",
            'k' to "Stand", 'l' to "Woman", 'm' to "Rain", 'n' to "Fish", 'o' to "Mouth",
            'p' to "Ear", 'q' to "Stone", 'r' to "Work", 's' to "Leather", 't' to "Thread",
            'u' to "Grass", 'v' to "Grain", 'w' to "Mountain", 'x' to "Water", 'y' to "Fire",
            'z' to "Heart",
            ',' to "Power", '.' to "Dot", '/' to "Bamboo", ';' to "Bug"
        )

        val resolved = KeyNameResolver.resolve(
            keyNameMap = dayiKeyNameMap
        )

        // KeyNameResolver preserves all keys from input and adds missing letter keys
        // Since dayiKeyNameMap already has all 40 keys, result should also have 40
        assertEquals("KeyNameResolver should preserve all input keys", 40, resolved.size)
    }

    @Test
    fun testDayiKeyboardLayoutStructure() {
        val row1 = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val row2 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
        val row3 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';')
        val row4 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/')

        assertEquals("Row 1 should have 10 keys", 10, row1.size)
        assertEquals("Row 2 should have 10 keys", 10, row2.size)
        assertEquals("Row 3 should have 10 keys", 10, row3.size)
        assertEquals("Row 4 should have 10 keys", 10, row4.size)

        val allKeys = row1 + row2 + row3 + row4
        assertEquals("Total should be 40 keys", 40, allKeys.size)
        assertEquals("All keys should be unique", 40, allKeys.distinct().size)
    }

    @Test
    fun testDayiKeysBreakdown() {
        val dayiKeys = setOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            ',', '.', '/', ';'
        )

        val numbers = dayiKeys.filter { it.isDigit() }
        val letters = dayiKeys.filter { it.isLetter() }
        val punctuation = dayiKeys.filter { !it.isLetterOrDigit() }

        assertEquals("Should have 10 digit keys", 10, numbers.size)
        assertEquals("Should have 26 letter keys", 26, letters.size)
        assertEquals("Should have 4 punctuation keys", 4, punctuation.size)
    }
}
