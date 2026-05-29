package id.neotica.orpheum.uploader.ui.feature.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
fun UploadView(
    viewModel: UploadViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Orpheum Uploader",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Form Container
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Artist Section ---
                Text("Artist Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                OutlinedTextField(
                    value = state.artistName,
                    onValueChange = viewModel::updateArtistName,
                    label = { Text("Artist Name") },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.artistBio,
                    onValueChange = viewModel::updateArtistBio,
                    label = { Text("Artist Bio (Optional)") },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                // --- Album Section ---
                Text("Album Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = state.albumTitle,
                        onValueChange = viewModel::updateAlbumTitle,
                        label = { Text("Album Title") },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.releaseYear,
                        onValueChange = viewModel::updateReleaseYear,
                        label = { Text("Year") },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                // --- Track Section ---
                Text("Track Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = state.trackTitle,
                        onValueChange = viewModel::updateTrackTitle,
                        label = { Text("Track Title") },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(3f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.trackNumber,
                        onValueChange = viewModel::updateTrackNumber,
                        label = { Text("Track No.") },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- File Dropzone ---
                FileDropzone(
                    selectedFile = state.selectedFile,
                    isLoading = state.isLoading,
                    onFileSelected = viewModel::setFile
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Action Section ---
        UploadActionSection(
            state = state,
            onSubmit = viewModel::submitUpload,
            onDismissMessage = viewModel::dismissMessage
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FileDropzone(
    selectedFile: File?,
    isLoading: Boolean,
    onFileSelected: (File?) -> Unit
) {
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
                        // For this current version, we just grab the first file
                        onFileSelected(validFiles.first())
                        return true
                    }
                }
                return false
            }
        }
    }

    val borderColor = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isDragging) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(8.dp))
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dropTarget
            )
            .clickable(enabled = !isLoading) {
                val file = chooseNativeFile()
                if (file != null) onFileSelected(file)
            },
        contentAlignment = Alignment.Center
    ) {
        if (selectedFile == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "☁️",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Click to browse or drag MP3/M4A here",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎵",
                    fontSize = 32.sp
                )
                Text(
                    text = selectedFile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "❌",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable(enabled = !isLoading) { onFileSelected(null) }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun UploadActionSection(
    state: UploadUiState,
    onSubmit: () -> Unit,
    onDismissMessage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            LinearProgressIndicator(
                progress = { state.uploadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Text(
                text = state.statusMessage ?: "Processing...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Start Upload Chain")
            }
        }

        // Status / Error Messages
        if (!state.isLoading && state.statusMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = if (state.isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onDismissMessage() }
            ) {
                Text(
                    text = state.statusMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (state.isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Native AWT File Picker for Compose Desktop
private fun chooseNativeFile(): File? {
    val dialog = FileDialog(null as Frame?, "Select Audio File", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.endsWith(".mp3", ignoreCase = true) || name.endsWith(".m4a", ignoreCase = true)
    }
    dialog.isVisible = true

    val directory = dialog.directory
    val file = dialog.file

    return if (directory != null && file != null) {
        File(directory, file)
    } else {
        null
    }
}