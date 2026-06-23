package id.neotica.orpheum.uploader.ui.feature.albumdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AlbumDetailView(
    albumId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlbumDetailViewModel?
) {
    val resolvedViewModel = viewModel ?: koinViewModel(key = albumId) { parametersOf(albumId) }
    val state by resolvedViewModel.state.collectAsState()

    // Automatically navigate back if the album gets deleted
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.albumDetails?.album?.title ?: "Loading Album...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        Text("⬅️")
                    }
                },
                actions = {
                    if (state.albumDetails != null) {
                        OutlinedButton(
                            onClick = resolvedViewModel::deleteAlbum,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
//                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("🗑️")
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Album")
                        }

                        Button(
                            onClick = resolvedViewModel::saveChanges,
                            enabled = !state.isSaving
                        ) {
                            Text("💾")
//                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.isSaving) "Saving..." else "Save Changes")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null && state.albumDetails == null) {
                Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (state.albumDetails != null) {

                // Desktop Split Layout
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {

                    // LEFT COLUMN: Cover Art (Weight 1)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Cover Art", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        ImageDropzone(
                            selectedFile = state.selectedCoverFile,
                            currentCoverUrl = state.albumDetails?.album?.coverUrl,
                            isLoading = state.isSaving,
                            onFileSelected = resolvedViewModel::setCoverFile
                        )

                        if (state.errorMessage != null) {
                            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // RIGHT COLUMN: Metadata & Tracks (Weight 2)
                    Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                        // Metadata Editor Card
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Metadata", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = state.editTitle,
                                    onValueChange = resolvedViewModel::updateTitle,
                                    label = { Text("Album Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = state.editYear,
                                    onValueChange = resolvedViewModel::updateYear,
                                    label = { Text("Release Year") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Tracklist
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Tracklist (${state.albumDetails?.tracks?.size ?: 0})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.albumDetails!!.tracks.sortedBy { it.trackNumber }) { track ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Text("${track.trackNumber}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(track.title, fontWeight = FontWeight.Medium)
                                            }
                                            // Optional: Format duration if you start parsing it
                                            Text(track.fileUrl.substringAfterLast("/"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ImageDropzone(
    selectedFile: File?,
    currentCoverUrl: String?,
    isLoading: Boolean,
    onFileSelected: (File?) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }

    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) { isDragging = true }
            override fun onEnded(event: DragAndDropEvent) { isDragging = false }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
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
                        onFileSelected(validImage)
                        return true
                    }
                }
                return false
            }
        }
    }

    val borderColor = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isDragging) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Keep it a perfect square!
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(12.dp))
            .dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dropTarget)
            .clickable(enabled = !isLoading) {
                val file = chooseNativeImage()
                if (file != null) onFileSelected(file)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            if (selectedFile != null) {
                Text("✅ New Cover Ready", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(selectedFile.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text("Click to change", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            } else if (currentCoverUrl != null) {
                Text("🖼️", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("Existing Cover Path:", fontWeight = FontWeight.Bold)
                Text(currentCoverUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(24.dp))
                Text("Drag & Drop or Click to Replace", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            } else {
                Text("🖼️", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("No Cover Art", fontWeight = FontWeight.Bold)
                Text("Drag & Drop JPG/PNG", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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