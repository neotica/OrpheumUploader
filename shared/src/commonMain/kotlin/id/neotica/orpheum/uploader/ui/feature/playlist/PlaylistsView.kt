package id.neotica.orpheum.uploader.ui.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.domain.model.playlist.PlaylistRemoteModel
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlaylistsView(
    onPlaylistClick: (String) -> Unit,
    viewModel: PlaylistsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.hasMore && !state.isLoading) {
            viewModel.loadMore()
        }
    }

    LaunchedEffect(state.createSuccess) {
        if (state.createSuccess) {
            showCreateDialog = false
            viewModel.consumeCreateResult()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Playlists",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DarkPrimary
            )

            Button(onClick = { showCreateDialog = true }) {
                Text("+ New")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading && state.playlists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.errorMessage != null && state.playlists.isEmpty()) {
            Text(
                "Error: ${state.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (state.playlists.isEmpty()) {
            Text(
                "No playlists yet. Create one to get started.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.playlists, key = { it.id }) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist.slug) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                if (state.isLoading && state.playlists.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            isCreating = state.isCreating,
            errorMessage = state.createError,
            onDismiss = {
                showCreateDialog = false
                viewModel.consumeCreateResult()
            },
            onCreate = { title, description, isPublic ->
                viewModel.createPlaylist(title, description, isPublic)
            }
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: PlaylistRemoteModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\uD83C\uDFA7",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playlist.description
                    ?: if (playlist.isPublic) "Public playlist" else "Private playlist",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = if (playlist.isPublic) "\uD83C\uDF10" else "\uD83D\uDD12",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    isCreating: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, isPublic: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = { Text("New playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        enabled = !isCreating
                    )
                    Text("Public playlist")
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(title, description, isPublic) },
                enabled = !isCreating && title.isNotBlank()
            ) {
                Text(if (isCreating) "Creating..." else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCreating) {
                Text("Cancel")
            }
        }
    )
}
