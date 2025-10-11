package `in`.procyk.webcam.client

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
actual inline fun <reified T : @Serializable Any> camCodec(): Codec<T> {
    val context = LocalContext.current
    val filesDir = context.filesDir
    val file = filesDir.resolve(".cam-client")

    return FileCodec<T>(
        file = Path(file.absolutePath),
        tempFile = Path(filesDir.resolve(Uuid.random().toHexDashString()).absolutePath)
    )
}
