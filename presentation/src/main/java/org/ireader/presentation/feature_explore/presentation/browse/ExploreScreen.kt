package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.ExperimentalPagingApi
import kotlinx.coroutines.launch
import org.ireader.domain.FetchType
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.LayoutType
import org.ireader.presentation.feature_explore.presentation.browse.viewmodel.ExploreViewModel
import org.ireader.presentation.feature_library.presentation.components.LayoutComposable
import org.ireader.presentation.presentation.components.showLoading
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.SmallTextComposable
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.WebViewScreenSpec
import tachiyomi.source.CatalogSource
import tachiyomi.source.HttpSource
import tachiyomi.source.Source
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@ExperimentalPagingApi
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    vm: ExploreViewModel,
    source: CatalogSource,
    onFilterClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onWebView: () -> Unit,
    onPop: () -> Unit,
    onLayoutTypeSelect: (DisplayMode) -> Unit,
    currentLayout: LayoutType,
    getBooks: (query: String?, listing: Listing?, filters: List<Filter<*>>) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    //val books = vm.books.collectAsLazyPagingItems()

    val gridState = rememberLazyGridState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        vm.modifiedFilter = source.getFilters()
    }

    val scaffoldState = rememberScaffoldState()
    val (showSnackBar, setShowSnackBar) = remember {
        mutableStateOf(false)
    }
    val (snackBarText, setSnackBarText) = remember {
        mutableStateOf("")
    }
    LaunchedEffect(key1 = vm.stateItems) {
        //vm.insertBooks(vm.stateItems)
    }
    if (showSnackBar) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            val result = scaffoldState.snackbarHostState.showSnackbar(
                snackBarText,
                actionLabel = "Reload",
                duration = SnackbarDuration.Indefinite
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    setShowSnackBar(false)
                    vm.endReached = false
                    vm.loadItems()
                    //books.retry()
                }
            }
        }
    }
    LaunchedEffect(key1 = vm.error != null) {
        val errors = vm.error
        if (errors != null && errors.asString(context).isNotBlank() && vm.page > 1) {
            setShowSnackBar(true)
            setSnackBarText(errors.asString(context))

        }
    }
    ModalBottomSheetLayout(
        modifier = Modifier.statusBarsPadding(),
        sheetState = bottomSheetState,
        sheetContent = {
            FilterBottomSheet(
                onApply = {
                    val mFilters = vm.modifiedFilter.filterNot { it.isDefaultValue() }
                    vm.stateFilters = mFilters
                    vm.searchQuery = null
                    vm.loadItems(true)
                    //vm.getBooks(filters = mFilters, source = source)
                },
                filters = vm.modifiedFilter,
                onReset = {
                    vm.modifiedFilter = source.getFilters()
                },
                onUpdate = {
                    vm.modifiedFilter = it
                }
            )
        },
        sheetBackgroundColor = MaterialTheme.colors.background,

        ) {
        Scaffold(
            topBar = {
                BrowseTopAppBar(
                    state = vm,
                    source = source,
                    onValueChange = onValueChange,
                    onSearch = onSearch,
                    onSearchDisable = onSearchDisable,
                    onSearchEnable = onSearchEnable,
                    onWebView = onWebView,
                    onPop = onPop,
                    onLayoutTypeSelect = onLayoutTypeSelect,
                    currentLayout = currentLayout
                )
            },
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(hostState = it) { data ->
                    Snackbar(
                        actionColor = MaterialTheme.colors.primary,
                        snackbarData = data,
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        MidSizeTextComposable(
                            text = stringResource(org.ireader.presentation.R.string.filter),
                            color = Color.White
                        )
                    },
                    onClick = {
                        scope.launch {
                            bottomSheetState.show()
                        }
                    },
                    icon = {
                        Icon(Icons.Filled.Add, "", tint = MaterialTheme.colors.onSecondary)
                    }
                )
            },
        ) { paddingValue ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)) {
                when {
                    vm.isLoading && vm.page == 1 -> {
                        showLoading()
                    }
                    vm.error != null && vm.page == 1 -> {
                        ExploreScreenErrorComposable(
                            error = vm.error!!.asString(context),
                            source = source,
                            onRefresh = { getBooks(null, null, emptyList()) },
                            onWebView = {
                                navController.navigate(WebViewScreenSpec.buildRoute(
                                    sourceId = source.id,
                                    fetchType = FetchType.LatestFetchType.index,
                                    url = it.baseUrl
                                )
                                )
                            }
                        )
                    }
                    else -> {
                        LayoutComposable(
                            books = vm.stateItems,
                            layout = vm.layout,
                            scrollState = scrollState,
                            source = source,
                            navController = navController,
                            isLocal = false,
                            gridState = gridState,
                            onClick = { book ->
                                navController.navigate(
                                    route = BookDetailScreenSpec.buildRoute(sourceId = book.sourceId,
                                        bookId = book.id)
                                )
                            },
                            isLoading = vm.isLoading,
                            onEndReachValidator = { index ->
                                if (index >= vm.stateItems.lastIndex && !vm.endReached && !vm.isLoading) {
                                    vm.loadItems()
                                }
                            }
                        )
                    }
                }

            }


        }
    }

}

@Composable
private fun BoxScope.ExploreScreenErrorComposable(
    modifier: Modifier = Modifier,
    error: String,
    onRefresh: () -> Unit,
    source: Source,
    onWebView: (HttpSource) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ErrorTextWithEmojis(
            error = error,
            modifier = Modifier
                .padding(20.dp)
        )
        Row(Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier
                .weight(.5f)
                .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIconButton(imageVector = Icons.Default.Refresh,
                    title = "Retry",
                    onClick = {

                        onRefresh()
                    })
                SmallTextComposable(text = "Retry")
            }
            Column(Modifier
                .weight(.5f)
                .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (source is HttpSource) {
                    AppIconButton(imageVector = Icons.Default.Public,
                        title = "Open in WebView",
                        onClick = {
                            onWebView(source)
                        })
                }
                SmallTextComposable(text = "Open in WebView")
            }

        }

    }
}
