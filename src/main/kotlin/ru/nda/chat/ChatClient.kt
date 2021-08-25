package ru.nda.chat

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable
import java.io.IOException
import java.net.Socket

class ChatClient(
    private val host: String,
    private val port: Int,
    val clientName: String
) : Closeable {
    private lateinit var server: Socket
    private lateinit var reader: Reader
    private lateinit var writer: Writer

    init {
        start()
    }

    private fun start() {
        server = Socket(host, port)
        reader = Reader(server.getOutputStream().bufferedWriter())
        writer = Writer(server.getInputStream().bufferedReader())
    }

    override fun close() {
        if (this::reader.isInitialized) reader.interrupt()
        if (this::writer.isInitialized) writer.interrupt()
        if (this::server.isInitialized) server.close()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = ChatClient("localhost", 4004, readLine() ?: "").use {  }
    }

    private inner class Reader(private val output: BufferedWriter) : Thread() {
        init {
            start()
        }

        override fun run() = output.use { _ ->
            while (!isInterrupted) {
                val msg = readLine() ?: throw IOException()
                with(output) {
                    write("$clientName: $msg")
                    newLine()
                    flush()
                }
            }
        }
    }

    private inner class Writer(private val input: BufferedReader) : Thread() {
        init {
            start()
        }

        override fun run() = input.use { _ ->
            while (!isInterrupted) {
                val msg = input.readLine()
                println(msg)
            }
        }
    }
}
