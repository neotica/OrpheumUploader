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

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setFile(copyUriToCache(it, context, "audio_${System.currentTimeMillis()}"))
        }
    }

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setCoverFile(copyUriToCache(it, context, "cover_${System.currentTimeMillis()}"))
        }
    }

    AdaptiveUploadView(
        viewModel = viewModel,
        onPickFile = { audioLauncher.launch("audio/*") },
        onPickCover = { coverLauncher.launch("image/*") },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun copyUriToCache(
    uri: android.net.Uri,
    context: android.content.Context,
    fallbackName: String,
): File? = try {
    val name = resolveDisplayName(uri, context) ?: fallbackName
    val temp = File(context.cacheDir, name)
    context.contentResolver.openInputStream(uri)?.use { input ->
        temp.outputStream().use { output -> input.copyTo(output) }
    }
    temp
} catch (_: Exception) {
    null
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
