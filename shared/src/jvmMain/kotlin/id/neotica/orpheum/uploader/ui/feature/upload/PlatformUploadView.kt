package id.neotica.orpheum.uploader.ui.feature.upload

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import org.koin.compose.viewmodel.koinViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun PlatformUploadView() {
    val viewModel = koinViewModel<UploadViewModel>()
    var isDragging by remember { mutableStateOf(false) }

    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDragging = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragging = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
                val transferable = event.awtTransferable
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                    val validFiles = files?.filterIsInstance<File>() ?: emptyList()
                    if (validFiles.isNotEmpty()) {
                        viewModel.setFile(validFiles.first())
                        return true
                    }
                }
                return false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dropTarget,
            ),
    ) {
        AdaptiveUploadView(
            viewModel = viewModel,
            onPickFile = {
                val file = chooseNativeAudioFile()
                if (file != null) viewModel.setFile(file)
            },
        )
    }
}

private fun chooseNativeAudioFile(): File? {
    val dialog = FileDialog(null as Frame?, "Select Audio File", FileDialog.LOAD)
    dialog.filenameFilter = { _, name ->
        name.endsWith(".mp3", ignoreCase = true) || name.endsWith(".m4a", ignoreCase = true)
    }
    dialog.isVisible = true

    val directory = dialog.directory
    val file = dialog.file

    return if (directory != null && file != null) File(directory, file) else null
}
