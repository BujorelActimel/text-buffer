package com.terminal.buffer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class LineTest {
    @Test
    fun `creates line with correct width`() {
        val line = Line(80)
        assertEquals(80, line.width)
    }

    @Test
    fun `new line contains empty cells`() {
        val line = Line(10)
        for (i in 0 until 10) {
            assertEquals(Cell.EMPTY, line.getCell(i))
        }
    }

    @Test
    fun `getText returns spaces for empty line`() {
        val line = Line(5)
        assertEquals("     ", line.getText())
    }

    @Test
    fun `setCell and getCell work correctly`() {
        val line = Line(10)
        val cell = Cell('A', CellAttributes())

        line.setCell(5, cell)

        assertEquals(cell, line.getCell(5))
    }

    @Test
    fun `getText returns characters from cells`() {
        val line = Line(5)
        line.setCell(0, Cell('H', CellAttributes()))
        line.setCell(1, Cell('e', CellAttributes()))
        line.setCell(2, Cell('l', CellAttributes()))
        line.setCell(3, Cell('l', CellAttributes()))
        line.setCell(4, Cell('o', CellAttributes()))

        assertEquals("Hello", line.getText())
    }

    @Test
    fun `getCell throws on negative index`() {
        val line = Line(10)
        assertThrows<IllegalArgumentException> {
            line.getCell(-1)
        }
    }

    @Test
    fun `getCell throws on index equal to width`() {
        val line = Line(10)
        assertThrows<IllegalArgumentException> {
            line.getCell(10)
        }
    }

    @Test
    fun `setCell throws on negative index`() {
        val line = Line(10)
        assertThrows<IllegalArgumentException> {
            line.setCell(-1, Cell.EMPTY)
        }
    }

    @Test
    fun `setCell throws on out of bounds index`() {
        val line = Line(10)
        assertThrows<IllegalArgumentException> {
            line.setCell(15, Cell.EMPTY)
        }
    }

    @Test
    fun `fill replaces all cells`() {
        val line = Line(5)
        val cell = Cell('X', CellAttributes(foreground = Color.RED))

        line.fill(cell)

        for (i in 0 until 5) {
            assertEquals(cell, line.getCell(i))
        }
        assertEquals("XXXXX", line.getText())
    }

    @Test
    fun `clear resets all cells to empty`() {
        val line = Line(5)
        line.fill(Cell('X', CellAttributes()))

        line.clear()

        for (i in 0 until 5) {
            assertEquals(Cell.EMPTY, line.getCell(i))
        }
    }
}
