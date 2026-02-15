package com.terminal.buffer

class TerminalBuffer(
    val width: Int, 
    val height: Int,
    val maxScrollBack: Int,
) {
    private val screen: MutableList<Line> = mutableListOf()
    private val scrollBack: MutableList<Line> = mutableListOf()
    private val cursor: Cursor = Cursor(0, 0)
    private var currentAttributes = CellAttributes()
    val scrollbackSize: Int
        get() = scrollBack.size

    init {
        repeat(height) {
            screen.add(Line(width))
        }
    }

    fun getCursor(): Cursor {
        return cursor.copy()
    }

    fun setCursor(column: Int, row: Int) {
        cursor.column = column.coerceIn(0, width-1)
        cursor.row = row.coerceIn(0, height-1)
    }

    fun moveCursorUp(n: Int) {
        require(n >= 0) {
            "Use moveCursorDown instead of negative value"
        }
        cursor.row = (cursor.row - n).coerceIn(0, height-1)
    }

    fun moveCursorDown(n: Int) {
        require(n >= 0) {
            "Use moveCursorUp instead of negative value"
        }
        cursor.row = (cursor.row + n).coerceIn(0, height-1)
    }

    fun moveCursorLeft(n: Int) {
        require(n >= 0) {
            "Use moveCursorRight instead of negative value"
        }
        cursor.column = (cursor.column - n).coerceIn(0, width-1)
    }  
    
    fun moveCursorRight(n: Int) {
        require(n >= 0) {
            "Use moveCursorLeft instead of negative value"
        }
        cursor.column = (cursor.column + n).coerceIn(0, width-1)
    }
}
