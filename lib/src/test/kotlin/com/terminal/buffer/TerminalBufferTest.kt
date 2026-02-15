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
}
