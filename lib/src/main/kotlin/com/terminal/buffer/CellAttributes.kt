package com.terminal.buffer

data class CellAttributes(
    val foreground: Color = Color.DEFAULT,
    val background: Color = Color.DEFAULT,
    val styleFlags: StyleFlags = StyleFlags(),
)
