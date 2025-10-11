package `in`.procyk.webcam.client

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
actual inline fun <reified T : @Serializable Any> camCodec(): Codec<T> {
    val file = Path(System.getProperty("user.home"))

    with(SystemFileSystem) { if (!exists(file)) createDirectories(file) }

    return FileCodec<T>(
        file = Path(file, ".cam-client"),
        tempFile = Path(file, Uuid.random().toHexDashString())
    )
}
