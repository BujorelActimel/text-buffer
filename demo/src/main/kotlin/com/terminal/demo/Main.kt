package com.terminal.demo

import com.terminal.buffer.TerminalBuffer
import com.terminal.demo.parser.AnsiCodeHandler
import com.terminal.demo.parser.AnsiParser
import com.terminal.demo.parser.AsciinemaParser
import com.terminal.demo.parser.RecordingEvent
import com.terminal.demo.ui.DemoApp
import java.io.File

fun main(args: Array<String>) {
    val castFile = args.getOrNull(0)
    if (castFile == null) {
        printUsage()
        return
    }

    val file = File(castFile)
    if (!file.exists()) {
        System.err.println("Error: File not found: $castFile")
        return
    }

    try {
        runPlayback(file)
    } catch (e: Exception) {
        System.err.println("Error during playback: ${e.message}")
        e.printStackTrace()
    }
}

private fun printUsage() {
    println("""
        Asciinema Visualizer Demo

        Usage: demo <file.cast>

        Plays an asciinema recording with a side-by-side debug view.

        Controls:
          q, Escape  - Quit
          Space      - Pause/Resume
          +/=        - Speed up (2x)
          -/_        - Slow down (0.5x)
          r          - Restart playback
    """.trimIndent())
}

private fun runPlayback(file: File) {
    val parser = AsciinemaParser(file)
    val header = parser.readHeader()

    println("Loading: ${file.name}")
    println("Size: ${header.width}x${header.height}")
    header.duration?.let { println("Duration: %.2fs".format(it)) }
    header.title?.let { println("Title: $it") }

    val events = parser.readEvents().filter { it.type == "o" }.toList()
    val totalFrames = events.size
    println("Frames: $totalFrames")
    println()
    println("Starting playback...")
    Thread.sleep(500)

    var restart = true
    while (restart) {
        restart = playRecording(header.width, header.height, events, header.title)
    }
}

private fun playRecording(
    width: Int,
    height: Int,
    events: List<RecordingEvent>,
    title: String?
): Boolean {
    val buffer = TerminalBuffer(width, height, 1000)
    val handler = AnsiCodeHandler(buffer)
    val app = DemoApp(buffer, title)

    app.start()

    try {
        var frameIndex = 0
        var lastEventTime = 0f
        var playbackTime = 0L
        var lastRealTime = System.currentTimeMillis()

        // Initial render
        app.render(0f, 0, events.size)

        while (frameIndex < events.size) {
            // Handle input
            when (app.pollInput()) {
                DemoApp.InputEvent.Quit -> {
                    app.stop()
                    return false
                }
                DemoApp.InputEvent.TogglePause -> {
                    app.setPaused(!app.isPaused())
                    if (!app.isPaused()) {
                        lastRealTime = System.currentTimeMillis()
                    }
                }
                DemoApp.InputEvent.SpeedUp -> {
                    val newSpeed = (app.getSpeed() * 2).coerceAtMost(16f)
                    app.setSpeed(newSpeed)
                }
                DemoApp.InputEvent.SpeedDown -> {
                    val newSpeed = (app.getSpeed() / 2).coerceAtLeast(0.125f)
                    app.setSpeed(newSpeed)
                }
                DemoApp.InputEvent.Restart -> {
                    app.stop()
                    return true
                }
                DemoApp.InputEvent.None -> {}
            }

            if (app.isPaused()) {
                // Just re-render to update cursor blink and status
                app.render(events.getOrNull(frameIndex - 1)?.time ?: 0f, frameIndex, events.size)
                Thread.sleep(50)
                continue
            }

            val currentRealTime = System.currentTimeMillis()
            val realDelta = currentRealTime - lastRealTime
            lastRealTime = currentRealTime

            // Update playback time with speed multiplier
            playbackTime += (realDelta * app.getSpeed()).toLong()
            val currentPlaybackSeconds = playbackTime / 1000f

            // Process all events up to current playback time
            while (frameIndex < events.size) {
                val event = events[frameIndex]
                if (event.time > currentPlaybackSeconds) {
                    break
                }

                // Parse and apply the ANSI sequences
                val tokens = AnsiParser.parse(event.data)
                handler.apply(tokens)

                frameIndex++
                lastEventTime = event.time
            }

            app.render(lastEventTime, frameIndex, events.size)

            // Small sleep to avoid busy-waiting
            Thread.sleep(10)
        }

        // Playback finished - wait for user input
        app.setPaused(true)
        app.render(lastEventTime, events.size, events.size)

        while (true) {
            when (app.pollInput()) {
                DemoApp.InputEvent.Quit -> {
                    app.stop()
                    return false
                }
                DemoApp.InputEvent.Restart -> {
                    app.stop()
                    return true
                }
                else -> {}
            }
            Thread.sleep(50)
            app.render(lastEventTime, events.size, events.size)
        }
    } catch (e: Exception) {
        app.stop()
        throw e
    }
}
