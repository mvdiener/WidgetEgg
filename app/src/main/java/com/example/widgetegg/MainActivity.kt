package com.example.widgetegg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.widgetegg.ui.theme.WidgetEggTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WidgetEggTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Greeting(name = "Derp")
                    Row(horizontalArrangement = Arrangement.Center) {
                        SignInButton()
                        SignOutButton()
                    }
                }
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
fun Greeting(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Composable
fun SignInButton() {
    Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(Color.Blue)) {
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
