package com.widgetegg.widgeteggapp.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.Routes
import tools.utilities.getAsset
import user.preferences.PreferencesDatastore

@Composable
fun MainScreen(navController: NavController) {
    val signInViewModel = viewModel<SignInViewModel>()
    val context = LocalContext.current
    val packageName = context.packageName
    val logoString = if (isSystemInDarkTheme()) {
        "logo-dark-mode"
    } else {
        "logo-light-mode"
    }

    val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    signInViewModel.updateIsBackgroundUsageDisabled(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            am.isBackgroundRestricted
        } else {
            false
        }
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        signInViewModel.updateIsBackgroundUsageDisabled(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                am.isBackgroundRestricted
            } else {
                false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 25.dp, top = 50.dp)
            .semantics { contentDescription = "Main Screen" },
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            Icons.Rounded.Settings,
            contentDescription = "Settings",
            modifier = Modifier.clickable {
                navController.navigate(Routes.settingsScreen)
            })
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val assetManager = LocalContext.current.assets
        val bitmapImage =
            BitmapFactory.decodeStream(getAsset(assetManager, "icons/$logoString.png"))
                .asImageBitmap()

        if (signInViewModel.isBackgroundUsageDisabled) {
            BackgroundUsageContent(signInViewModel, packageName, context)
        }
        Image(
            bitmap = bitmapImage,
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 30.dp)
        )
        SignInContent(signInViewModel)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        HelpButton(signInViewModel)
    }
    SignOutDialog(signInViewModel)
    FindMyEidDialog(signInViewModel)
    WhatNextDialog(signInViewModel)
}

@Composable
fun BackgroundUsageContent(
    signInViewModel: SignInViewModel,
    packageName: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
            .clickable {
                signInViewModel.updateShowBackgroundUsageDialog(true)
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Warning,
            contentDescription = "Background usage warning",
            modifier = Modifier
                .padding(end = 5.dp)
                .size(35.dp),
            tint = Color(0xffffa500)
        )
        Text(text = "Background Usage Disabled")
        Icon(
            Icons.Rounded.Info,
            contentDescription = "Background usage info",
            modifier = Modifier
                .padding(start = 5.dp)
                .size(15.dp)
        )
        BackgroundUsageDialog(signInViewModel, packageName, context)
    }
}

@Composable
fun BackgroundUsageDialog(
    signInViewModel: SignInViewModel,
    packageName: String,
    context: Context
) {
    if (signInViewModel.showBackgroundUsageDialog) {
        Dialog(
            onDismissRequest = {
                signInViewModel.updateShowBackgroundUsageDialog(false)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(size = 16.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text =
                        """
                        The widgets attempt to update automatically in the background every 15 minutes. In order to do this, they run background processes to fetch data.     
                        
                        With background usage disabled, the widgets will not update unless you update manually by tapping any widget.
                        
                        If you would like the widgets to update automatically, enable background usage within the battery settings for this app.
                    """.trimIndent()
                )
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 5.dp),
                    onClick = {
                        signInViewModel.updateShowBackgroundUsageDialog(false)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = "package:$packageName".toUri()
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = "App Settings")
                }
            }
        }
    }
}

@Composable
fun SignInContent(signInViewModel: SignInViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val preferences = PreferencesDatastore(context)

        val prefEiUsername = preferences.getEiUserName()
        if (prefEiUsername.isNotBlank()) signInViewModel.updateEiUserName(prefEiUsername)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
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
            .semantics { contentDescription = "EID Input" }
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
    val modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, bottom = 80.dp, end = 16.dp)
    if (signInViewModel.eiUserName.isBlank()) {
        Button(
            onClick = { signInViewModel.updateShowFindMyEidDialog(true) },
            modifier = modifier
        ) {
            Text("Where do I find my EID?")
        }
    } else {
        Button(
            onClick = { signInViewModel.updateShowWhatNextDialog(true) },
            modifier = modifier
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
                "3. Open the Settings menu by pressing the gear icon.",
                """4. Open "Privacy & Data".""",
                "5. Copy your EID (EI...) from the bottom.",
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
                """3. Search for "WidgetEgg", long press a widget and drag to the desired location.""",
                "4. Once added, the widget will automatically update every 15 minutes.",
                "5. Check the settings gear at the top right of the app to configure your widgets."
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
