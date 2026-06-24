package id.neotica.orpheum.uploader.ui.feature.albumdetail

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File

@Composable
actual fun AlbumDetailView(
    albumId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlbumDetailViewModel?
) {
    val resolvedViewModel = viewModel ?: koinViewModel(key = albumId) { parametersOf(albumId) }
    val state by resolvedViewModel.state.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val name = resolveFileName(it, context) ?: "cover_${System.currentTimeMillis()}"
                val temp = File(context.cacheDir, name)
                context.contentResolver.openInputStream(it)?.use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                resolvedViewModel.setCoverFile(temp)
            } catch (_: Exception) {
                resolvedViewModel.setCoverFile(null)
            }
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    AdaptiveAlbumDetailView(
        state = state,
        onNavigateBack = onNavigateBack,
        onDelete = resolvedViewModel::deleteAlbum,
        onSave = resolvedViewModel::saveChanges,
        onUpdateTitle = resolvedViewModel::updateTitle,
        onUpdateYear = resolvedViewModel::updateYear,
        onPickImage = { imagePickerLauncher.launch("image/*") },
    )
}

private fun resolveFileName(uri: android.net.Uri, context: android.content.Context): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) return it.getString(nameIndex)
        }
    }
    return uri.lastPathSegment
}
