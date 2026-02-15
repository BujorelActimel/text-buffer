package com.terminal.buffer

class Line(val width: Int) {
    private val cells: Array<Cell> = Array(width) { Cell.EMPTY }

    fun getCell(column: Int): Cell {
        require(column >= 0 && column < this.width) {
            "Position $column is out of the bounds of $this"
        }
        return cells[column]
    }

    fun setCell(column: Int, cell: Cell) {
        require(column >= 0 && column < this.width) {
            "Position $column is out of the bounds of $this"
        }
        cells[column] = cell
    }

    fun fill(cell: Cell) {
        cells.fill(cell)
    }

    fun clear() {
        fill(Cell.EMPTY)
    }

    fun getText(): String {
        return cells.map { it.char }.joinToString("")
    }

    override fun toString(): String {
        return "Line { width = $width, text = ${getText()} }"
    }
}
