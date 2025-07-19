package dev.noyex.notayex

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.noyex.notayex.theme.ProjectNTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }
    var notes by remember { mutableStateOf(getNotes(prefs)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var currentNoteText by remember { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var oldPassword by remember { mutableStateOf(TextFieldValue("")) }
    var newPassword by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Notlarınız",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Filled.Settings,
                                contentDescription = "Ayarlar",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Not Ekle")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            onClick = {
                                selectedNote = note
                                currentNoteText = note.content
                                showEditDialog = true
                            }
                        ) {
                            Text(
                                text = note.content,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Yeni not ekleme dialog'u
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Yeni Not") },
                text = {
                    OutlinedTextField(
                        value = currentNoteText,
                        onValueChange = { currentNoteText = it },
                        label = { Text("Not içeriği") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (currentNoteText.isNotEmpty()) {
                            val newNote = Note(
                                id = notes.size,
                                content = currentNoteText
                            )
                            notes = notes + newNote
                            saveNotes(prefs, notes)
                            currentNoteText = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        currentNoteText = ""
                        showAddDialog = false
                    }) {
                        Text("İptal")
                    }
                }
            )
        }

        // Not düzenleme/silme dialog'u
        if (showEditDialog && selectedNote != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    selectedNote = null
                    currentNoteText = ""
                },
                title = { Text("Notu Düzenle") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = currentNoteText,
                            onValueChange = { currentNoteText = it },
                            label = { Text("Not içeriği") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Silme butonu
                        TextButton(
                            onClick = {
                                notes = notes.filter { it.id != selectedNote?.id }
                                saveNotes(prefs, notes)
                                showEditDialog = false
                                selectedNote = null
                                currentNoteText = ""
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Sil")
                        }
                        // Güncelleme butonu
                        TextButton(
                            onClick = {
                                if (currentNoteText.isNotEmpty()) {
                                    val updatedNote = selectedNote?.copy(content = currentNoteText)
                                    if (updatedNote != null) {
                                        notes = notes.map { if (it.id == updatedNote.id) updatedNote else it }
                                        saveNotes(prefs, notes)
                                    }
                                    showEditDialog = false
                                    selectedNote = null
                                    currentNoteText = ""
                                }
                            }
                        ) {
                            Text("Güncelle")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditDialog = false
                        selectedNote = null
                        currentNoteText = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }

        // Ayarlar dialog'u
        if (showSettingsDialog) {

            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Şifre Değiştir") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Eski Şifre") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Yeni Şifre") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val storedPassword = prefs.getString("password", "")
                        if (oldPassword.text == storedPassword) {
                            if (newPassword.text.isNotEmpty()) {
                                val editor = prefs.edit()
                                editor.putString("password", newPassword.text)
                                editor.apply()
                                showSettingsDialog = false
                                Toast.makeText(context, "Şifre Değiştirildi", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Yeni şifre boş olamaz", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Eski şifre yanlış", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}

class NotePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotePage()
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true,uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotePagePreview() {
    ProjectNTheme {
        NotePage()
    }
}
@Preview(showBackground = true, showSystemUi = true,uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun NotePagePreview2() {
    ProjectNTheme {
        NotePage()
    }
}


private fun getNotes(prefs: SharedPreferences): List<Note> {
    val notesJson = prefs.getString("notes", "[]")
    val type = object : TypeToken<List<Note>>() {}.type
    return Gson().fromJson(notesJson, type)
}

private fun saveNotes(prefs: SharedPreferences, notes: List<Note>) {
    val notesJson = Gson().toJson(notes)
    prefs.edit().putString("notes", notesJson).apply()
}
