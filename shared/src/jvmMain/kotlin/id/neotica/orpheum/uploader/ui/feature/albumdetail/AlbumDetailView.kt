package id.neotica.orpheum.uploader.ui.feature.albumdetail

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun AlbumDetailView(
    albumId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlbumDetailViewModel?
) {
    val resolvedViewModel = viewModel ?: koinViewModel(key = albumId) { parametersOf(albumId) }
    val state by resolvedViewModel.state.collectAsState()
    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {}
            override fun onEnded(event: DragAndDropEvent) {}
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val transferable = event.awtTransferable
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                    val validImage = files?.filterIsInstance<File>()?.firstOrNull {
                        it.extension.equals("png", true) ||
                        it.extension.equals("jpg", true) ||
                        it.extension.equals("jpeg", true) ||
                        it.extension.equals("webp", true)
                    }
                    if (validImage != null) {
                        resolvedViewModel.setCoverFile(validImage)
                        return true
                    }
                }
                return false
            }
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dropTarget)
    ) {
        AdaptiveAlbumDetailView(
            state = state,
            onNavigateBack = onNavigateBack,
            onDelete = resolvedViewModel::deleteAlbum,
            onSave = resolvedViewModel::saveChanges,
            onUpdateTitle = resolvedViewModel::updateTitle,
            onUpdateYear = resolvedViewModel::updateYear,
            onPickImage = {
                val file = chooseNativeImage()
                if (file != null) resolvedViewModel.setCoverFile(file)
            },
        )
    }
}

private fun chooseNativeImage(): File? {
    val dialog = FileDialog(null as Frame?, "Select Album Cover", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.endsWith(".png", true) ||
        name.endsWith(".jpg", true) ||
        name.endsWith(".jpeg", true) ||
        name.endsWith(".webp", true)
    }
    dialog.isVisible = true
    val directory = dialog.directory
    val file = dialog.file
    return if (directory != null && file != null) File(directory, file) else null
}
