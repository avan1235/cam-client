package `in`.procyk.webcam.client

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.webcam.client.Key.Companion.fromBase64
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storeOf
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


class AppViewModel(codec: Codec<Data>) : ViewModel() {

    @Serializable
    data class Data(
        val url: String,
        val key: String,
    )

    private companion object {
        val default = Data("", "")
    }

    private val client = HttpClient {
        install(WebSockets)
    }

    private val rawData = MutableStateFlow<ByteArray?>(null)

    private val store = storeOf<Data>(codec, default)
    val data: StateFlow<Data> = store.updates
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), default)

    init {
        viewModelScope.launch {
            store.updates
                .mapNotNull {
                    val key = it?.key ?: return@mapNotNull null
                    val decrypt = runCatching {
                        decryptor(fromBase64(key))
                    }.getOrNull() ?: return@mapNotNull null
                    decrypt to it.url
                }
                .collectLatest { (decrypt, path) ->
                    client.webSocket(path) {
                        incoming
                            .consumeAsFlow()
                            .conflate()
                            .debounce(500.milliseconds)
                            .collectLatest { frame ->
                                val data = (frame as? Frame.Binary)?.data ?: return@collectLatest
                                rawData.value = decrypt(data)
                            }
                    }
                }
        }
    }

    val imageBitmap: StateFlow<ImageBitmap?> = rawData
        .map { it?.decodeToImageBitmap() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeout = 1.seconds), null)

    fun onKeyChange(key: String) {
        viewModelScope.launch {
            store.update { it?.copy(key = key) }
        }
    }

    fun onUrlChange(url: String) {
        viewModelScope.launch {
            store.update { it?.copy(url = url) }
        }
    }
}

@Composable
expect inline fun <reified T : @Serializable Any> camCodec(): Codec<T>
