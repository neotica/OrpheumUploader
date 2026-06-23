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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class WidthClass { Compact, Medium, Expanded }

@Composable
fun AdaptiveUploadView(
    viewModel: UploadViewModel,
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

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
                    OutlinedTextField(
                        value = state.artistName,
                        onValueChange = viewModel::updateArtistName,
                        label = { Text("Artist Name *") },
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    if (widthClass == WidthClass.Compact) {
                        OutlinedTextField(
                            value = state.artistBio,
                            onValueChange = viewModel::updateArtistBio,
                            label = { Text("Artist Bio (Optional)") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )
                    } else {
                        OutlinedTextField(
                            value = state.artistBio,
                            onValueChange = viewModel::updateArtistBio,
                            label = { Text("Artist Bio (Optional)") },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                    }

                    HorizontalDivider()

                    SectionHeader("Album Information")
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
private fun FileSelectionArea(
    hasFile: Boolean,
    fileName: String?,
    isLoading: Boolean,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
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
                    text = "\u2601\uFE0F",
                    fontSize = 36.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = "Tap to select MP3 / M4A",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "\uD83C\uDFB5", fontSize = 32.sp)
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
