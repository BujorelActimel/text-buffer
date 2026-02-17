package com.terminal.demo.parser

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File

data class RecordingHeader(
    val version: Int,
    val width: Int,
    val height: Int,
    val duration: Float? = null,
    val title: String? = null
)

data class RecordingEvent(
    val time: Float,
    val type: String,  // "o" (output), "i" (input), "r" (resize)
    val data: String
)

class AsciinemaParser(private val file: File) {
    private val gson = Gson()
    private val lines: List<String> by lazy { file.readLines() }

    constructor(path: String) : this(File(path))

    fun readHeader(): RecordingHeader {
        require(lines.isNotEmpty()) { "Empty recording file" }
        val headerJson = gson.fromJson(lines[0], JsonObject::class.java)

        return RecordingHeader(
            version = headerJson.get("version")?.asInt ?: 2,
            width = headerJson.get("width")?.asInt ?: 80,
            height = headerJson.get("height")?.asInt ?: 24,
            duration = headerJson.get("duration")?.asFloat,
            title = headerJson.get("title")?.asString
        )
    }

    fun readEvents(): Sequence<RecordingEvent> = sequence {
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val eventArray = gson.fromJson(line, JsonArray::class.java)
            if (eventArray.size() >= 3) {
                yield(
                    RecordingEvent(
                        time = eventArray[0].asFloat,
                        type = eventArray[1].asString,
                        data = eventArray[2].asString
                    )
                )
            }
        }
    }

    fun countEvents(): Int = lines.size - 1
}
