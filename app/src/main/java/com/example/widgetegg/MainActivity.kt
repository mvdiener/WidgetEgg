package com.example.widgetegg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.widgetegg.ui.theme.WidgetEggTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WidgetEggTheme {
                SignInContent()
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    HelpButton()
                }
            }
        }
    }
}

@Composable
fun SignInContent() {
    val signInViewModel = viewModel<SignInViewModel>()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Greeting(signInViewModel.eidName)
        EidInput(signInViewModel.eid, signInViewModel)
        Row(horizontalArrangement = Arrangement.Center) {
            SignInButton(signInViewModel::updateEidName)
            SignOutButton()
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(
        text = if (name.isBlank()) "Enter your EID:" else "Welcome, $name!",
    )
}

@Composable
fun EidInput(eid: String, signInViewModel: SignInViewModel) {
    OutlinedTextField(
        value = eid,
        onValueChange = { signInViewModel.updateEid(it) },
        shape = CircleShape,
        textStyle = TextStyle(color = Color.Black),
        placeholder = {
            Text(
                "EI0000000000000000"
            )
        },
    )
}

@Composable
fun SignInButton(updateEidName: () -> Unit) {
    Button(
        onClick = { updateEidName() },
        colors = ButtonDefaults.buttonColors(Color.Blue)
    ) {
        Text("Submit EID")
    }
}

@Composable
fun SignOutButton() {
    Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(Color.Red)) {
        Text("Sign Out")
    }
}

@Composable
fun HelpButton() {
    Button(
        onClick = { /*TODO*/ },
        colors = ButtonDefaults.buttonColors(Color.LightGray),
    ) {
        Text("Where do I find my EID?")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WidgetEggTheme {
        Greeting("Android")
    }
}
