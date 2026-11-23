package id.neotica.orpheum.uploader.ui.feature.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.neotica.orpheum.uploader.domain.model.artist.ArtistRemoteModel
import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel

private enum class WidthClass { Compact, Medium, Expanded }

@Composable
fun AdaptiveUploadView(
    viewModel: UploadViewModel,
    onPickFile: () -> Unit,
    onPickCover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var showArtistPicker by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth
        val widthClass = when {
            widthDp < 600.dp -> WidthClass.Compact
            widthDp < 840.dp -> WidthClass.Medium
            else -> WidthClass.Expanded
        }

        val horizPadding = when (widthClass) {
            WidthClass.Compact -> 16.dp
            WidthClass.Medium -> 24.dp
            WidthClass.Expanded -> 32.dp
        }
        val cardMaxWidth = when (widthClass) {
            WidthClass.Compact -> 1f
            WidthClass.Medium -> 0.92f
            WidthClass.Expanded -> 0.75f
        }
        val sectionSpacing = when (widthClass) {
            WidthClass.Compact -> 12.dp
            else -> 16.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = horizPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(cardMaxWidth),
                elevation = CardDefaults.cardElevation(defaultElevation = if (widthClass == WidthClass.Compact) 0.dp else 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (widthClass == WidthClass.Compact) 16.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                ) {
                    SectionHeader("Artist Information")

                    val selectedArtist = state.selectedArtist
                    if (selectedArtist == null) {
                        OutlinedTextField(
                            value = state.artistName,
                            onValueChange = viewModel::updateArtistName,
                            label = { Text("Artist Name *") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = state.artistBio,
                            onValueChange = viewModel::updateArtistBio,
                            label = { Text("Artist Bio (Optional)") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = if (widthClass == WidthClass.Compact) 2 else 3,
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.onArtistPickerOpened()
                                showArtistPicker = true
                            },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Select Existing Artist")
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedArtist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    val bio = selectedArtist.bio
                                    if (!bio.isNullOrBlank()) {
                                        Text(
                                            text = bio,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.onArtistPickerOpened()
                                        showArtistPicker = true
                                    },
                                    enabled = !state.isLoading,
                                ) {
                                    Text("Change")
                                }

                                TextButton(
                                    onClick = viewModel::clearArtistSelection,
                                    enabled = !state.isLoading,
                                ) {
                                    Text("Create new")
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    SectionHeader("Album Information")

                    if (state.selectedArtist != null) {
                        AlbumSelector(
                            selectedAlbum = state.selectedAlbum,
                            albums = state.artistAlbums,
                            isLoading = state.isLoadingAlbums,
                            errorMessage = state.albumsError,
                            enabled = !state.isLoading,
                            onSelect = viewModel::selectAlbum,
                            onCreateNew = viewModel::clearAlbumSelection,
                        )
                    }

                    val selectedAlbum = state.selectedAlbum
                    if (selectedAlbum == null) {
                        if (widthClass == WidthClass.Compact) {
                            OutlinedTextField(
                                value = state.albumTitle,
                                onValueChange = viewModel::updateAlbumTitle,
                                label = { Text("Album Title *") },
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = state.releaseYear,
                                onValueChange = viewModel::updateReleaseYear,
                                label = { Text("Year") },
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = state.albumTitle,
                                    onValueChange = viewModel::updateAlbumTitle,
                                    label = { Text("Album Title *") },
                                    enabled = !state.isLoading,
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = state.releaseYear,
                                    onValueChange = viewModel::updateReleaseYear,
                                    label = { Text("Year") },
                                    enabled = !state.isLoading,
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                            }
                        }

                        SectionHeader("Cover Art (Optional)")
                        FileSelectionArea(
                            hasFile = state.selectedCoverFile != null,
                            fileName = state.selectedCoverFile?.name,
                            isLoading = state.isLoading,
                            onPickFile = onPickCover,
                            onClearFile = { viewModel.setCoverFile(null) },
                            emptyPrompt = "Tap to select cover image (JPG / PNG / WebP)",
                            emptyIcon = "\uD83D\uDDBC\uFE0F",
                            selectedIcon = "\uD83D\uDDBC\uFE0F",
                        )
                    } else {
                        Text(
                            text = "Release Year: ${selectedAlbum.releaseYear}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    HorizontalDivider()

                    SectionHeader("Track Information")
                    if (widthClass == WidthClass.Compact) {
                        OutlinedTextField(
                            value = state.trackTitle,
                            onValueChange = viewModel::updateTrackTitle,
                            label = { Text("Track Title *") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.trackNumber,
                            onValueChange = viewModel::updateTrackNumber,
                            label = { Text("Track No.") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = state.trackTitle,
                                onValueChange = viewModel::updateTrackTitle,
                                label = { Text("Track Title *") },
                                enabled = !state.isLoading,
                                modifier = Modifier.weight(3f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = state.trackNumber,
                                onValueChange = viewModel::updateTrackNumber,
                                label = { Text("Track No.") },
                                enabled = !state.isLoading,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                    }

                    HorizontalDivider()

                    FileSelectionArea(
                        hasFile = state.selectedFile != null,
                        fileName = state.selectedFile?.name,
                        isLoading = state.isLoading,
                        onPickFile = onPickFile,
                        onClearFile = { viewModel.setFile(null) },
                    )

                    Spacer(Modifier.height(8.dp))

                    UploadActionSection(
                        isLoading = state.isLoading,
                        uploadProgress = state.uploadProgress,
                        statusMessage = state.statusMessage,
                        isSuccess = state.isSuccess,
                        onSubmit = viewModel::submitUpload,
                        onDismissMessage = viewModel::dismissMessage,
                    )
                }
            }
        }
    }

    if (showArtistPicker) {
        ArtistPickerDialog(
            artists = state.artists,
            search = state.artistSearch,
            isLoading = state.isLoadingArtists,
            errorMessage = state.artistsError,
            onSearchChange = viewModel::updateArtistSearch,
            onSelect = { artist ->
                viewModel.selectArtist(artist)
                showArtistPicker = false
            },
            onCreateNew = {
                viewModel.clearArtistSelection()
                showArtistPicker = false
            },
            onDismiss = { showArtistPicker = false },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun AlbumSelector(
    selectedAlbum: AlbumRemoteModel?,
    albums: List<AlbumRemoteModel>,
    isLoading: Boolean,
    errorMessage: String?,
    enabled: Boolean,
    onSelect: (AlbumRemoteModel) -> Unit,
    onCreateNew: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = when {
                    isLoading -> "Loading albums..."
                    selectedAlbum != null -> "${selectedAlbum.title} (${selectedAlbum.releaseYear})"
                    else -> "Create new album"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Create new album") },
                onClick = {
                    onCreateNew()
                    expanded = false
                },
            )

            albums.forEach { album ->
                DropdownMenuItem(
                    text = { Text("${album.title} (${album.releaseYear})") },
                    onClick = {
                        onSelect(album)
                        expanded = false
                    },
                )
            }

            if (albums.isEmpty() && !isLoading) {
                DropdownMenuItem(
                    text = { Text("No existing albums") },
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }

    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ArtistPickerDialog(
    artists: List<ArtistRemoteModel>,
    search: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSearchChange: (String) -> Unit,
    onSelect: (ArtistRemoteModel) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select artist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search artists...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    errorMessage != null -> Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )

                    artists.isEmpty() -> Text(
                        text = "No artists found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )

                    else -> LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(artists, key = { it.id }) { artist ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(artist) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                            ) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                val bio = artist.bio
                                if (!bio.isNullOrBlank()) {
                                    Text(
                                        text = bio,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNew) {
                Text("Create new artist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun FileSelectionArea(
    hasFile: Boolean,
    fileName: String?,
    isLoading: Boolean,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    emptyPrompt: String = "Tap to select MP3 / M4A",
    emptyIcon: String = "\u2601\uFE0F",
    selectedIcon: String = "\uD83C\uDFB5",
) {
    val borderColor = MaterialTheme.colorScheme.outline
    val backgroundColor = Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable(enabled = !isLoading) { onPickFile() },
        contentAlignment = Alignment.Center,
    ) {
        if (!hasFile) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = emptyIcon,
                    fontSize = 36.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = emptyPrompt,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = selectedIcon, fontSize = 32.sp)
                Text(
                    text = fileName ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "\u274C",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable(enabled = !isLoading) { onClearFile() }
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun UploadActionSection(
    isLoading: Boolean,
    uploadProgress: Float,
    statusMessage: String?,
    isSuccess: Boolean,
    onSubmit: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading) {
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )
            Text(
                text = statusMessage ?: "Processing...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text("Start Upload Chain")
            }
        }

        if (!isLoading && statusMessage != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = if (isSuccess) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onDismissMessage() },
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
