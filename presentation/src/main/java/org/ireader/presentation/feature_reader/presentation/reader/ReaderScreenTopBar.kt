package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.presentation.ToolBar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import tachiyomi.source.Source

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderScreenTopBar(
    isReaderModeEnable: Boolean,
    isLoaded: Boolean,
    modalBottomSheetValue: ModalBottomSheetValue,
    chapter: Chapter?,
    navController: NavController,
    onRefresh: () -> Unit,
    source: Source,
    onWebView: () -> Unit,


    ) {
    if (!isReaderModeEnable && isLoaded && modalBottomSheetValue == ModalBottomSheetValue.Expanded) {
        AnimatedVisibility(
            visible = !isReaderModeEnable && isLoaded,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(700)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(700))
        ) {
            ToolBar(
                modifier = Modifier.systemBarsPadding(),
                title = {
                    Text(
                        text = chapter?.title ?: "",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = 8.dp,
                navigationIcon = {
                    TopAppBarBackButton(navController = navController)
                },
                actions = {
                    if (chapter != null) {
                        AppIconButton(imageVector = Icons.Default.Autorenew,
                            title = "Refresh",
                            onClick = {
                                onRefresh()
                            })
                    }
                    AppIconButton(imageVector = Icons.Default.Public,
                        title = "WebView",
                        onClick = {
                            onWebView()
                        })
                }
            )
        }
    } else if (!isLoaded) {
        ToolBar(
            modifier = Modifier.systemBarsPadding(),
            title = {},
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            actions = {
                if (chapter != null) {
                    AppIconButton(imageVector = Icons.Default.Autorenew,
                        title = "Refresh",
                        onClick = {
                            onRefresh()
                        })
                }
                AppIconButton(imageVector = Icons.Default.Public,
                    title = "WebView",
                    onClick = {
                        onWebView()

                    })
            },
            navigationIcon = {

                TopAppBarBackButton(navController = navController)
            },


            )
    }
}