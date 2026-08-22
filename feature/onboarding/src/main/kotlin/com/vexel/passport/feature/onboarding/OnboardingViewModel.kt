package com.vexel.passport.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.datastore.PreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: PreferencesStore
) : ViewModel() {

    fun completeOnboarding() = viewModelScope.launch {
        preferences.setOnboardingComplete(true)
    }
}
