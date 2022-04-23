package org.ireader.presentation.ui

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import coil.Coil
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.LocalTransparentStatusBar
import org.ireader.core_ui.theme.Shapes
import org.ireader.core_ui.theme.Typography


@Composable
fun AppTheme(
    content: @Composable() () -> Unit,
) {
    val vm: AppThemeViewModel = hiltViewModel()
    val (materialColors, customColors) = vm.getColors()
    val rippleTheme = vm.getRippleTheme()
    val systemUiController = rememberSystemUiController()
    val transparentStatusBar = LocalTransparentStatusBar.current.enabled

    LaunchedEffect(customColors.isBarLight, transparentStatusBar) {
        val darkIcons =
            if (transparentStatusBar) materialColors.isLight else customColors.isBarLight
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = darkIcons,
            isNavigationBarContrastEnforced = false
        )
        systemUiController.setNavigationBarColor(
            color = materialColors.background,
            darkIcons = darkIcons,
        )
    }
    Coil.setImageLoader(vm.coilLoader)
    AppColors(
        materialColors = materialColors,
        extraColors = customColors,
        typography = Typography,
        shape = Shapes
    ) {
            CompositionLocalProvider(
                LocalRippleTheme provides rippleTheme,
               // LocalImageLoader provides vm.coilLoader,
                content = content
            )
    }

}