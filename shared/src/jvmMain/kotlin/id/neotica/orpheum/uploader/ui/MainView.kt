package id.neotica.orpheum.uploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.ui.feature.feed.TrackFeedView
import id.neotica.orpheum.uploader.ui.feature.feed.album.AlbumHostView
import id.neotica.orpheum.uploader.ui.feature.upload.UploadView

@Composable
fun MainView(
    onLogout: () -> Unit = {}
) {
    var screenTypeDropdownExpanded by remember { mutableStateOf(false) }
    var moreDropdownExpanded by remember { mutableStateOf(false) }
    var screenType by remember { mutableStateOf(MainScreenType.UPLOADER) }

    MaterialTheme {
        Scaffold(
            topBar = {
                Column {
                    Row {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Neostore Admin",
                                    color = DarkPrimary
                                )
                            },
//                            backgroundColor = DarkBackground,
                            actions = {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .clickable { screenTypeDropdownExpanded = !screenTypeDropdownExpanded }
                                            .border(1.dp, MaterialTheme.colorScheme.primary)
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = screenType.name,
                                                color = DarkPrimary
                                            )
                                            Text(
                                                text = if (screenTypeDropdownExpanded) "⬆️" else "⬇️",
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = screenTypeDropdownExpanded,
                                            onDismissRequest = { screenTypeDropdownExpanded = false }
                                        ) {
                                            MainScreenType.entries.forEach { target ->
                                                DropdownMenuItem(
                                                    text = { Text(target.name) },
                                                    onClick = {
                                                        screenTypeDropdownExpanded = false
                                                        screenType = target
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        Modifier
                                            .border(1.dp, DarkPrimary)
                                            .clickable { moreDropdownExpanded = !moreDropdownExpanded }
                                    ) {
                                        Text(
                                            text = "More ...",
                                            color = DarkPrimary,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                        DropdownMenu(
                                            expanded = moreDropdownExpanded,
                                            onDismissRequest = { moreDropdownExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Logout") },
                                                onClick = {
                                                    moreDropdownExpanded = false
                                                    onLogout()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    HorizontalDivider(Modifier, thickness = 2.dp, color = DarkPrimary)
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(it)
            ) {
                when (screenType) {
                    MainScreenType.UPLOADER -> UploadView()
                    MainScreenType.TRACK_FEED -> TrackFeedView()
                    MainScreenType.ALBUM_MANAGER -> AlbumHostView()
                }
            }
        }
    }
}

enum class MainScreenType {
    UPLOADER,
    TRACK_FEED,
    ALBUM_MANAGER
}