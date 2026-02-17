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

    @Test
    fun `buffer starts with one line`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        assertEquals(1, buffer.lineCount)
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
        // Add more lines first so we can set cursor there
        buffer.newLine()
        buffer.newLine()
        buffer.newLine()
        buffer.newLine()
        buffer.newLine()

        buffer.setCursor(10, 5)
        val cursor = buffer.getCursor()
        assertEquals(10, cursor.column)
        assertEquals(5, cursor.row)
    }

    @Test
    fun `setCursor clamps negative column to zero`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(-10, 0)
        assertEquals(0, buffer.getCursor().column)
    }

    @Test
    fun `setCursor allows column beyond width`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(100, 0)
        assertEquals(100, buffer.getCursor().column)
    }

    @Test
    fun `setCursor clamps row to existing lines`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        // Buffer starts with 1 line, so max row is 0
        buffer.setCursor(10, 50)
        assertEquals(0, buffer.getCursor().row)

        buffer.setCursor(10, -5)
        assertEquals(0, buffer.getCursor().row)
    }

    @Test
    fun `setCursor clamps to last line when multiple lines exist`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.newLine()
        buffer.newLine()  // Now 3 lines exist (rows 0, 1, 2)

        buffer.setCursor(0, 10)
        assertEquals(2, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorUp moves cursor up`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        // Create lines first
        for (i in 0 until 10) buffer.newLine()
        buffer.setCursor(10, 10)
        buffer.moveCursorUp(3)
        assertEquals(7, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorUp stops at top`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        for (i in 0 until 5) buffer.newLine()
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
    fun `moveCursorDown moves cursor down within existing lines`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        for (i in 0 until 15) buffer.newLine()
        buffer.setCursor(10, 10)
        buffer.moveCursorDown(5)
        assertEquals(15, buffer.getCursor().row)
    }

    @Test
    fun `moveCursorDown stops at last line`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.newLine()
        buffer.newLine()  // 3 lines total (0, 1, 2)
        buffer.setCursor(10, 0)
        buffer.moveCursorDown(10)
        assertEquals(2, buffer.getCursor().row)
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
        buffer.setCursor(10, 0)
        buffer.moveCursorLeft(3)
        assertEquals(7, buffer.getCursor().column)
    }

    @Test
    fun `moveCursorLeft stops at left edge`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(5, 0)
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
        buffer.setCursor(10, 0)
        buffer.moveCursorRight(5)
        assertEquals(15, buffer.getCursor().column)
    }

    @Test
    fun `moveCursorRight allows moving beyond width`() {
        val buffer = TerminalBuffer(80, 24, 1000)
        buffer.setCursor(75, 0)
        buffer.moveCursorRight(10)
        assertEquals(85, buffer.getCursor().column)
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

        assertEquals('A', buffer.getScreenChar(0, 0))
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
    fun `writeChar extends line beyond width`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setCursor(9, 0)
        buffer.writeChar('X')

        // Cursor moves beyond width (no auto-wrap)
        val cursor = buffer.getCursor()
        assertEquals(10, cursor.column)
        assertEquals(0, cursor.row)
    }

    @Test
    fun `writeChar can write at any column`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setCursor(15, 0)
        buffer.writeChar('X')

        assertEquals(16, buffer.getCursor().column)
        // Line overflow captures content beyond width
        assertEquals("X", buffer.getLineOverflow(0).drop(5))  // at position 15
    }

    @Test
    fun `writeChar applies current attributes`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setForeground(Color.RED)
        buffer.setBackground(Color.BLUE)
        buffer.setStyle(bold = true, italic = false, underline = true)

        buffer.writeChar('A')

        assertEquals('A', buffer.getScreenChar(0, 0))
        val attrs = buffer.getScreenAttributes(0, 0)
        assertEquals(Color.RED, attrs.foreground)
        assertEquals(Color.BLUE, attrs.background)
        assertEquals(true, attrs.styleFlags.bold)
        assertEquals(true, attrs.styleFlags.underline)
    }

    @Test
    fun `writeText writes multiple characters`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello")

        assertEquals("Hello     ", buffer.getScreenLine(0))
        assertEquals(5, buffer.getCursor().column)
    }

    @Test
    fun `writeText handles newline`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("AB\nCD")

        assertEquals("AB        ", buffer.getScreenLine(0))
        assertEquals("CD        ", buffer.getScreenLine(1))
        assertEquals(2, buffer.getCursor().column)
        assertEquals(1, buffer.getCursor().row)
    }

    @Test
    fun `writeText handles carriage return`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABCD")
        buffer.writeText("\rXY")

        // \r moves to column 0, then XY overwrites AB
        assertEquals("XYCD      ", buffer.getScreenLine(0))
        assertEquals(2, buffer.getCursor().column)
    }

    @Test
    fun `writeText extends line beyond width`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("1234567890ABCD")  // 14 chars for width 10

        // All on same logical line, displayed with wrapping
        assertEquals("1234567890", buffer.getScreenLine(0))
        assertEquals("ABCD      ", buffer.getScreenLine(1))
        assertEquals(14, buffer.getCursor().column)
        assertEquals(0, buffer.getCursor().row)  // Still on logical row 0
    }

    @Test
    fun `fillLine fills line with character and current attributes`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setForeground(Color.GREEN)
        buffer.fillLine(0, '-')

        assertEquals("----------", buffer.getScreenLine(0))
        assertEquals(Color.GREEN, buffer.getScreenAttributes(0, 0).foreground)
    }

    @Test
    fun `fillLine with no char fills with spaces`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Test")
        buffer.fillLine(0)

        assertEquals("          ", buffer.getScreenLine(0))
    }

    @Test
    fun `fillLine throws on invalid row`() {
        val buffer = TerminalBuffer(10, 5, 100)
        assertThrows<IllegalArgumentException> {
            buffer.fillLine(-1, 'X')
        }
        assertThrows<IllegalArgumentException> {
            buffer.fillLine(1, 'X')  // Only row 0 exists initially
        }
    }

    // Line Operations
    @Test
    fun `newLine creates new logical line`() {
        val buffer = TerminalBuffer(10, 3, 100)
        assertEquals(1, buffer.lineCount)

        buffer.newLine()

        assertEquals(2, buffer.lineCount)
        assertEquals(0, buffer.getCursor().column)
        assertEquals(1, buffer.getCursor().row)
    }

    @Test
    fun `scrollUp moves top line to scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.newLine()
        buffer.writeText("Line2")
        buffer.newLine()
        buffer.writeText("Line3")

        buffer.scrollUp()

        assertEquals(1, buffer.scrollbackSize)
        assertEquals("Line1     ", buffer.getScrollbackLine(0))
        assertEquals("Line2     ", buffer.getScreenLine(0))
        assertEquals("Line3     ", buffer.getScreenLine(1))
    }

    @Test
    fun `scrollUp adjusts cursor row`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.newLine()
        buffer.newLine()
        buffer.setCursor(5, 2)

        buffer.scrollUp()

        assertEquals(1, buffer.getCursor().row)
        assertEquals(5, buffer.getCursor().column)
    }

    @Test
    fun `scrollUp trims scrollback when exceeding max`() {
        val buffer = TerminalBuffer(10, 2, 2)  // max scrollback = 2

        for (i in 1..4) {
            buffer.writeText("Line$i")
            buffer.scrollUp()
        }

        assertEquals(2, buffer.scrollbackSize)
    }

    @Test
    fun `clearScreen clears all lines`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.newLine()
        buffer.writeText("Line2")

        buffer.clearScreen()

        assertEquals(1, buffer.lineCount)
        assertEquals("          ", buffer.getScreenLine(0))
    }

    @Test
    fun `clearScreen resets cursor to origin`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.newLine()
        buffer.newLine()
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

        assertEquals("          ", buffer.getScreenLine(0))
        assertEquals(0, buffer.scrollbackSize)
    }

    @Test
    fun `clearAll resets cursor to origin`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.newLine()
        buffer.newLine()
        buffer.setCursor(7, 2)
        buffer.clearAll()

        val cursor = buffer.getCursor()
        assertEquals(0, cursor.column)
        assertEquals(0, cursor.row)
    }

    // Insert Text
    @Test
    fun `insertText inserts at cursor position`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABCDEF")
        buffer.setCursor(2, 0)

        buffer.insertText("XX")

        assertEquals("ABXXCDEF  ", buffer.getScreenLine(0))
    }

    @Test
    fun `insertText moves cursor after inserted text`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setCursor(2, 0)
        buffer.insertText("XXX")

        assertEquals(5, buffer.getCursor().column)
        assertEquals(0, buffer.getCursor().row)
    }

    @Test
    fun `insertText shifts existing content right`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello")
        buffer.setCursor(0, 0)

        buffer.insertText("Say ")

        assertEquals("Say Hello ", buffer.getScreenLine(0))
    }

    @Test
    fun `insertText preserves overflow content`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABCDEFGHIJ")  // fills line
        buffer.setCursor(2, 0)

        buffer.insertText("XX")

        // "IJ" pushed to overflow
        assertEquals("ABXXCDEFGH", buffer.getScreenLine(0))
        assertEquals("IJ", buffer.getLineOverflow(0))
    }

    @Test
    fun `insertText applies current attributes`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABC")
        buffer.setCursor(1, 0)
        buffer.setForeground(Color.RED)

        buffer.insertText("X")

        assertEquals('X', buffer.getScreenChar(1, 0))
        assertEquals(Color.RED, buffer.getScreenAttributes(1, 0).foreground)
    }

    @Test
    fun `insertText at end of line`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABC")
        buffer.setCursor(3, 0)

        buffer.insertText("XYZ")

        assertEquals("ABCXYZ    ", buffer.getScreenLine(0))
        assertEquals(6, buffer.getCursor().column)
    }

    @Test
    fun `insertText extends line beyond width`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("ABC")
        buffer.setCursor(8, 0)

        buffer.insertText("XXXXX")

        assertEquals("ABC     XX", buffer.getScreenLine(0))
        assertEquals("XXX       ", buffer.getScreenLine(1))
        assertEquals(13, buffer.getCursor().column)
    }

    @Test
    fun `insertText on empty line`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setCursor(3, 0)

        buffer.insertText("Hello")

        assertEquals("   Hello  ", buffer.getScreenLine(0))
    }

    // Content Access - Screen
    @Test
    fun `getScreenChar returns character at position`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello")

        assertEquals('H', buffer.getScreenChar(0, 0))
        assertEquals('e', buffer.getScreenChar(1, 0))
        assertEquals('l', buffer.getScreenChar(2, 0))
    }

    @Test
    fun `getScreenChar throws on invalid position`() {
        val buffer = TerminalBuffer(10, 5, 100)
        assertThrows<IllegalArgumentException> { buffer.getScreenChar(-1, 0) }
        assertThrows<IllegalArgumentException> { buffer.getScreenChar(10, 0) }
        assertThrows<IllegalArgumentException> { buffer.getScreenChar(0, -1) }
        assertThrows<IllegalArgumentException> { buffer.getScreenChar(0, 5) }
    }

    @Test
    fun `getScreenAttributes returns attributes at position`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.setForeground(Color.RED)
        buffer.writeText("Hi")

        assertEquals(Color.RED, buffer.getScreenAttributes(0, 0).foreground)
        assertEquals(Color.RED, buffer.getScreenAttributes(1, 0).foreground)
    }

    @Test
    fun `getScreenLine returns line content`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello")

        assertEquals("Hello     ", buffer.getScreenLine(0))
    }

    @Test
    fun `getScreenLine throws on invalid row`() {
        val buffer = TerminalBuffer(10, 5, 100)
        assertThrows<IllegalArgumentException> { buffer.getScreenLine(-1) }
        assertThrows<IllegalArgumentException> { buffer.getScreenLine(5) }
    }

    @Test
    fun `getLineOverflow returns content beyond width`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("1234567890ABCDE")

        assertEquals("ABCDE", buffer.getLineOverflow(0))
    }

    @Test
    fun `getLineOverflow returns empty for short lines`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello")

        assertEquals("", buffer.getLineOverflow(0))
    }

    // Content Access - Scrollback
    @Test
    fun `getScrollbackChar returns character from scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("Line1")
        buffer.scrollUp()

        assertEquals('L', buffer.getScrollbackChar(0, 0))
        assertEquals('i', buffer.getScrollbackChar(1, 0))
    }

    @Test
    fun `getScrollbackChar throws on empty scrollback`() {
        val buffer = TerminalBuffer(10, 5, 100)
        assertThrows<IllegalArgumentException> { buffer.getScrollbackChar(0, 0) }
    }

    @Test
    fun `getScrollbackAttributes returns attributes from scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.setForeground(Color.BLUE)
        buffer.writeText("Test")
        buffer.scrollUp()

        assertEquals(Color.BLUE, buffer.getScrollbackAttributes(0, 0).foreground)
    }

    @Test
    fun `getScrollbackLine returns line from scrollback`() {
        val buffer = TerminalBuffer(10, 3, 100)
        buffer.writeText("OldLine")
        buffer.scrollUp()

        assertEquals("OldLine   ", buffer.getScrollbackLine(0))
    }

    // Content as string
    @Test
    fun `getScreenContent returns all screen lines`() {
        val buffer = TerminalBuffer(5, 3, 100)
        buffer.writeText("AB")
        buffer.newLine()
        buffer.writeText("CD")

        val content = buffer.getScreenContent()
        assertEquals("AB   \nCD   \n     ", content)
    }

    @Test
    fun `getFullContent returns scrollback and screen`() {
        val buffer = TerminalBuffer(5, 2, 100)
        buffer.writeText("Old")
        buffer.scrollUp()
        buffer.setCursor(0, 0)
        buffer.writeText("New")

        val content = buffer.getFullContent()
        // Scrollback (1 line) + screen (2 display rows, but only 1 has content)
        assertEquals("Old  \nNew  \n     ", content)
    }

    @Test
    fun `getFullContent with empty scrollback returns screen only`() {
        val buffer = TerminalBuffer(5, 2, 100)
        buffer.writeText("Test")

        val content = buffer.getFullContent()
        assertEquals("Test \n     ", content)
    }

    // Resize
    @Test
    fun `resize changes dimensions`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.resize(20, 10)

        assertEquals(20, buffer.width)
        assertEquals(10, buffer.height)
    }

    @Test
    fun `resize preserves content`() {
        val buffer = TerminalBuffer(10, 5, 100)
        buffer.writeText("Hello World!")

        buffer.resize(5, 5)

        // Content is preserved, just displayed differently
        assertEquals("Hello", buffer.getScreenLine(0))
        assertEquals(" Worl", buffer.getScreenLine(1))
        assertEquals("d!   ", buffer.getScreenLine(2))

        buffer.resize(12, 5)
        assertEquals("Hello World!", buffer.getScreenLine(0))
    }

    @Test
    fun `resize throws on invalid dimensions`() {
        val buffer = TerminalBuffer(10, 5, 100)
        assertThrows<IllegalArgumentException> { buffer.resize(0, 5) }
        assertThrows<IllegalArgumentException> { buffer.resize(10, 0) }
    }
}
