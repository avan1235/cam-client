package `in`.procyk.webcam.client

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storage.StorageCodec
import kotlinx.serialization.Serializable

@Composable
actual inline fun <reified T : @Serializable Any> camCodec(): Codec<T> =
    StorageCodec(".cam-client")
