package `in`.procyk.webcam.sender

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import `in`.procyk.qrcodegen.QrCode
import `in`.procyk.webcam.client.Key
import `in`.procyk.webcam.client.encryptor
import `in`.procyk.webcam.client.handleClientRequests
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.Frame.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

private class SenderCommand : CliktCommand() {
    val keyFile: String by option("--key-file", "-k", metavar = "<path>")
        .default(".cam-client-key")
        .help("Path to file containing the key used for encryption")
    val generateKey: Boolean by option("--generate-key", "-g")
        .flag(default = false)
        .help("Generate new key and overwrite the file containing it")
    val wsRemoteUrl: String by option("--ws-remote-url", "-w", metavar = "<url>")
        .required()
        .help("Remote URL of the webcam server")
    val mappings: List<String> by argument()
        .multiple(required = true)
        .help("Camera mappings in format <local-camera-url>:<remote-path>")

    override fun run() = runBlocking {
        val path = Path(keyFile)
        val key = when {
            generateKey || !SystemFileSystem.exists(path) -> Key.generate().also { key ->
                println("Writing key to $path")
                SystemFileSystem.sink(path).buffered().use {
                    it.writeString(key.encodeBase64().also(::printlnKeyQrCode))
                }
            }

            else -> SystemFileSystem.source(path).buffered().use {
                it.readString()
            }.also(::printlnKeyQrCode).let(Key::fromBase64)
        }
        val client = HttpClient {
            install(WebSockets)
        }
        val encrypt = encryptor(key)
        mappings.forEach { mapping ->
            val (local, remote) = mapping.split(":", limit = 2)
            launch {
                val dataChannel = Channel<ByteArray>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                val localUrl = "ws://$local"
                val remoteUrl = "$wsRemoteUrl/${remote.removePrefix("/")}"
                println("Streaming from $localUrl to $remoteUrl")
                launch {
                    client.handleClientRequests(localUrl) {
                        dataChannel.trySend(it)
                    }
                }
                launch {
                    client.webSocket(remoteUrl) {
                        dataChannel.receiveAsFlow().collect {
                            send(Binary(true, encrypt(it)))
                        }
                    }
                }
            }
        }
    }
}

private fun printlnKeyQrCode(key: String) {
    val qrCode = QrCode.encodeText(key, QrCode.Ecc.HIGH)
    println(qrCode.toQrString())
}

fun main(args: Array<String>) = SenderCommand().main(args)