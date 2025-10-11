package `in`.procyk.webcam.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.procyk.webcam.client.AppViewModel.Data
import io.github.xxfast.kstore.Codec

@Composable
fun App(
    codec: Codec<Data> = camCodec(),
    viewModel: AppViewModel = viewModel { AppViewModel(codec) }
) {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val imageBitmap by viewModel.imageBitmap.collectAsState()
            when (val imageBitmap = imageBitmap) {
                null -> Text("No image data")
                else -> Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),

                    contentScale = ContentScale.Crop,
                )
            }
            Row(
                modifier = Modifier.background(
                    verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                ).padding(12.dp).fillMaxWidth().align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val data by viewModel.data.collectAsState()
                OutlinedTextField(
                    value = data.url,
                    onValueChange = viewModel::onUrlChange,
                    label = { Text("Url") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = data.key,
                    onValueChange = viewModel::onKeyChange,
                    label = { Text("Key") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}