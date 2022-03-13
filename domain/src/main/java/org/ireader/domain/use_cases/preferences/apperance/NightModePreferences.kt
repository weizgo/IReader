package org.ireader.domain.use_cases.preferences.apperance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.core_ui.theme.UiPreferences
import javax.inject.Inject


class NightModePreferencesUseCase @Inject constructor(
    private val uiPreferences: UiPreferences,
) {
    fun save(mode: ThemeMode) {
        uiPreferences.themeMode().set(mode)
    }

    fun read(): Flow<ThemeMode> = flow {
        emit(uiPreferences.themeMode().get())
    }
}
