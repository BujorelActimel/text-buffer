package com.terminal.buffer

data class Cell(
    val char: Char,
    val attributes: CellAttributes,   
) {
    companion object {
        val EMPTY = Cell(' ', CellAttributes())
    }
}
