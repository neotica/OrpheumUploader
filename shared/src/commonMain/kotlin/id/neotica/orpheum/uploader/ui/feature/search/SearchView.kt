package id.neotica.orpheum.uploader.ui.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.ui.feature.feed.TrackCompactItem
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchView(
    playbackViewModel: PlaybackViewModel,
    viewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentTrack by playbackViewModel.currentTrack.collectAsState()
    val listState = rememberLazyListState()

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

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search Tracks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = DarkPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChanged,
            placeholder = { Text("Search by title or artist...", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = DarkPrimary,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading && state.results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.errorMessage != null && state.results.isEmpty()) {
            Text(
                "Error: ${state.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (!state.isLoading && state.query.isNotBlank() && state.results.isEmpty()) {
            Text(
                "No results found",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.results, key = { it.id }) { track ->
                    val isActive = track.id == currentTrack?.id

                    TrackCompactItem(
                        track = track,
                        isActive = isActive,
                        onClick = { viewModel.playTrack(track) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                if (state.isLoading && state.results.isNotEmpty()) {
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
}
