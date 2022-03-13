package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.domain.FetchType
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.view_models.explore.ExploreScreenEvents
import org.ireader.domain.view_models.explore.ExploreViewModel
import org.ireader.presentation.feature_explore.presentation.browse.ExploreScreen
import org.ireader.presentation.presentation.EmptyScreenComposable
import tachiyomi.source.HttpSource

object ExploreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "explore_route/{sourceId}?query={query}"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.query,
        NavigationArgs.sourceId
    )


    fun buildRoute(sourceId: Long, query: String? = null): String {
        return if (query != null) {
            "explore_route/$sourceId?query=$query"
        } else {
            "explore_route/$sourceId"
        }
    }

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        androidx.paging.ExperimentalPagingApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm: ExploreViewModel = hiltViewModel()
        val focusManager = LocalFocusManager.current
        val state = vm
        val source = vm.source
        val scope = rememberCoroutineScope()
        if (source != null) {
            ExploreScreen(
                navController = navController,
                vm = vm,
                onFilterClick = {
                    vm.toggleFilterMode()
                },
                source = source,
                onPop = { navController.popBackStack() },
                currentLayout = vm.layout,
                onLayoutTypeSelect = { layout ->
                    vm.onEvent(ExploreScreenEvents.OnLayoutTypeChnage(
                        layoutType = layout))
                },
                onSearch = {
                    if (state.searchQuery.isNotBlank())
                        vm.getBooks(
                            query = state.searchQuery,
                            source = source
                        ) else {
                        vm.getBooks(listing = source.getListings().first(), source = source)
                        scope.launch {
                            vm.showSnackBar(UiText.StringResource(R.string.query_must_not_be_empty))
                        }
                    }
                    focusManager.clearFocus()
                },
                onSearchDisable = {
                    vm.onEvent(ExploreScreenEvents.ToggleSearchMode(false))
                },
                onSearchEnable = {
                    vm.onEvent(ExploreScreenEvents.ToggleSearchMode(true))
                },
                onValueChange = {
                    vm.onEvent(ExploreScreenEvents.OnQueryChange(it))
                },
                onWebView = {
                    navController.navigate(WebViewScreenSpec.buildRoute(
                        sourceId = source.id,
                        fetchType = FetchType.LatestFetchType.index,
                        url = (source as HttpSource).baseUrl
                    ))
                },
                getBooks = { query, listing, filters ->
                    vm.getBooks(source = source,
                        query = query,
                        listing = listing,
                        filters = filters)
                }
            )
        } else {
            EmptyScreenComposable(navController, R.string.source_not_available)
        }
    }

}
