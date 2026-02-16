package com.terminal.buffer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class TerminalBufferTest {
    // Setup and dimensions
    @Test
    fun `creates buffer with correct dimensions`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertEquals(80, buffer.width)
        assertEquals(24, buffer.height)
        assertEquals(1000, buffer.maxScrollBack)
    }

    @Test
    fun `scrollback starts empty`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertEquals(0, buffer.scrollbackSize)
    }

    // Cursor operations
    @Test
    fun `cursor starts at origin`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        val cursor = buffer.getCursor()
        assertEquals(0, cursor.column)
        assertEquals(0, cursor.row)
    }

    @Test
    fun `getCursor returns a copy`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        val cursor1 = buffer.getCursor()
        val cursor2 = buffer.getCursor()
        assertNotSame(cursor1, cursor2)
    }

    @Test
    fun `setCursor updates position`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 5)
        val cursor = buffer.getCursor()
        assertEquals(10, cursor.column)
        assertEquals(5, cursor.row)
    }

    @Test
    fun `setCursor clamps column to bounds`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(100, 5)
        assertEquals(79, buffer.getCursor().column)

        buffer.setCursor(-10, 5)
        assertEquals(0, buffer.getCursor().column)
    }

    @Test
    fun `setCursor clamps row to bounds`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 50)
        assertEquals(23, buffer.getCursor().row)

        buffer.setCursor(10, -5)
        assertEquals(0, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorUp moves cursor up`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 10)
        buffer.moveCursorUp(3)
        assertEquals(7, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorUp stops at top`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 5)
        buffer.moveCursorUp(10)
        assertEquals(0, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorUp throws on negative`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertThrows<IllegalArgumentException> {
            buffer.moveCursorUp(-1)
        }
    }

    @Test
    fun `moveCursorDown moves cursor down`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 10)
        buffer.moveCursorDown(5)
        assertEquals(15, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorDown stops at bottom`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 20)
        buffer.moveCursorDown(10)
        assertEquals(23, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorDown throws on negative`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertThrows<IllegalArgumentException> {
            buffer.moveCursorDown(-1)
        }
    }

    @Test
    fun `moveCursorLeft moves cursor left`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 5)
        buffer.moveCursorLeft(3)
        assertEquals(7, buffer.getCursor().column)
    }

    @Test
    fun `moveCursorLeft stops at left edge`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(5, 5)
        buffer.moveCursorLeft(10)
        assertEquals(0, buffer.getCursor().column)
    }

    @Test
    fun `moveCursorLeft throws on negative`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertThrows<IllegalArgumentException> {
            buffer.moveCursorLeft(-1)
        }
    }

    @Test
    fun `moveCursorRight moves cursor right`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(10, 5)
        buffer.moveCursorRight(5)
        assertEquals(15, buffer.getCursor().column)
    }

    @Test
    fun `moveCursorRight stops at right edge`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(75, 5)
        buffer.moveCursorRight(10)
        assertEquals(79, buffer.getCursor().column)
    }

    @Test
    fun `moveCursorRight throws on negative`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertThrows<IllegalArgumentException> {
            buffer.moveCursorRight(-1)
        }
    }

    // Attributes management
    @Test
    fun `attributes start with defaults`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        val attrs = buffer.getCurrentAttributes()
        assertEquals(Color.DEFAULT, attrs.foreground)
        assertEquals(Color.DEFAULT, attrs.background)
        assertEquals(false, attrs.styleFlags.bold)
        assertEquals(false, attrs.styleFlags.italic)
        assertEquals(false, attrs.styleFlags.underline)
    }

    @Test
    fun `setForeground updates foreground color`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setForeground(Color.RED)
        assertEquals(Color.RED, buffer.getCurrentAttributes().foreground)
    }

    @Test
    fun `setBackground updates background color`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setBackground(Color.BLUE)
        assertEquals(Color.BLUE, buffer.getCurrentAttributes().background)
    }

    @Test
    fun `setStyle updates bold`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setStyle(bold = true, italic = null, underline = null)
        assertEquals(true, buffer.getCurrentAttributes().styleFlags.bold)
        assertEquals(false, buffer.getCurrentAttributes().styleFlags.italic)
    }

    @Test
    fun `setStyle updates multiple styles`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setStyle(bold = true, italic = true, underline = true)
        val styles = buffer.getCurrentAttributes().styleFlags
        assertEquals(true, styles.bold)
        assertEquals(true, styles.italic)
        assertEquals(true, styles.underline)
    }

    @Test
    fun `setStyle with null preserves existing`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setStyle(bold = true, italic = false, underline = true)
        buffer.setStyle(bold = null, italic = true, underline = null)

        val styles = buffer.getCurrentAttributes().styleFlags
        assertEquals(true, styles.bold)      // preserved
        assertEquals(true, styles.italic)    // changed
        assertEquals(true, styles.underline) // preserved
    }

    @Test
    fun `resetAttributes resets to defaults`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setForeground(Color.RED)
        buffer.setBackground(Color.BLUE)
        buffer.setStyle(bold = true, italic = true, underline = true)

        buffer.resetAttributes()

        val attrs = buffer.getCurrentAttributes()
        assertEquals(Color.DEFAULT, attrs.foreground)
        assertEquals(Color.DEFAULT, attrs.background)
        assertEquals(false, attrs.styleFlags.bold)
        assertEquals(false, attrs.styleFlags.italic)
        assertEquals(false, attrs.styleFlags.underline)
    }

    // Basic Editing
    @Test
    fun `writeChar writes character at cursor position`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeChar('A')

        // Check character was written at (0, 0)
        val line = buffer.getScreenLine(0)
        assertEquals('A', line.getCell(0).char)
    }

    @Test
    fun `writeChar moves cursor right`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeChar('A')

        val cursor = buffer.getCursor()
        assertEquals(1, cursor.column)
        assertEquals(0, cursor.row)
    }

    @Test
    fun `writeChar wraps to next line at end of line`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setCursor(9, 0)
        buffer.writeChar('X')

        val cursor = buffer.getCursor()
        assertEquals(0, cursor.column)
        assertEquals(1, cursor.row)
    }

    @Test
    fun `writeChar scrolls at last row and column`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setCursor(9, 4)  // Last position
        buffer.writeChar('X')

        val cursor = buffer.getCursor()
        assertEquals(0, cursor.column)
        assertEquals(4, cursor.row)  // Still at last row
        assertEquals(1, buffer.scrollbackSize)  // Scrollback increased
    }

    @Test
    fun `writeChar applies current attributes`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setForeground(Color.RED)
        buffer.setBackground(Color.BLUE)
        buffer.setStyle(bold = true, italic = false, underline = true)

        buffer.writeChar('A')

        val cell = buffer.getScreenLine(0).getCell(0)
        assertEquals('A', cell.char)
        assertEquals(Color.RED, cell.attributes.foreground)
        assertEquals(Color.BLUE, cell.attributes.background)
        assertEquals(true, cell.attributes.styleFlags.bold)
        assertEquals(true, cell.attributes.styleFlags.underline)
    }

    @Test
    fun `writeText writes multiple characters`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello")

        val line = buffer.getScreenLine(0)
        assertEquals("Hello     ", line.getText())
        assertEquals(5, buffer.getCursor().column)
    }

    @Test
    fun `writeText handles newline`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("AB\nCD")

        assertEquals("AB        ", buffer.getScreenLine(0).getText())
        assertEquals("CD        ", buffer.getScreenLine(1).getText())
        assertEquals(2, buffer.getCursor().column)
        assertEquals(1, buffer.getCursor().row)
    }

    @Test
    fun `writeText handles carriage return`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABCD")
        buffer.writeText("\rXY")

        // \r moves to column 0, then XY overwrites AB
        assertEquals("XYCD      ", buffer.getScreenLine(0).getText())
        assertEquals(2, buffer.getCursor().column)
    }

    @Test
    fun `writeText wraps at line end`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("12345678901234")  // 14 chars for width 10

        assertEquals("1234567890", buffer.getScreenLine(0).getText())
        assertEquals("1234      ", buffer.getScreenLine(1).getText())
        assertEquals(4, buffer.getCursor().column)
        assertEquals(1, buffer.getCursor().row)
    }

    @Test
    fun `fillLine fills line with character and current attributes`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setForeground(Color.GREEN)
        buffer.fillLine(2, '-')

        val line = buffer.getScreenLine(2)
        assertEquals("----------", line.getText())

        // Check attributes on first cell
        assertEquals(Color.GREEN, line.getCell(0).attributes.foreground)
    }

    @Test
    fun `fillLine with no char fills with spaces`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Test")
        buffer.fillLine(0)

        assertEquals("          ", buffer.getScreenLine(0).getText())
    }

    @Test
    fun `fillLine throws on invalid row`() {
        val buffer = TerminalBuffer(10, 5, 100)
        assertThrows<IllegalArgumentException> {
            buffer.fillLine(-1, 'X')
        }
        assertThrows<IllegalArgumentException> {
            buffer.fillLine(5, 'X')
        }
    }

    // Line Operations
    @Test
    fun `scrollUp moves top line to scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.setCursor(0, 1)
        buffer.writeText("Line2")
        buffer.setCursor(0, 2)
        buffer.writeText("Line3")

        buffer.scrollUp()

        // Top line should be in scrollback
        assertEquals(1, buffer.scrollbackSize)
        // Screen should have Line2, Line3, and new empty line
        assertEquals("Line2     ", buffer.getScreenLine(0).getText())
        assertEquals("Line3     ", buffer.getScreenLine(1).getText())
        assertEquals("          ", buffer.getScreenLine(2).getText())
    }

    @Test
    fun `scrollUp adds new empty line at bottom`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Test")
        buffer.scrollUp()

        // Last line should be empty
        assertEquals("          ", buffer.getScreenLine(2).getText())
    }

    @Test
    fun `scrollUp trims scrollback when exceeding max`() {
        val buffer = TerminalBuffer(10, 2, 2)  // max scrollback = 2

        // Write and scroll 4 times
        for (i in 1..4) {
            buffer.setCursor(0, 0)
            buffer.writeText("Line$i")
            buffer.scrollUp()
        }

        // Should only keep last 2 in scrollback
        assertEquals(2, buffer.scrollbackSize)
    }

    @Test
    fun `clearScreen clears all lines`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.setCursor(0, 1)
        buffer.writeText("Line2")

        buffer.clearScreen()

        assertEquals("          ", buffer.getScreenLine(0).getText())
        assertEquals("          ", buffer.getScreenLine(1).getText())
        assertEquals("          ", buffer.getScreenLine(2).getText())
    }

    @Test
    fun `clearScreen resets cursor to origin`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.setCursor(5, 2)
        buffer.clearScreen()

        val cursor = buffer.getCursor()
        assertEquals(0, cursor.column)
        assertEquals(0, cursor.row)
    }

    @Test
    fun `clearScreen preserves scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.scrollUp()

        assertEquals(1, buffer.scrollbackSize)

        buffer.clearScreen()

        // Scrollback should still be there
        assertEquals(1, buffer.scrollbackSize)
    }

    @Test
    fun `clearAll clears screen and scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.scrollUp()
        buffer.writeText("Line2")

        assertEquals(1, buffer.scrollbackSize)

        buffer.clearAll()

        assertEquals("          ", buffer.getScreenLine(0).getText())
        assertEquals(0, buffer.scrollbackSize)
    }

    @Test
    fun `clearAll resets cursor to origin`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.setCursor(7, 2)
        buffer.clearAll()

        val cursor = buffer.getCursor()
        assertEquals(0, cursor.column)
        assertEquals(0, cursor.row)
    }
}
