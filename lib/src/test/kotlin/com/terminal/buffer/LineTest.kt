package com.terminal.buffer

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LineTest {
    @Test
    fun `new line has zero length`() {
        val line = Line()
        assertEquals(0, line.length)
    }

    @Test
    fun `getCell returns empty for unset positions`() {
        val line = Line()
        assertEquals(Cell.EMPTY, line.getCell(0))
        assertEquals(Cell.EMPTY, line.getCell(100))
    }

    @Test
    fun `setCell expands line as needed`() {
        val line = Line()
        val cell = Cell('A', CellAttributes())

        line.setCell(5, cell)

        assertEquals(6, line.length)
        assertEquals(cell, line.getCell(5))
    }

    @Test
    fun `setCell fills gaps with empty cells`() {
        val line = Line()
        line.setCell(3, Cell('X', CellAttributes()))

        assertEquals(Cell.EMPTY, line.getCell(0))
        assertEquals(Cell.EMPTY, line.getCell(1))
        assertEquals(Cell.EMPTY, line.getCell(2))
        assertEquals('X', line.getCell(3).char)
    }

    @Test
    fun `getText returns characters from cells`() {
        val line = Line()
        line.setCell(0, Cell('H', CellAttributes()))
        line.setCell(1, Cell('e', CellAttributes()))
        line.setCell(2, Cell('l', CellAttributes()))
        line.setCell(3, Cell('l', CellAttributes()))
        line.setCell(4, Cell('o', CellAttributes()))

        assertEquals("Hello", line.getText())
    }

    @Test
    fun `getText returns empty string for empty line`() {
        val line = Line()
        assertEquals("", line.getText())
    }

    @Test
    fun `getTextPadded pads to requested width`() {
        val line = Line()
        line.setCell(0, Cell('H', CellAttributes()))
        line.setCell(1, Cell('i', CellAttributes()))

        assertEquals("Hi   ", line.getTextPadded(5))
    }

    @Test
    fun `getTextPadded truncates if line is longer`() {
        val line = Line()
        line.setCell(0, Cell('H', CellAttributes()))
        line.setCell(1, Cell('e', CellAttributes()))
        line.setCell(2, Cell('l', CellAttributes()))
        line.setCell(3, Cell('l', CellAttributes()))
        line.setCell(4, Cell('o', CellAttributes()))

        assertEquals("Hel", line.getTextPadded(3))
    }

    @Test
    fun `fill sets cells to specified value`() {
        val line = Line()
        val cell = Cell('X', CellAttributes(foreground = Color.RED))

        line.fill(cell, 5)

        assertEquals(5, line.length)
        for (i in 0 until 5) {
            assertEquals(cell, line.getCell(i))
        }
        assertEquals("XXXXX", line.getText())
    }

    @Test
    fun `fill replaces existing content`() {
        val line = Line()
        line.setCell(0, Cell('A', CellAttributes()))
        line.setCell(1, Cell('B', CellAttributes()))

        line.fill(Cell('X', CellAttributes()), 3)

        assertEquals("XXX", line.getText())
    }

    @Test
    fun `clear removes all cells`() {
        val line = Line()
        line.setCell(0, Cell('A', CellAttributes()))
        line.setCell(1, Cell('B', CellAttributes()))

        line.clear()

        assertEquals(0, line.length)
        assertEquals("", line.getText())
    }
}
