package com.terminal.buffer

class TerminalBuffer(
    val width: Int, 
    val height: Int,
    val maxScrollBack: Int,
) {
    private val screen: ArrayDeque<Line> = ArrayDeque()
    private val scrollBack: ArrayDeque<Line> = ArrayDeque()
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

    fun setForeground(color: Color) {
        currentAttributes = currentAttributes.copy(foreground = color)
    }

    fun setBackground(color: Color) {
        currentAttributes = currentAttributes.copy(background = color)
    }

    fun setStyle(bold: Boolean?, italic: Boolean?, underline: Boolean?) {
        val currentStyles = currentAttributes.styleFlags
        currentAttributes = currentAttributes.copy(styleFlags = currentStyles.copy(
            bold = bold ?: currentStyles.bold,
            italic = italic ?: currentStyles.italic,
            underline = underline ?: currentStyles.underline,
        ))
    }

    fun resetAttributes() {
        currentAttributes = CellAttributes()
    }

    fun getCurrentAttributes(): CellAttributes {
        return currentAttributes
    }

    fun writeChar(char: Char) {
        screen[cursor.row].setCell(cursor.column, Cell(char, currentAttributes))

        if (cursor.column < width - 1) {
            moveCursorRight(1)
            return
        }

        newLine()
    }

    fun writeText(text: String) {
        text.forEach { char -> 
            when (char) {
                '\n' -> newLine()
                '\r' -> cursor.column = 0
                else -> writeChar(char)
            }
        }
    }

    fun fillLine(row: Int, char: Char) {
        require(row in 0 until height) { "Row $row out of bounds" }
        screen[row].fill(Cell(char, currentAttributes))
    }
    
    fun fillLine(row: Int) {
        require(row in 0 until height) { "Row $row out of bounds" }
        screen[row].fill(Cell.EMPTY.copy(attributes = currentAttributes))
    }

    fun newLine() {
        if (cursor.row == height - 1) { // last line
            scrollUp()
        }
        else {
            cursor.row++
        }
        cursor.column = 0
    }

    fun scrollUp() {
        scrollBack.add(screen.removeFirst())

        while (scrollBack.size > maxScrollBack) {
            scrollBack.removeFirst()
        }

        screen.add(Line(width))
    }
}
