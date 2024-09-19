package com.example.widgetegg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.widgetegg.ui.theme.WidgetEggTheme
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WidgetEggTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val signInViewModel = viewModel<SignInViewModel>()
                    SignInContent(signInViewModel)
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        HelpButton(signInViewModel)
                    }
                    SignOutDialog(signInViewModel)
                    FindMyEidDialog(signInViewModel)
                    WhatNextDialog(signInViewModel)
                }

            }
        }
    }
}

@Composable
fun SignInContent(signInViewModel: SignInViewModel) {
    val context = LocalContext.current
    runBlocking {
        val preferences = PreferencesDatastore(context)

        val prefEiUsername = preferences.getEiUserName()
        if (prefEiUsername.isNotBlank()) signInViewModel.updateEiUserName(prefEiUsername)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (signInViewModel.hasSubmitted) {
            LoadingMessage()
        } else if (signInViewModel.hasError) {
            Error(signInViewModel.errorMessage)
        } else {
            Greeting(signInViewModel.eiUserName)
        }
        EidInput(signInViewModel)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            SignInButton(
                signInViewModel,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            SignOutButton(
                signInViewModel,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = if (name.isBlank()) "Enter your EID:" else "Welcome, $name!")
}

@Composable
fun LoadingMessage() {
    Text(text = "Checking...")
}

@Composable
fun Error(message: String) {
    Text(text = message)
}

@Composable
fun EidInput(signInViewModel: SignInViewModel) {
    OutlinedTextField(
        value = signInViewModel.eid,
        onValueChange = { signInViewModel.updateEid(it) },
        shape = CircleShape,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        ),
        placeholder = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EI0000000000000000",
                    color = Color.LightGray
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp)
    )
}

@Composable
fun SignInButton(signInViewModel: SignInViewModel, modifier: Modifier) {
    Button(
        onClick = {
            if (signInViewModel.eiUserName.isBlank()) {
                signInViewModel.getBackupData()
            }
        },
        colors = ButtonDefaults.buttonColors(Color.Blue),
        modifier = modifier
    ) {
        Text(
            text = "Submit EID",
            style = TextStyle(color = Color.White)
        )
    }
}

@Composable
fun SignOutButton(signInViewModel: SignInViewModel, modifier: Modifier) {
    Button(
        onClick = {
            if (signInViewModel.eiUserName.isNotBlank()) {
                signInViewModel.updateShowSignoutConfirmDialog(true)
            }
        },
        colors = ButtonDefaults.buttonColors(Color.Red),
        modifier = modifier
    ) {
        Text(
            text = "Sign Out",
            style = TextStyle(color = Color.White)
        )
    }
}

@Composable
fun HelpButton(signInViewModel: SignInViewModel) {
    if (signInViewModel.eiUserName.isBlank()) {
        Button(
            onClick = { signInViewModel.updateShowFindMyEidDialog(true) },
            modifier = Modifier.padding(bottom = 50.dp)
        ) {
            Text("Where do I find my EID?")
        }
    } else {
        Button(
            onClick = { signInViewModel.updateShowWhatNextDialog(true) },
            modifier = Modifier.padding(bottom = 50.dp)
        ) {
            Text("What next?")
        }
    }
}

@Composable
fun SignOutDialog(signInViewModel: SignInViewModel) {
    if (signInViewModel.showSignoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                signInViewModel.updateShowSignoutConfirmDialog(false)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { Text(text = "Are you sure?") }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            signInViewModel.updateShowSignoutConfirmDialog(false)
                            signInViewModel.signOut()
                        }
                    ) {
                        Text(
                            text = "Confirm"
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun FindMyEidDialog(signInViewModel: SignInViewModel) {
    if (signInViewModel.showFindMyEidDialog) {
        Dialog(
            onDismissRequest = {
                signInViewModel.updateShowFindMyEidDialog(false)
            }
        ) {
            val instructions = listOf(
                "1. Open Egg, Inc.",
                "2. Open the Settings menu by pressing the nine dots at the bottom.",
                "3. Open the Help menu by pressing the question mark icon.",
                """4. Select "Data Loss Issue".""",
                "5. Copy your EID (EI...) from the subject line.",
                "6. Open WidgetEgg.",
                """7. Paste your EID into the text box and press "Submit EID"."""
            )
            val length = instructions.size
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(size = 16.dp)
                    )
                    .padding(20.dp)
            ) {
                instructions.forEachIndexed { index, item ->
                    Text(
                        text = item,
                        modifier = Modifier.padding(0.dp, 10.dp)
                    )
                    if (index != length - 1) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun WhatNextDialog(signInViewModel: SignInViewModel) {
    if (signInViewModel.showWhatNextDialog) {
        Dialog(
            onDismissRequest = {
                signInViewModel.updateShowWhatNextDialog(false)
            }
        ) {
            val instructions = listOf(
                "1. Long press on your home screen to begin editing.",
                """2. Select the "Widgets" option.""",
                """3. Search for "WidgetEgg", select a widget and press the "Add" button.""",
                "4. It may take up to 30 minutes for your data to appear in the widget."
            )
            val length = instructions.size
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(size = 16.dp)
                    )
                    .padding(20.dp)
            ) {
                instructions.forEachIndexed { index, item ->
                    Text(
                        text = item,
                        modifier = Modifier.padding(0.dp, 10.dp)
                    )
                    if (index != length - 1) HorizontalDivider()
                }
            }
        }
    }
}
