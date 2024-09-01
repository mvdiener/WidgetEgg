package com.example.widgetegg

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import user.preferences.PreferencesDatastore


class SignInViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences: PreferencesDatastore

    init {
        val context = getApplication<Application>().applicationContext
        preferences = PreferencesDatastore(context)
    }

    var eid by mutableStateOf("")
        private set

    fun updateEid(input: String) {
        eid = input
    }

    var eiUserName by mutableStateOf("")
        private set

    fun updateEiUserName(input: String) {
        eiUserName = input
    }

    var errorMessage by mutableStateOf("")
        private set

    private fun updateErrorMessage(input: String) {
        errorMessage = input
    }

    var hasError by mutableStateOf(false)
        private set

    private fun updateHasError(input: Boolean) {
        hasError = input
    }

    var hasSubmitted by mutableStateOf(false)
        private set

    private fun updateHasSubmitted(input: Boolean) {
        hasSubmitted = input
    }

    fun getBackupData() {
        viewModelScope.launch(Dispatchers.IO) {
            val basicRequestInfo = api.getBasicRequestInfo(eid)
            updateHasSubmitted(true)
            updateHasError(false)
            try {
                val result = api.fetchBackup(basicRequestInfo)
                updateEiUserName(result.userName)
                preferences.saveEiUserName(result.userName)
                preferences.saveEid(eid)
                updateHasSubmitted(false)
            } catch (e: Exception) {
                updateErrorMessage("Please enter a valid EID!")
                updateHasError(true)
                updateHasSubmitted(false)
            }
        }
    }
}