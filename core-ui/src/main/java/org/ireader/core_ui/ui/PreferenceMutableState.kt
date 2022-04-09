package org.ireader.core_ui.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tachiyomi.core.prefs.Preference

class PreferenceMutableState<T>(
    private val preference: Preference<T>,
    scope: CoroutineScope,
) : MutableState<T> {

    private val state = mutableStateOf(preference.get())

    init {
        scope.launch(Dispatchers.Main) {
            preference.changes()
                .collectLatest { state.value = it }
        }
    }

    override var value: T
        get() = state.value
        set(value) {
            preference.set(value)
        }

    override fun component1(): T {
        return state.value
    }

    override fun component2(): (T) -> Unit {
        return { preference.set(it) }
    }

}

fun <T> Preference<T>.asStateIn(scope: CoroutineScope): PreferenceMutableState<T> {
    return PreferenceMutableState(this, scope)
}
