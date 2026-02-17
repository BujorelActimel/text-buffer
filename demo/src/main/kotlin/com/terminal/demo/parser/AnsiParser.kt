package com.terminal.demo.parser

sealed class AnsiToken {
    data class Text(val content: String) : AnsiToken()
    data class SGR(val codes: List<Int>) : AnsiToken()
    data class CursorPosition(val row: Int, val col: Int) : AnsiToken()
    data class CursorMove(val direction: Char, val count: Int) : AnsiToken()
    data class EraseDisplay(val mode: Int) : AnsiToken()
    data class EraseLine(val mode: Int) : AnsiToken()
    object CursorHome : AnsiToken()
    object CarriageReturn : AnsiToken()
    object LineFeed : AnsiToken()
    object Backspace : AnsiToken()
    object Bell : AnsiToken()
    object Tab : AnsiToken()
    data class Unknown(val sequence: String) : AnsiToken()
}

object AnsiParser {
    private enum class State {
        NORMAL,
        ESCAPE,
        CSI,
        OSC
    }

    fun parse(input: String): List<AnsiToken> {
        val tokens = mutableListOf<AnsiToken>()
        val textBuffer = StringBuilder()
        val paramBuffer = StringBuilder()
        var state = State.NORMAL

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                tokens.add(AnsiToken.Text(textBuffer.toString()))
                textBuffer.clear()
            }
        }

        var i = 0
        while (i < input.length) {
            val c = input[i]

            when (state) {
                State.NORMAL -> {
                    when (c) {
                        '\u001b' -> {
                            flushText()
                            state = State.ESCAPE
                        }
                        '\r' -> {
                            flushText()
                            tokens.add(AnsiToken.CarriageReturn)
                        }
                        '\n' -> {
                            flushText()
                            tokens.add(AnsiToken.LineFeed)
                        }
                        '\b' -> {
                            flushText()
                            tokens.add(AnsiToken.Backspace)
                        }
                        '\u0007' -> {
                            flushText()
                            tokens.add(AnsiToken.Bell)
                        }
                        '\t' -> {
                            flushText()
                            tokens.add(AnsiToken.Tab)
                        }
                        else -> textBuffer.append(c)
                    }
                }

                State.ESCAPE -> {
                    when (c) {
                        '[' -> {
                            state = State.CSI
                            paramBuffer.clear()
                        }
                        ']' -> {
                            state = State.OSC
                            paramBuffer.clear()
                        }
                        '(' -> {
                            // Character set designation - skip next char
                            i++
                            state = State.NORMAL
                        }
                        ')' -> {
                            // Character set designation - skip next char
                            i++
                            state = State.NORMAL
                        }
                        '=' -> {
                            // Application keypad mode - ignore
                            state = State.NORMAL
                        }
                        '>' -> {
                            // Normal keypad mode - ignore
                            state = State.NORMAL
                        }
                        'M' -> {
                            // Reverse index - move cursor up
                            tokens.add(AnsiToken.CursorMove('A', 1))
                            state = State.NORMAL
                        }
                        'c' -> {
                            // Reset - treat as clear
                            tokens.add(AnsiToken.EraseDisplay(2))
                            tokens.add(AnsiToken.SGR(listOf(0)))
                            state = State.NORMAL
                        }
                        else -> {
                            // Unknown escape sequence
                            tokens.add(AnsiToken.Unknown("ESC$c"))
                            state = State.NORMAL
                        }
                    }
                }

                State.CSI -> {
                    when {
                        c.isDigit() || c == ';' || c == '?' -> paramBuffer.append(c)
                        c in 'A'..'Z' || c in 'a'..'z' || c == '@' || c == '`' -> {
                            tokens.add(parseCsiSequence(c, paramBuffer.toString()))
                            state = State.NORMAL
                        }
                        else -> {
                            tokens.add(AnsiToken.Unknown("CSI${paramBuffer}$c"))
                            state = State.NORMAL
                        }
                    }
                }

                State.OSC -> {
                    // OSC sequences end with BEL or ST (ESC \)
                    when {
                        c == '\u0007' -> {
                            // BEL terminates OSC - ignore OSC content
                            state = State.NORMAL
                        }
                        c == '\u001b' && i + 1 < input.length && input[i + 1] == '\\' -> {
                            // ST (String Terminator) terminates OSC
                            i++ // Skip the backslash
                            state = State.NORMAL
                        }
                        else -> paramBuffer.append(c)
                    }
                }
            }
            i++
        }

        flushText()
        return tokens
    }

    private fun parseCsiSequence(command: Char, params: String): AnsiToken {
        val cleanParams = params.removePrefix("?")
        val parts = cleanParams.split(';').map { it.toIntOrNull() ?: 0 }

        return when (command) {
            'm' -> AnsiToken.SGR(if (cleanParams.isEmpty()) listOf(0) else parts)
            'H', 'f' -> {
                if (cleanParams.isEmpty()) {
                    AnsiToken.CursorHome
                } else {
                    val row = parts.getOrElse(0) { 1 }
                    val col = parts.getOrElse(1) { 1 }
                    AnsiToken.CursorPosition(row, col)
                }
            }
            'A' -> AnsiToken.CursorMove('A', parts.getOrElse(0) { 1 }.coerceAtLeast(1))
            'B' -> AnsiToken.CursorMove('B', parts.getOrElse(0) { 1 }.coerceAtLeast(1))
            'C' -> AnsiToken.CursorMove('C', parts.getOrElse(0) { 1 }.coerceAtLeast(1))
            'D' -> AnsiToken.CursorMove('D', parts.getOrElse(0) { 1 }.coerceAtLeast(1))
            'E' -> AnsiToken.CursorMove('E', parts.getOrElse(0) { 1 }.coerceAtLeast(1))
            'F' -> AnsiToken.CursorMove('F', parts.getOrElse(0) { 1 }.coerceAtLeast(1))
            'G' -> AnsiToken.CursorPosition(0, parts.getOrElse(0) { 1 })
            'd' -> AnsiToken.CursorPosition(parts.getOrElse(0) { 1 }, 0)
            'J' -> AnsiToken.EraseDisplay(parts.getOrElse(0) { 0 })
            'K' -> AnsiToken.EraseLine(parts.getOrElse(0) { 0 })
            'h', 'l' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            'r' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            's' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            'u' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            'n' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            'X' -> {
                AnsiToken.EraseLine(0)
            }
            'P' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            '@' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            'L' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            'M' -> {
                AnsiToken.Unknown("CSI$params$command")
            }
            else -> AnsiToken.Unknown("CSI$params$command")
        }
    }
}
