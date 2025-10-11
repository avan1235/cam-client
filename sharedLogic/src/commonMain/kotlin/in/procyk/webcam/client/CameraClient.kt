package `in`.procyk.webcam.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend fun HttpClient.handleClientRequests(
    url: String,
    refreshEachMillis: Duration = 500.milliseconds,
    onData: suspend (ByteArray) -> Unit,
) {
    while (true) runCatching {
        webSocket(url) {
            var last = Clock.System.now()
            while (currentCoroutineContext().isActive) {
                val now = Clock.System.now()
                val diff = now - last
                if (diff < refreshEachMillis) {
                    delay(refreshEachMillis - diff)
                }
                last = now

                outgoing.send(RequestFrame)
                val frame = incoming.receive() as? Frame.Binary ?: continue
                onData(frame.data)
            }
        }
    }.onFailure {
        println("WebSocket failed with ${it.message}, retrying in 1 second...")
        delay(1.seconds)
    }
}

private val RequestFrame: Frame.Text = Frame.Text("s")