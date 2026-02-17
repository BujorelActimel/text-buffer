package com.terminal.demo.ui

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.terminal.buffer.Color
import com.terminal.buffer.TerminalBuffer

class TerminalRenderer(
    private val buffer: TerminalBuffer,
    private val offsetX: Int,
    private val offsetY: Int
) {

    fun render(graphics: TextGraphics, showCursor: Boolean = true) {
        for (row in 0 until buffer.height) {
            for (col in 0 until buffer.width) {
                val char = buffer.getScreenChar(col, row)
                val attrs = buffer.getScreenAttributes(col, row)

                graphics.setForegroundColor(mapColor(attrs.foreground, false))
                graphics.setBackgroundColor(mapColor(attrs.background, true))

                val sgrs = mutableSetOf<SGR>()
                if (attrs.styleFlags.bold) sgrs.add(SGR.BOLD)
                if (attrs.styleFlags.italic) sgrs.add(SGR.ITALIC)
                if (attrs.styleFlags.underline) sgrs.add(SGR.UNDERLINE)

                if (sgrs.isNotEmpty()) {
                    graphics.enableModifiers(*sgrs.toTypedArray())
                }

                graphics.setCharacter(offsetX + col, offsetY + row, char)

                if (sgrs.isNotEmpty()) {
                    graphics.disableModifiers(*sgrs.toTypedArray())
                }
            }
        }

        // Draw cursor
        if (showCursor) {
            val cursor = buffer.getCursor()
            if (cursor.row < buffer.height && cursor.column < buffer.width) {
                graphics.setBackgroundColor(TextColor.ANSI.WHITE)
                graphics.setForegroundColor(TextColor.ANSI.BLACK)
                val cursorChar = buffer.getScreenChar(cursor.column, cursor.row)
                graphics.setCharacter(offsetX + cursor.column, offsetY + cursor.row, cursorChar)
                graphics.setBackgroundColor(TextColor.ANSI.DEFAULT)
                graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
            }
        }
    }

    private fun mapColor(color: Color, isBackground: Boolean): TextColor {
        return when (color) {
            Color.BLACK -> TextColor.ANSI.BLACK
            Color.RED -> TextColor.ANSI.RED
            Color.GREEN -> TextColor.ANSI.GREEN
            Color.YELLOW -> TextColor.ANSI.YELLOW
            Color.BLUE -> TextColor.ANSI.BLUE
            Color.MAGENTA -> TextColor.ANSI.MAGENTA
            Color.CYAN -> TextColor.ANSI.CYAN
            Color.WHITE -> TextColor.ANSI.WHITE
            Color.BRIGHT_BLACK -> TextColor.ANSI.BLACK_BRIGHT
            Color.BRIGHT_RED -> TextColor.ANSI.RED_BRIGHT
            Color.BRIGHT_GREEN -> TextColor.ANSI.GREEN_BRIGHT
            Color.BRIGHT_YELLOW -> TextColor.ANSI.YELLOW_BRIGHT
            Color.BRIGHT_BLUE -> TextColor.ANSI.BLUE_BRIGHT
            Color.BRIGHT_MAGENTA -> TextColor.ANSI.MAGENTA_BRIGHT
            Color.BRIGHT_CYAN -> TextColor.ANSI.CYAN_BRIGHT
            Color.BRIGHT_WHITE -> TextColor.ANSI.WHITE_BRIGHT
            Color.DEFAULT -> if (isBackground) TextColor.ANSI.DEFAULT else TextColor.ANSI.DEFAULT
        }
    }
}
