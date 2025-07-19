package dev.noyex.notayex

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.noyex.notayex.theme.ProjectNTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hızlı başlangıç için UI'yi hemen göster
        setContent {
            ProjectNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    showDialog: Boolean = false
) {
    var passwordState by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }
    var isFirstRun by remember { mutableStateOf(true) }
    var storedPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current
    var openDialog by remember { mutableStateOf(showDialog) }

    // Async olarak SharedPreferences yükle
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val firstRun = sharedPreferences.getBoolean("isFirstRun", true)
            val password = sharedPreferences.getString("password", "") ?: ""

            // Main thread'e dön ve state'leri güncelle
            withContext(Dispatchers.Main) {
                isFirstRun = firstRun
                storedPassword = password
                isLoading = false
            }
        }
    }

    // Loading durumunda basit bir loading göster
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    val isSettingPassword = isFirstRun
    val loginTitle = if (isSettingPassword) {
        stringResource(R.string.set_password_title)
    } else {
        stringResource(R.string.login_title)
    }
    val buttonText = if (isSettingPassword) {
        stringResource(R.string.set_password_button)
    } else {
        stringResource(R.string.login_button)
    }
    val scrollState = rememberScrollState()

    // Toast mesajları için string kaynakları
    val passwordSetSuccess = stringResource(R.string.password_set_success)
    val passwordEmptyError = stringResource(R.string.password_empty_error)
    val passwordIncorrect = stringResource(R.string.password_incorrect)

    // Login işlemini optimize et
    fun handleLogin() {
        val activity = context as? Activity ?: return

        if (isSettingPassword) {
            if (passwordState.text.isNotEmpty()) {
                // Background thread'de SharedPreferences güncelle
                Thread {
                    val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("password", passwordState.text)
                    editor.putBoolean("isFirstRun", false)
                    editor.apply()
                }.start()

                Toast.makeText(context, passwordSetSuccess, Toast.LENGTH_SHORT).show()
                // Ana sayfaya geç
                context.startActivity(Intent(context, NotePageActivity::class.java))
                activity.finish()
            } else {
                Toast.makeText(context, passwordEmptyError, Toast.LENGTH_SHORT).show()
            }
        } else {
            if (passwordState.text == storedPassword) {
                context.startActivity(Intent(context, NotePageActivity::class.java))
                activity.finish()
            } else {
                Toast.makeText(context, passwordIncorrect, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TopAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.onBackground),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        IconButton(
                            onClick = { openDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            )

            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = loginTitle,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    OutlinedTextField(
                        value = passwordState,
                        onValueChange = { passwordState = it },
                        label = { Text(stringResource(R.string.password_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = { handleLogin() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                            cursorColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                            focusedTextColor = MaterialTheme.colorScheme.secondary,
                            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                        )
                    )
                    Button(
                        onClick = { handleLogin() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(buttonText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // About dialog
        if (openDialog) {
            AlertDialog(
                onDismissRequest = { openDialog = false },
                title = {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Made by nyaex",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val annotatedText = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                pushStringAnnotation(
                                    tag = "URL",
                                    annotation = "https://github.com/nyaexx"
                                )
                                append("GitHub Profilim")
                                pop()
                            }
                        }

                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(
                                    tag = "URL",
                                    start = offset,
                                    end = offset
                                ).firstOrNull()?.let { annotation ->
                                    uriHandler.openUri(annotation.item)
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { openDialog = false }
                    ) {
                        Text(
                            text = "OK",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenPreview() {
    ProjectNTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun LoginScreenPreview2() {
    ProjectNTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DialogDarkPreview() {
    ProjectNTheme {
        LoginScreen(showDialog = true)
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DialogLightPreview() {
    ProjectNTheme {
        LoginScreen(showDialog = true)
    }
}