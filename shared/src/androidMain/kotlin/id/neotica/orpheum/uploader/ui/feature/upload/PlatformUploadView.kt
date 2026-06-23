package id.neotica.orpheum.uploader.ui.feature.upload

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

@Composable
actual fun PlatformUploadView() {
    val viewModel = koinViewModel<UploadViewModel>()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val name = resolveDisplayName(it, context) ?: "audio_${System.currentTimeMillis()}"
                val temp = File(context.cacheDir, name)
                context.contentResolver.openInputStream(it)?.use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                viewModel.setFile(temp)
            } catch (_: Exception) {
                viewModel.setFile(null)
            }
        }
    }

    AdaptiveUploadView(
        viewModel = viewModel,
        onPickFile = { launcher.launch("audio/*") },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun resolveDisplayName(uri: android.net.Uri, context: android.content.Context): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) return it.getString(nameIndex)
        }
    }
    return uri.lastPathSegment
}
