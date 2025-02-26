package ireader.presentation.ui.settings.appearance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.Theme
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.ColorPickerDialog
import ireader.presentation.ui.component.components.ColorPickerInfo
import ireader.presentation.ui.component.components.ColorPreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.LazyColumnWithInsets
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.isLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingScreen(
        modifier: Modifier = Modifier,
        onPopBackStack: () -> Unit,
        saveDarkModePreference: (PreferenceValues.ThemeMode) -> Unit,
        vm: AppearanceViewModel,
        scaffoldPaddingValues: PaddingValues,
        onColorChange: () -> Unit,
        onColorReset: () -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val customizedColors = vm.getCustomizedColors()
    val systemTheme = isSystemInDarkTheme()
    val isLight = remember(vm.themeMode.value) {
        if (vm.themeMode.value == PreferenceValues.ThemeMode.System) {
            !systemTheme
        } else {
            vm.themeMode.value == PreferenceValues.ThemeMode.Light
        }
    }

    val scope = rememberCoroutineScope()
    val themesForCurrentMode = remember(vm.themeMode.value, vm.vmThemes.size, isLight) {
        if (isLight)
            vm.vmThemes.filter { !it.isDark }
        else
            vm.vmThemes.filter { it.isDark }
    }
    var showColorDialog = remember {
        mutableStateOf(false)
    }
    var colorPickerInfo by remember {
        mutableStateOf(ColorPickerInfo())
    }

    LazyColumnWithInsets(scaffoldPaddingValues) {
        item {
            Components.Header(
                    text = "Theme",
            ).Build()
        }
        item {
            Components.Dynamic {
                ChoicePreference<PreferenceValues.ThemeMode>(
                        preference = vm.themeMode,
                        choices = mapOf(
                                PreferenceValues.ThemeMode.System to localizeHelper.localize(MR.strings.follow_system_settings),
                                PreferenceValues.ThemeMode.Light to localizeHelper.localize(MR.strings.light),
                                PreferenceValues.ThemeMode.Dark to localizeHelper.localize(MR.strings.dark)
                        ),
                        title = localizeHelper.localize(MR.strings.theme),
                        subtitle = null,
                        onValue = {
                            vm.saveNightModePreferences(it)
                        }
                )
            }.Build()
        }
        item {
            Components.Header(
                    text = "Preset themes",
            ).Build()
        }
        item {
            Components.Dynamic {
                LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(items = themesForCurrentMode) { theme ->
                        ThemeItem(
                                theme,
                                onClick = { theme ->
                                    vm.colorTheme.value = theme.id
                                    customizedColors.primaryState.value = theme.materialColors.primary
                                    customizedColors.secondaryState.value = theme.materialColors.secondary
                                    customizedColors.barsState.value = theme.extraColors.bars
                                    vm.isSavable = false
                                },
                                isSelected = vm.colorTheme.value == theme.id,
                        )
                    }
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                        preference = customizedColors.primaryState,
                        title = "Color primary",
                        subtitle = "Displayed most frequently across your app",
                        unsetColor = MaterialTheme.colorScheme.primary,
                        onChangeColor = onColorChange,
                        onRestToDefault = onColorReset,
                        showColorDialog = showColorDialog,
                        onShow = {
                            colorPickerInfo = it
                        }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                        preference = customizedColors.secondaryState,
                        title = "Color secondary",
                        subtitle = "Accents select parts of the UI",
                        unsetColor = MaterialTheme.colorScheme.secondary,
                        onChangeColor = onColorChange,
                        onRestToDefault = onColorReset,
                        showColorDialog = showColorDialog,
                        onShow = {
                            colorPickerInfo = it
                        }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                        preference = customizedColors.barsState,
                        title = "Toolbar color",
                        unsetColor = AppColors.current.bars,
                        onChangeColor = onColorChange,
                        onRestToDefault = onColorReset,
                        showColorDialog = showColorDialog,
                        onShow = {
                            colorPickerInfo = it
                        }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    if (vm.isSavable) {
                        TextButton(onClick = {
                            vm.isSavable = false
                            scope.launchIO {
                                val theme = vm.getThemes(vm.colorTheme.value, isLight)
                                if (theme != null) {
                                    scope.launchIO {
                                        val themeId =
                                                vm.themeRepository.insert(theme.toCustomTheme())
                                        vm.colorTheme.value = themeId
                                        vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_saved))
                                    }
                                } else {
                                    vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_not_valid))
                                }
                                vm.isSavable = false
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize(MR.strings.save_custom_theme))
                        }
                    } else if (vm.colorTheme.value > 0) {
                        TextButton(onClick = {
                            scope.launchIO {
                                scope.launch {
                                    vm.vmThemes.find { it.id == vm.colorTheme.value }
                                            ?.toCustomTheme()
                                            ?.let { vm.themeRepository.delete(it) }
                                }
                                vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_deleted))
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize(MR.strings.delete_custom_theme))
                        }
                    }
                }
            }.Build()
        }
        item {
            Components.Header(
                    text = "Timestamp",
            ).Build()
        }
        item {
            Components.Dynamic {
                ChoicePreference<PreferenceValues.RelativeTime>(
                        preference = vm.relativeTime,
                        choices = vm.relativeTimes.associateWith { value ->
                            when (value) {
                                PreferenceValues.RelativeTime.Off -> localizeHelper.localize(MR.strings.off)
                                PreferenceValues.RelativeTime.Day -> localizeHelper.localize(MR.strings.pref_relative_time_short)
                                PreferenceValues.RelativeTime.Week -> localizeHelper.localize(MR.strings.pref_relative_time_long)
                                else -> localizeHelper.localize(MR.strings.off)
                            }
                        },
                        title = localizeHelper.localize(MR.strings.pref_relative_format),
                        subtitle = null,
                )
            }.Build()
        }


    }
    if (showColorDialog.value) {
        ColorPickerDialog(
                title = { Text(colorPickerInfo.title ?: "") },
                onDismissRequest = { showColorDialog.value = false },
                onSelected = {
                    colorPickerInfo.preference?.value = it
                    showColorDialog.value = false
                    colorPickerInfo.onChangeColor()
                },
                initialColor = colorPickerInfo.initialColor,
        )
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ThemeItem(
        theme: Theme,
        onClick: (Theme) -> Unit,
        onLongClick: (Theme) -> Unit = {},
        isSelected: Boolean = false,
) {
    val borders = MaterialTheme.shapes.small
    val borderColor = remember {
        if (theme.materialColors.isLight()) {
            Color.Black.copy(alpha = 0.25f)
        } else {
            Color.White.copy(alpha = 0.15f)
        }
    }
    Surface(
            tonalElevation = 4.dp, color = theme.materialColors.background, shape = borders,
            modifier = Modifier
                    .size(100.dp, 160.dp)
                    .padding(8.dp)
                    .border(1.dp, borderColor, borders)
                    .combinedClickable(onClick = { onClick(theme) }, onLongClick = { onLongClick(theme) })
    ) {
        Box {
            Column(
                    modifier = Modifier.border(1.dp, borderColor, borders)
            ) {
                Toolbar(
                        modifier = Modifier.requiredHeight(24.dp), title = {},
                        backgroundColor = theme.extraColors.bars
                )
                Box(
                        Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(6.dp)
                ) {
                    Text("Text", fontSize = 11.sp, color = theme.materialColors.onBackground)
                    Button(
                            onClick = { onClick(theme) },
                            enabled = true,
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .size(40.dp, 20.dp),
                            content = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.materialColors.primary
                            )
                    )
                    Surface(
                            modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd),
                            shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                            color = theme.materialColors.secondary,
                            tonalElevation = 6.dp,
                            content = { }
                    )
                }
                BottomAppBar(
                        modifier = Modifier.requiredHeight(24.dp),
                        containerColor = theme.extraColors.bars
                ) {
                }
            }
            if (isSelected) {
                Icon(
                        modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(30.dp)
                                .padding(2.dp),
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "theme is selected",
                        tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
