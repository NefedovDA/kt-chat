package ru.nda.chat

import java.io.Closeable
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ChatServer(private val port: Int) : Thread() {
    private val activeClients: MutableList<ActiveClient> = mutableListOf()

    init {
        start()
    }

    override fun run() {
        ServerSocket(port).use { server ->
            while (!isInterrupted) {
                val client = server.accept()
                println("added!")
                try {
                    activeClients.add(ActiveClient(client))
                } catch (e: IOException) {
                    client.close()
                }
            }
            activeClients.forEach(Thread::interrupt)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit =
            ChatServer(4004).let { readLine(); it.interrupt() }
    }

    private inner class ActiveClient(private val client: Socket) : Thread(), Closeable {
        private val input = client.getInputStream().bufferedReader()
        private val output = client.getOutputStream().bufferedWriter()

        init {
            start()
        }

        override fun run() = use { _ ->
            try {
                while (!interrupted() && client.isConnected) {
                    val msg = input.readLine()
                    activeClients.forEach { it.send(msg) }
                }
            } catch (_: IOException) {
                // ignore
            }
        }

        override fun close() {
            input.close()
            output.close()
            client.close()
        }

        private fun send(msg: String) =
            with(output) {
                write(msg)
                newLine()
                flush()
            }
    }
}
