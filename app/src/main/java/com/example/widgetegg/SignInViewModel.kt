package com.example.widgetegg

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    var eid by mutableStateOf("")
        private set

    fun updateEid(input: String) {
        eid = input
    }

    var eidName by mutableStateOf("")
        private set

    private fun updateEidName(input: String) {
        eidName = input
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
            updateHasSubmitted(true)
            updateHasError(false)
            try {
                val result = api.fetchData(eid)
                updateEidName(result.userName)
                updateHasSubmitted(false)
            } catch (e: Exception) {
                updateErrorMessage("Please enter a valid EID!")
                updateHasError(true)
                updateHasSubmitted(false)
            }
        }
    }
}