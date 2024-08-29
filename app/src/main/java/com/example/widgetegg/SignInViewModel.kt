package com.example.widgetegg

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SignInViewModel : ViewModel() {
    var eid by mutableStateOf("")
        private set

    fun updateEid(input: String) {
        eid = input
    }

    var eidName by mutableStateOf("")
        private set

    fun updateEidName() {
        eidName = eid
    }
}