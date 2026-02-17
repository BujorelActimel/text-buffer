package com.terminal.demo.ui

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.terminal.buffer.TerminalBuffer

data class PlaybackState(
    val currentTime: Float = 0f,
    val frameNumber: Int = 0,
    val totalFrames: Int = 0,
    val paused: Boolean = false,
    val speed: Float = 1.0f,
    val lastSequence: String = ""
)

class DebugRenderer(
    private val buffer: TerminalBuffer,
    private val offsetX: Int,
    private val offsetY: Int,
    private val width: Int
) {

    fun render(graphics: TextGraphics, state: PlaybackState) {
        graphics.setForegroundColor(TextColor.ANSI.WHITE)
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT)

        var row = offsetY

        // Title
        graphics.setForegroundColor(TextColor.ANSI.CYAN)
        putLine(graphics, row++, "=== Debug Info ===")
        graphics.setForegroundColor(TextColor.ANSI.WHITE)
        row++

        // Cursor position
        val cursor = buffer.getCursor()
        putLine(graphics, row++, "Cursor: (${cursor.column}, ${cursor.row})")

        // Buffer dimensions
        putLine(graphics, row++, "Buffer: ${buffer.width}x${buffer.height}")
        row++

        // Current attributes
        val attrs = buffer.getCurrentAttributes()
        graphics.setForegroundColor(TextColor.ANSI.YELLOW)
        putLine(graphics, row++, "--- Attributes ---")
        graphics.setForegroundColor(TextColor.ANSI.WHITE)

        putLine(graphics, row++, "FG: ${attrs.foreground}")
        putLine(graphics, row++, "BG: ${attrs.background}")
        putLine(graphics, row++, "Bold: ${attrs.styleFlags.bold}")
        putLine(graphics, row++, "Italic: ${attrs.styleFlags.italic}")
        putLine(graphics, row++, "Underline: ${attrs.styleFlags.underline}")
        row++

        // Playback info
        graphics.setForegroundColor(TextColor.ANSI.YELLOW)
        putLine(graphics, row++, "--- Playback ---")
        graphics.setForegroundColor(TextColor.ANSI.WHITE)

        val timeStr = "%.3f".format(state.currentTime)
        putLine(graphics, row++, "Time: ${timeStr}s")
        putLine(graphics, row++, "Frame: ${state.frameNumber}/${state.totalFrames}")
        putLine(graphics, row++, "Speed: ${state.speed}x")

        val statusColor = if (state.paused) TextColor.ANSI.RED else TextColor.ANSI.GREEN
        graphics.setForegroundColor(statusColor)
        putLine(graphics, row++, if (state.paused) "PAUSED" else "PLAYING")
        graphics.setForegroundColor(TextColor.ANSI.WHITE)
        row++

        // Scrollback info
        graphics.setForegroundColor(TextColor.ANSI.YELLOW)
        putLine(graphics, row++, "--- Buffer ---")
        graphics.setForegroundColor(TextColor.ANSI.WHITE)

        putLine(graphics, row++, "Lines: ${buffer.lineCount}")
        putLine(graphics, row++, "Scrollback: ${buffer.scrollbackSize}")
        row++

        // Controls
        graphics.setForegroundColor(TextColor.ANSI.YELLOW)
        putLine(graphics, row++, "--- Controls ---")
        graphics.setForegroundColor(TextColor.ANSI.WHITE)

        putLine(graphics, row++, "q     - Quit")
        putLine(graphics, row++, "SPACE - Pause/Resume")
        putLine(graphics, row++, "+/-   - Speed up/down")
        putLine(graphics, row++, "r     - Restart")
    }

    private fun putLine(graphics: TextGraphics, row: Int, text: String) {
        val truncated = if (text.length > width) text.take(width - 1) + ">" else text
        graphics.putString(offsetX, row, truncated.padEnd(width))
    }
}
