package id.neotica.orpheum.uploader.ui.feature.albumdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveAlbumDetailView(
    state: AlbumDetailUiState,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateYear: (String) -> Unit,
    onPickImage: () -> Unit,
    currentTrackId: String? = null,
    onTrackClick: (TrackRemoteModel) -> Unit = {},
    onDeleteTrack: (TrackRemoteModel) -> Unit = {},
    onUpdateTrack: (TrackRemoteModel, String, Int) -> Unit = { _, _, _ -> },
    onMoveTrack: (TrackRemoteModel, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var trackPendingDelete by remember { mutableStateOf<TrackRemoteModel?>(null) }
    var trackPendingEdit by remember { mutableStateOf<TrackRemoteModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.albumDetails?.album?.title ?: "Album Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2B05\uFE0F", style = MaterialTheme.typography.titleMedium)
                    }
                },
                actions = {
                    if (state.albumDetails != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("\uD83D\uDDD1\uFE0F")
                        }

                        Button(
                            onClick = onSave,
                            enabled = !state.isSaving
                        ) {
                            Text(if (state.isSaving) "\u23F3" else "\uD83D\uDCBE")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null && state.albumDetails == null) {
                Text(
                    text = "Error: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
                )
            } else if (state.albumDetails != null) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    if (maxWidth >= 840.dp) {
                        WideAlbumDetailContent(
                            state = state,
                            onUpdateTitle = onUpdateTitle,
                            onUpdateYear = onUpdateYear,
                            onPickImage = onPickImage,
                            currentTrackId = currentTrackId,
                            onTrackClick = onTrackClick,
                            onTrackDeleteRequest = { track -> trackPendingDelete = track },
                            onTrackEditRequest = { track -> trackPendingEdit = track },
                            onMoveTrack = onMoveTrack,
                            actionsEnabled = !state.isTrackMutating,
                        )
                    } else {
                        StackedAlbumDetailContent(
                            state = state,
                            onUpdateTitle = onUpdateTitle,
                            onUpdateYear = onUpdateYear,
                            onPickImage = onPickImage,
                            currentTrackId = currentTrackId,
                            onTrackClick = onTrackClick,
                            onTrackDeleteRequest = { track -> trackPendingDelete = track },
                            onTrackEditRequest = { track -> trackPendingEdit = track },
                            onMoveTrack = onMoveTrack,
                            actionsEnabled = !state.isTrackMutating,
                        )
                    }
                }
            }
        }
    }

    val pendingDelete = trackPendingDelete
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { trackPendingDelete = null },
            title = { Text("Delete track?") },
            text = { Text("\"${pendingDelete.title}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTrack(pendingDelete)
                    trackPendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { trackPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val pendingEdit = trackPendingEdit
    if (pendingEdit != null) {
        EditTrackDialog(
            track = pendingEdit,
            onDismiss = { trackPendingEdit = null },
            onSave = { title, number ->
                onUpdateTrack(pendingEdit, title, number)
                trackPendingEdit = null
            }
        )
    }
}

@Composable
private fun EditTrackDialog(
    track: TrackRemoteModel,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var title by remember { mutableStateOf(track.title) }
    var numberText by remember { mutableStateOf(track.trackNumber.toString()) }

    val parsedNumber = numberText.toIntOrNull()
    val isValid = title.isNotBlank() && parsedNumber != null && parsedNumber > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit track") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { input -> numberText = input.filter { it.isDigit() } },
                    label = { Text("Track Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedNumber?.let { onSave(title.trim(), it) } },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WideAlbumDetailContent(
    state: AlbumDetailUiState,
    onUpdateTitle: (String) -> Unit,
    onUpdateYear: (String) -> Unit,
    onPickImage: () -> Unit,
    currentTrackId: String?,
    onTrackClick: (TrackRemoteModel) -> Unit,
    onTrackDeleteRequest: (TrackRemoteModel) -> Unit,
    onTrackEditRequest: (TrackRemoteModel) -> Unit,
    onMoveTrack: (TrackRemoteModel, Int) -> Unit,
    actionsEnabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // LEFT COLUMN: Cover Art
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Cover Art", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            CoverArtSection(
                selectedFile = state.selectedCoverFile,
                currentCoverUrl = state.albumDetails?.album?.coverUrl,
                isLoading = state.isSaving,
                onPickImage = onPickImage
            )

            if (state.errorMessage != null) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.primary)
            }
        }

        // RIGHT COLUMN: Metadata & Tracks
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MetadataCard(
                editTitle = state.editTitle,
                editYear = state.editYear,
                onUpdateTitle = onUpdateTitle,
                onUpdateYear = onUpdateYear
            )

            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                TracklistHeader(trackCount = state.albumDetails?.tracks?.size ?: 0)

                val sortedTracks = state.albumDetails!!.tracks.sortedBy { it.trackNumber }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = bottomPlayerPadding(currentTrackId)
                ) {
                    itemsIndexed(
                        items = sortedTracks,
                        key = { _, track -> track.id }
                    ) { index, track ->
                        TrackRow(
                            track = track,
                            isActive = track.id == currentTrackId,
                            actionsEnabled = actionsEnabled,
                            onClick = { onTrackClick(track) },
                            onEdit = { onTrackEditRequest(track) },
                            onMoveUp = { onMoveTrack(track, -1) }.takeIf { index > 0 },
                            onMoveDown = { onMoveTrack(track, 1) }.takeIf { index < sortedTracks.size - 1 },
                            onDelete = { onTrackDeleteRequest(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StackedAlbumDetailContent(
    state: AlbumDetailUiState,
    onUpdateTitle: (String) -> Unit,
    onUpdateYear: (String) -> Unit,
    onPickImage: () -> Unit,
    currentTrackId: String?,
    onTrackClick: (TrackRemoteModel) -> Unit,
    onTrackDeleteRequest: (TrackRemoteModel) -> Unit,
    onTrackEditRequest: (TrackRemoteModel) -> Unit,
    onMoveTrack: (TrackRemoteModel, Int) -> Unit,
    actionsEnabled: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = bottomPlayerPadding(currentTrackId)
    ) {
        item {
            CoverArtSection(
                selectedFile = state.selectedCoverFile,
                currentCoverUrl = state.albumDetails?.album?.coverUrl,
                isLoading = state.isSaving,
                onPickImage = onPickImage
            )
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            MetadataCard(
                editTitle = state.editTitle,
                editYear = state.editYear,
                onUpdateTitle = onUpdateTitle,
                onUpdateYear = onUpdateYear
            )
        }

        item {
            TracklistHeader(trackCount = state.albumDetails?.tracks?.size ?: 0)
        }

        val sortedTracks = state.albumDetails!!.tracks.sortedBy { it.trackNumber }

        itemsIndexed(
            items = sortedTracks,
            key = { _, track -> track.id }
        ) { index, track ->
            TrackRow(
                track = track,
                isActive = track.id == currentTrackId,
                actionsEnabled = actionsEnabled,
                onClick = { onTrackClick(track) },
                onEdit = { onTrackEditRequest(track) },
                onMoveUp = { onMoveTrack(track, -1) }.takeIf { index > 0 },
                onMoveDown = { onMoveTrack(track, 1) }.takeIf { index < sortedTracks.size - 1 },
                onDelete = { onTrackDeleteRequest(track) }
            )
        }
    }
}

private fun bottomPlayerPadding(currentTrackId: String?): PaddingValues =
    PaddingValues(bottom = if (currentTrackId != null) 88.dp else 0.dp)

@Composable
private fun MetadataCard(
    editTitle: String,
    editYear: String,
    onUpdateTitle: (String) -> Unit,
    onUpdateYear: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Metadata",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = editTitle,
                onValueChange = onUpdateTitle,
                label = { Text("Album Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editYear,
                onValueChange = onUpdateYear,
                label = { Text("Release Year") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TracklistHeader(trackCount: Int) {
    Text(
        text = "Tracklist ($trackCount)",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun TrackRow(
    track: TrackRemoteModel,
    isActive: Boolean,
    onClick: () -> Unit,
    actionsEnabled: Boolean = true,
    onEdit: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${track.trackNumber}.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        track.title,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                track.artistName.let { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }

            if (isActive) {
                Text(
                    text = "\u266B",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Text(
                text = formatDuration(track.durationSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onMoveUp != null) {
                    TrackAction(text = "\u25B2", enabled = actionsEnabled, onClick = onMoveUp)
                }
                if (onMoveDown != null) {
                    TrackAction(text = "\u25BC", enabled = actionsEnabled, onClick = onMoveDown)
                }
                if (onEdit != null) {
                    TrackAction(text = "\u270F\uFE0F", enabled = actionsEnabled, onClick = onEdit)
                }
                if (onDelete != null) {
                    TrackAction(text = "\uD83D\uDDD1\uFE0F", enabled = actionsEnabled, onClick = onDelete)
                }
            }
        }
    }
}

@Composable
private fun TrackAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
private fun CoverArtSection(
    selectedFile: File?,
    currentCoverUrl: String?,
    isLoading: Boolean,
    onPickImage: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(backgroundColor)
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), shape)
                .clickable(enabled = !isLoading, onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                if (selectedFile != null) {
                    Text(
                        "\u2705 New Cover Ready",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        selectedFile.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Tap to change",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                } else if (currentCoverUrl != null) {
                    Text("\uD83D\uDDBC\uFE0F", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Current Cover:", fontWeight = FontWeight.Bold)
                    Text(
                        currentCoverUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Tap to Replace",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text("\uD83D\uDDBC\uFE0F", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No Cover Art", fontWeight = FontWeight.Bold)
                    Text(
                        "Tap to select image",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val sec = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$sec"
}
