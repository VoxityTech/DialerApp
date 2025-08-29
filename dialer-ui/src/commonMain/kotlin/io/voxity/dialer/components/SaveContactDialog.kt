package io.voxity.dialer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveContactDialog(
    phoneNumber: String,
    isVisible: Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var contactName by remember(isVisible) { mutableStateOf("") } // Reset when visibility changes

    if (isVisible) {
        AlertDialog(
            onDismissRequest = {
                contactName = ""
                onDismiss()
            },
            title = { Text("Save Contact") },
            text = {
                Column {
                    Text("Phone Number: $phoneNumber")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (contactName.isNotBlank()) {
                            onSave(contactName.trim())
                            onDismiss()
                        }
                    },
                    enabled = contactName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    contactName = ""
                    onDismiss()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}