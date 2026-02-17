package com.terminal.demo.ui

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import java.nio.charset.Charset
import com.terminal.buffer.TerminalBuffer

class DemoApp(
    private val buffer: TerminalBuffer,
    private val title: String? = null
) {
    private lateinit var terminal: Terminal
    private lateinit var screen: Screen
    private lateinit var terminalRenderer: TerminalRenderer
    private lateinit var debugRenderer: DebugRenderer

    private var playbackState = PlaybackState()
    private val debugPanelWidth = 25

    fun start() {
        terminal = DefaultTerminalFactory()
            .setPreferTerminalEmulator(false)  // Prefer native terminal when available
            .setTerminalEmulatorTitle(title ?: "Asciinema Demo")
            .createTerminal()
        screen = TerminalScreen(terminal)
        screen.startScreen()
        screen.cursorPosition = null

        setupRenderers()
    }

    private fun setupRenderers() {
        val size = screen.terminalSize

        // Terminal panel starts at (1, 2) to leave room for border
        terminalRenderer = TerminalRenderer(buffer, 1, 2)

        // Debug panel on the right side
        val debugX = buffer.width + 4
        debugRenderer = DebugRenderer(buffer, debugX, 2, debugPanelWidth)
    }

    fun render(currentTime: Float, frameNumber: Int, totalFrames: Int) {
        playbackState = playbackState.copy(
            currentTime = currentTime,
            frameNumber = frameNumber,
            totalFrames = totalFrames
        )

        val graphics = screen.newTextGraphics()

        // Clear screen
        graphics.setBackgroundColor(TextColor.ANSI.DEFAULT)
        graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
        for (row in 0 until screen.terminalSize.rows) {
            graphics.putString(0, row, " ".repeat(screen.terminalSize.columns))
        }

        // Draw title bar
        graphics.setForegroundColor(TextColor.ANSI.CYAN)
        val displayTitle = title ?: "Asciinema Playback"
        graphics.putString(1, 0, displayTitle.take(buffer.width + 2))

        // Draw terminal border
        graphics.setForegroundColor(TextColor.ANSI.WHITE)
        drawBox(graphics, 0, 1, buffer.width + 2, buffer.height + 2)

        // Render terminal content
        terminalRenderer.render(graphics, showCursor = !playbackState.paused || (System.currentTimeMillis() / 500) % 2 == 0L)

        // Render debug panel
        debugRenderer.render(graphics, playbackState)

        screen.refresh()
    }

    private fun drawBox(graphics: com.googlecode.lanterna.graphics.TextGraphics, x: Int, y: Int, width: Int, height: Int) {
        // Corners
        graphics.setCharacter(x, y, '\u250C')
        graphics.setCharacter(x + width - 1, y, '\u2510')
        graphics.setCharacter(x, y + height - 1, '\u2514')
        graphics.setCharacter(x + width - 1, y + height - 1, '\u2518')

        // Top and bottom edges
        for (col in 1 until width - 1) {
            graphics.setCharacter(x + col, y, '\u2500')
            graphics.setCharacter(x + col, y + height - 1, '\u2500')
        }

        // Left and right edges
        for (row in 1 until height - 1) {
            graphics.setCharacter(x, y + row, '\u2502')
            graphics.setCharacter(x + width - 1, y + row, '\u2502')
        }
    }

    sealed class InputEvent {
        object Quit : InputEvent()
        object TogglePause : InputEvent()
        object SpeedUp : InputEvent()
        object SpeedDown : InputEvent()
        object Restart : InputEvent()
        object None : InputEvent()
    }

    fun pollInput(): InputEvent {
        val keyStroke = screen.pollInput() ?: return InputEvent.None

        return when {
            keyStroke.keyType == KeyType.Character && keyStroke.character == 'q' -> InputEvent.Quit
            keyStroke.keyType == KeyType.Character && keyStroke.character == ' ' -> InputEvent.TogglePause
            keyStroke.keyType == KeyType.Character && (keyStroke.character == '+' || keyStroke.character == '=') -> InputEvent.SpeedUp
            keyStroke.keyType == KeyType.Character && (keyStroke.character == '-' || keyStroke.character == '_') -> InputEvent.SpeedDown
            keyStroke.keyType == KeyType.Character && keyStroke.character == 'r' -> InputEvent.Restart
            keyStroke.keyType == KeyType.Escape -> InputEvent.Quit
            keyStroke.keyType == KeyType.EOF -> InputEvent.Quit
            else -> InputEvent.None
        }
    }

    fun setPaused(paused: Boolean) {
        playbackState = playbackState.copy(paused = paused)
    }

    fun isPaused(): Boolean = playbackState.paused

    fun setSpeed(speed: Float) {
        playbackState = playbackState.copy(speed = speed)
    }

    fun getSpeed(): Float = playbackState.speed

    fun stop() {
        screen.stopScreen()
        terminal.close()
    }

    fun waitForKeyPress() {
        screen.readInput()
    }
}
