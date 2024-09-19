package com.example.widgetegg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
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
        EidInput(signInViewModel.eid, signInViewModel)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            SignInButton(
                signInViewModel::getBackupData,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            SignOutButton(
                signInViewModel::signOut,
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
fun EidInput(eid: String, signInViewModel: SignInViewModel) {
    OutlinedTextField(
        value = eid,
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
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp)
    )
}

@Composable
fun SignInButton(getBackupData: () -> Unit, modifier: Modifier) {
    Button(
        onClick = { getBackupData() },
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
fun SignOutButton(signOut: () -> Unit, modifier: Modifier) {
    Button(
        onClick = { signOut() },
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
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(Color.LightGray),
            modifier = Modifier.padding(bottom = 50.dp)
        ) {
            Text("Where do I find my EID?")
        }
    } else {
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(Color.LightGray),
            modifier = Modifier.padding(bottom = 50.dp)
        ) {
            Text("What next?")
        }
    }

}
