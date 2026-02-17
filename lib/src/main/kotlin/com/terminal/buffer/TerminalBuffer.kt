package com.terminal.buffer

class TerminalBuffer(
    initialWidth: Int,
    initialHeight: Int,
    val maxScrollBack: Int,
) {
    var width: Int = initialWidth
        private set
    var height: Int = initialHeight
        private set

    private val lines = mutableListOf<Line>()
    private val scrollback = mutableListOf<Line>()
    private val cursor = Cursor(0, 0)
    private var currentAttributes = CellAttributes()

    val scrollbackSize: Int get() = scrollback.size
    val lineCount: Int get() = lines.size

    init {
        lines.add(Line())
    }

    fun getCursor(): Cursor = cursor.copy()

    fun setCursor(column: Int, row: Int) {
        cursor.column = column.coerceAtLeast(0)
        cursor.row = row.coerceIn(0, lines.lastIndex.coerceAtLeast(0))
    }

    fun moveCursorUp(n: Int) {
        require(n >= 0) { "Use moveCursorDown instead of negative value" }
        cursor.row = (cursor.row - n).coerceAtLeast(0)
    }

    fun moveCursorDown(n: Int) {
        require(n >= 0) { "Use moveCursorUp instead of negative value" }
        cursor.row = (cursor.row + n).coerceAtMost(lines.lastIndex.coerceAtLeast(0))
    }

    fun moveCursorLeft(n: Int) {
        require(n >= 0) { "Use moveCursorRight instead of negative value" }
        cursor.column = (cursor.column - n).coerceAtLeast(0)
    }

    fun moveCursorRight(n: Int) {
        require(n >= 0) { "Use moveCursorLeft instead of negative value" }
        cursor.column = cursor.column + n
    }

    fun setForeground(color: Color) {
        currentAttributes = currentAttributes.copy(foreground = color)
    }

    fun setBackground(color: Color) {
        currentAttributes = currentAttributes.copy(background = color)
    }

    fun setStyle(bold: Boolean?, italic: Boolean?, underline: Boolean?) {
        val currentStyles = currentAttributes.styleFlags
        currentAttributes = currentAttributes.copy(
            styleFlags = currentStyles.copy(
                bold = bold ?: currentStyles.bold,
                italic = italic ?: currentStyles.italic,
                underline = underline ?: currentStyles.underline,
            )
        )
    }

    fun resetAttributes() {
        currentAttributes = CellAttributes()
    }

    fun getCurrentAttributes(): CellAttributes = currentAttributes

    fun writeChar(char: Char) {
        ensureLineExists(cursor.row)
        lines[cursor.row].setCell(cursor.column, Cell(char, currentAttributes))
        cursor.column++
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

    fun newLine() {
        cursor.row++
        cursor.column = 0
        ensureLineExists(cursor.row)
        ensureCursorOnScreen()
    }

    fun fillLine(row: Int, char: Char) {
        require(row in 0 until lines.size) { "Row $row out of bounds" }
        lines[row].fill(Cell(char, currentAttributes), width)
    }

    fun fillLine(row: Int) {
        require(row in 0 until lines.size) { "Row $row out of bounds" }
        lines[row].fill(Cell.EMPTY.copy(attributes = currentAttributes), width)
    }

    fun insertText(text: String) {
        ensureLineExists(cursor.row)
        val line = lines[cursor.row]

        val existingLength = line.length
        for (i in (existingLength - 1) downTo cursor.column) {
            line.setCell(i + text.length, line.getCell(i))
        }

        for ((i, char) in text.withIndex()) {
            line.setCell(cursor.column + i, Cell(char, currentAttributes))
        }

        cursor.column += text.length
    }

    fun scrollUp() {
        if (lines.isEmpty()) return

        scrollback.add(lines.removeAt(0))

        while (scrollback.size > maxScrollBack) {
            scrollback.removeAt(0)
        }

        if (cursor.row > 0) cursor.row--
    }

    private fun ensureLineExists(row: Int) {
        while (lines.size <= row) {
            lines.add(Line())
        }
    }

    private fun ensureCursorOnScreen() {
        val displayRow = getDisplayRow(cursor.row, cursor.column)
        while (displayRow >= height) {
            scrollUp()
        }
    }

    private fun getDisplayRowCount(line: Line): Int {
        return maxOf(1, (line.length + width - 1) / width)
    }

    private fun getDisplayRow(logicalRow: Int, column: Int): Int {
        var displayRow = 0
        for (i in 0 until minOf(logicalRow, lines.size)) {
            displayRow += getDisplayRowCount(lines[i])
        }
        displayRow += column / width
        return displayRow
    }

    // Convert display row to (logical line index, column offset)
    // Returns (-1, 0) if display row is beyond content
    private fun displayToLogical(displayRow: Int): Pair<Int, Int> {
        var row = 0
        for ((index, line) in lines.withIndex()) {
            val rows = getDisplayRowCount(line)
            if (displayRow < row + rows) {
                val offset = (displayRow - row) * width
                return Pair(index, offset)
            }
            row += rows
        }
        return Pair(-1, 0)  // Beyond content
    }

    fun clearScreen() {
        lines.clear()
        lines.add(Line())
        cursor.column = 0
        cursor.row = 0
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    fun getScreenChar(column: Int, row: Int): Char {
        require(column in 0 until width) { "Column $column out of bounds" }
        require(row in 0 until height) { "Row $row out of bounds" }

        val (lineIndex, offset) = displayToLogical(row)
        if (lineIndex < 0 || lineIndex >= lines.size) return ' '
        return lines[lineIndex].getCell(offset + column).char
    }

    fun getScreenAttributes(column: Int, row: Int): CellAttributes {
        require(column in 0 until width) { "Column $column out of bounds" }
        require(row in 0 until height) { "Row $row out of bounds" }

        val (lineIndex, offset) = displayToLogical(row)
        if (lineIndex < 0 || lineIndex >= lines.size) return CellAttributes()
        return lines[lineIndex].getCell(offset + column).attributes
    }

    fun getScreenLine(row: Int): String {
        require(row in 0 until height) { "Row $row out of bounds" }

        val (lineIndex, offset) = displayToLogical(row)
        if (lineIndex < 0 || lineIndex >= lines.size) return " ".repeat(width)
        return lines[lineIndex].getTextPadded(offset + width).drop(offset).take(width).padEnd(width)
    }

    fun getLineOverflow(logicalRow: Int): String {
        require(logicalRow in 0 until lines.size) { "Row $logicalRow out of bounds" }
        val line = lines[logicalRow]
        if (line.length <= width) return ""
        return (width until line.length).map { line.getCell(it).char }.joinToString("")
    }

    fun getScrollbackChar(column: Int, row: Int): Char {
        require(column in 0 until width) { "Column $column out of bounds" }
        require(row in 0 until scrollbackSize) { "Row $row out of bounds" }

        return scrollback[row].getCell(column).char
    }

    fun getScrollbackAttributes(column: Int, row: Int): CellAttributes {
        require(column in 0 until width) { "Column $column out of bounds" }
        require(row in 0 until scrollbackSize) { "Row $row out of bounds" }

        return scrollback[row].getCell(column).attributes
    }

    fun getScrollbackLine(row: Int): String {
        require(row in 0 until scrollbackSize) { "Row $row out of bounds" }
        return scrollback[row].getTextPadded(width)
    }

    fun getScreenContent(): String {
        return (0 until height).map { getScreenLine(it) }.joinToString("\n")
    }

    fun getFullContent(): String {
        if (scrollback.isEmpty()) return getScreenContent()
        val scrollbackContent = scrollback.joinToString("\n") { it.getTextPadded(width) }
        return scrollbackContent + "\n" + getScreenContent()
    }

    fun resize(newWidth: Int, newHeight: Int) {
        require(newWidth > 0) { "Width must be positive" }
        require(newHeight > 0) { "Height must be positive" }

        width = newWidth
        height = newHeight

        if (lines.isEmpty()) {
            lines.add(Line())
        }
        
        ensureCursorOnScreen()
    }
}
