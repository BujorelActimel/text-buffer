package com.terminal.demo.parser

import com.terminal.buffer.Color
import com.terminal.buffer.TerminalBuffer

class AnsiCodeHandler(private val buffer: TerminalBuffer) {

    fun apply(tokens: List<AnsiToken>) {
        for (token in tokens) {
            apply(token)
        }
    }

    fun apply(token: AnsiToken) {
        when (token) {
            is AnsiToken.Text -> buffer.writeText(token.content)

            is AnsiToken.CarriageReturn -> {
                val cursor = buffer.getCursor()
                buffer.setCursor(0, cursor.row)
            }

            is AnsiToken.LineFeed -> buffer.newLine()

            is AnsiToken.Backspace -> {
                val cursor = buffer.getCursor()
                if (cursor.column > 0) {
                    buffer.setCursor(cursor.column - 1, cursor.row)
                }
            }

            is AnsiToken.Tab -> {
                val cursor = buffer.getCursor()
                val nextTab = ((cursor.column / 8) + 1) * 8
                buffer.setCursor(nextTab.coerceAtMost(buffer.width - 1), cursor.row)
            }

            is AnsiToken.Bell -> {
                // Bell - ignore in visual playback
            }

            is AnsiToken.SGR -> applySGR(token.codes)

            AnsiToken.CursorHome -> buffer.setCursor(0, 0)

            is AnsiToken.CursorPosition -> {
                // ANSI uses 1-based indexing
                val row = (token.row - 1).coerceAtLeast(0)
                val col = (token.col - 1).coerceAtLeast(0)
                buffer.setCursor(col, row)
            }

            is AnsiToken.CursorMove -> {
                when (token.direction) {
                    'A' -> buffer.moveCursorUp(token.count)
                    'B' -> buffer.moveCursorDown(token.count)
                    'C' -> buffer.moveCursorRight(token.count)
                    'D' -> buffer.moveCursorLeft(token.count)
                    'E' -> {
                        // Cursor next line
                        buffer.moveCursorDown(token.count)
                        buffer.setCursor(0, buffer.getCursor().row)
                    }
                    'F' -> {
                        // Cursor previous line
                        buffer.moveCursorUp(token.count)
                        buffer.setCursor(0, buffer.getCursor().row)
                    }
                }
            }

            is AnsiToken.EraseDisplay -> {
                when (token.mode) {
                    0 -> clearFromCursorToEnd()
                    1 -> clearFromStartToCursor()
                    2, 3 -> buffer.clearScreen()
                }
            }

            is AnsiToken.EraseLine -> {
                when (token.mode) {
                    0 -> clearLineFromCursor()
                    1 -> clearLineToCursor()
                    2 -> clearEntireLine()
                }
            }

            is AnsiToken.Unknown -> {
                // Ignore unknown sequences
            }
        }
    }

    private fun applySGR(codes: List<Int>) {
        var i = 0
        while (i < codes.size) {
            when (val code = codes[i]) {
                0 -> buffer.resetAttributes()

                // Bold, italic, underline
                1 -> buffer.setStyle(bold = true, italic = null, underline = null)
                3 -> buffer.setStyle(bold = null, italic = true, underline = null)
                4 -> buffer.setStyle(bold = null, italic = null, underline = true)

                // Reset styles
                21, 22 -> buffer.setStyle(bold = false, italic = null, underline = null)
                23 -> buffer.setStyle(bold = null, italic = false, underline = null)
                24 -> buffer.setStyle(bold = null, italic = null, underline = false)

                // Standard foreground colors (30-37)
                30 -> buffer.setForeground(Color.BLACK)
                31 -> buffer.setForeground(Color.RED)
                32 -> buffer.setForeground(Color.GREEN)
                33 -> buffer.setForeground(Color.YELLOW)
                34 -> buffer.setForeground(Color.BLUE)
                35 -> buffer.setForeground(Color.MAGENTA)
                36 -> buffer.setForeground(Color.CYAN)
                37 -> buffer.setForeground(Color.WHITE)
                39 -> buffer.setForeground(Color.DEFAULT)

                // Standard background colors (40-47)
                40 -> buffer.setBackground(Color.BLACK)
                41 -> buffer.setBackground(Color.RED)
                42 -> buffer.setBackground(Color.GREEN)
                43 -> buffer.setBackground(Color.YELLOW)
                44 -> buffer.setBackground(Color.BLUE)
                45 -> buffer.setBackground(Color.MAGENTA)
                46 -> buffer.setBackground(Color.CYAN)
                47 -> buffer.setBackground(Color.WHITE)
                49 -> buffer.setBackground(Color.DEFAULT)

                // Bright foreground colors (90-97)
                90 -> buffer.setForeground(Color.BRIGHT_BLACK)
                91 -> buffer.setForeground(Color.BRIGHT_RED)
                92 -> buffer.setForeground(Color.BRIGHT_GREEN)
                93 -> buffer.setForeground(Color.BRIGHT_YELLOW)
                94 -> buffer.setForeground(Color.BRIGHT_BLUE)
                95 -> buffer.setForeground(Color.BRIGHT_MAGENTA)
                96 -> buffer.setForeground(Color.BRIGHT_CYAN)
                97 -> buffer.setForeground(Color.BRIGHT_WHITE)

                // Bright background colors (100-107)
                100 -> buffer.setBackground(Color.BRIGHT_BLACK)
                101 -> buffer.setBackground(Color.BRIGHT_RED)
                102 -> buffer.setBackground(Color.BRIGHT_GREEN)
                103 -> buffer.setBackground(Color.BRIGHT_YELLOW)
                104 -> buffer.setBackground(Color.BRIGHT_BLUE)
                105 -> buffer.setBackground(Color.BRIGHT_MAGENTA)
                106 -> buffer.setBackground(Color.BRIGHT_CYAN)
                107 -> buffer.setBackground(Color.BRIGHT_WHITE)

                // 256-color and true-color support
                38 -> {
                    // Extended foreground color
                    if (i + 1 < codes.size && codes[i + 1] == 5 && i + 2 < codes.size) {
                        // 256-color mode: ESC[38;5;{n}m
                        val colorIndex = codes[i + 2]
                        buffer.setForeground(index256ToColor(colorIndex))
                        i += 2
                    } else if (i + 1 < codes.size && codes[i + 1] == 2 && i + 4 < codes.size) {
                        // True color mode: ESC[38;2;{r};{g};{b}m - approximate to 16 colors
                        val r = codes[i + 2]
                        val g = codes[i + 3]
                        val b = codes[i + 4]
                        buffer.setForeground(rgbToColor(r, g, b))
                        i += 4
                    }
                }

                48 -> {
                    // Extended background color
                    if (i + 1 < codes.size && codes[i + 1] == 5 && i + 2 < codes.size) {
                        // 256-color mode: ESC[48;5;{n}m
                        val colorIndex = codes[i + 2]
                        buffer.setBackground(index256ToColor(colorIndex))
                        i += 2
                    } else if (i + 1 < codes.size && codes[i + 1] == 2 && i + 4 < codes.size) {
                        // True color mode: ESC[48;2;{r};{g};{b}m
                        val r = codes[i + 2]
                        val g = codes[i + 3]
                        val b = codes[i + 4]
                        buffer.setBackground(rgbToColor(r, g, b))
                        i += 4
                    }
                }
            }
            i++
        }
    }

    private fun index256ToColor(index: Int): Color {
        return when (index) {
            0 -> Color.BLACK
            1 -> Color.RED
            2 -> Color.GREEN
            3 -> Color.YELLOW
            4 -> Color.BLUE
            5 -> Color.MAGENTA
            6 -> Color.CYAN
            7 -> Color.WHITE
            8 -> Color.BRIGHT_BLACK
            9 -> Color.BRIGHT_RED
            10 -> Color.BRIGHT_GREEN
            11 -> Color.BRIGHT_YELLOW
            12 -> Color.BRIGHT_BLUE
            13 -> Color.BRIGHT_MAGENTA
            14 -> Color.BRIGHT_CYAN
            15 -> Color.BRIGHT_WHITE
            in 16..231 -> {
                // 6x6x6 color cube - approximate to nearest basic color
                val adjusted = index - 16
                val r = adjusted / 36
                val g = (adjusted % 36) / 6
                val b = adjusted % 6
                approximateRGBtoBasic(r * 51, g * 51, b * 51)
            }
            in 232..255 -> {
                // Grayscale - map to white/black/bright_black
                val gray = (index - 232) * 10 + 8
                when {
                    gray < 64 -> Color.BLACK
                    gray < 128 -> Color.BRIGHT_BLACK
                    gray < 192 -> Color.WHITE
                    else -> Color.BRIGHT_WHITE
                }
            }
            else -> Color.DEFAULT
        }
    }

    private fun rgbToColor(r: Int, g: Int, b: Int): Color {
        return approximateRGBtoBasic(r, g, b)
    }

    private fun approximateRGBtoBasic(r: Int, g: Int, b: Int): Color {
        // Simple approximation: check if each channel is "on" (> 127)
        val hasRed = r > 127
        val hasGreen = g > 127
        val hasBlue = b > 127
        val bright = r > 200 || g > 200 || b > 200

        return when {
            !hasRed && !hasGreen && !hasBlue -> if (bright) Color.BRIGHT_BLACK else Color.BLACK
            hasRed && !hasGreen && !hasBlue -> if (bright) Color.BRIGHT_RED else Color.RED
            !hasRed && hasGreen && !hasBlue -> if (bright) Color.BRIGHT_GREEN else Color.GREEN
            hasRed && hasGreen && !hasBlue -> if (bright) Color.BRIGHT_YELLOW else Color.YELLOW
            !hasRed && !hasGreen && hasBlue -> if (bright) Color.BRIGHT_BLUE else Color.BLUE
            hasRed && !hasGreen && hasBlue -> if (bright) Color.BRIGHT_MAGENTA else Color.MAGENTA
            !hasRed && hasGreen && hasBlue -> if (bright) Color.BRIGHT_CYAN else Color.CYAN
            else -> if (bright) Color.BRIGHT_WHITE else Color.WHITE
        }
    }

    private fun clearFromCursorToEnd() {
        val cursor = buffer.getCursor()
        // Clear rest of current line
        clearLineFromCursor()
        // Clear all lines below
        for (row in (cursor.row + 1) until buffer.height) {
            buffer.setCursor(0, row)
            clearEntireLine()
        }
        buffer.setCursor(cursor.column, cursor.row)
    }

    private fun clearFromStartToCursor() {
        val cursor = buffer.getCursor()
        // Clear lines above
        for (row in 0 until cursor.row) {
            buffer.setCursor(0, row)
            clearEntireLine()
        }
        // Clear current line up to cursor
        clearLineToCursor()
        buffer.setCursor(cursor.column, cursor.row)
    }

    private fun clearLineFromCursor() {
        val cursor = buffer.getCursor()
        for (col in cursor.column until buffer.width) {
            buffer.writeChar(' ')
        }
        buffer.setCursor(cursor.column, cursor.row)
    }

    private fun clearLineToCursor() {
        val cursor = buffer.getCursor()
        buffer.setCursor(0, cursor.row)
        for (col in 0..cursor.column) {
            buffer.writeChar(' ')
        }
        buffer.setCursor(cursor.column, cursor.row)
    }

    private fun clearEntireLine() {
        val cursor = buffer.getCursor()
        buffer.setCursor(0, cursor.row)
        for (col in 0 until buffer.width) {
            buffer.writeChar(' ')
        }
        buffer.setCursor(cursor.column, cursor.row)
    }
}
