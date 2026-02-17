package com.terminal.buffer

class Line {
    private val cells = mutableListOf<Cell>()

    val length: Int get() = cells.size

    fun getCell(column: Int): Cell = cells.getOrElse(column) { Cell.EMPTY }

    fun setCell(column: Int, cell: Cell) {
        while (cells.size <= column) cells.add(Cell.EMPTY)
        cells[column] = cell
    }

    fun getText(): String = cells.joinToString("") { it.char.toString() }

    fun getTextPadded(width: Int): String {
        return (0 until width).map { getCell(it).char }.joinToString("")
    }

    fun clear() = cells.clear()

    fun fill(cell: Cell, width: Int) {
        cells.clear()
        repeat(width) { cells.add(cell) }
    }
}
