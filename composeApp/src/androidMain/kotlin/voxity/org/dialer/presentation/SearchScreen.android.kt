package voxity.org.dialer.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import voxity.org.dialer.data.ContactsReader
import voxity.org.dialer.data.CallLogReader
import voxity.org.dialer.data.Contact
import voxity.org.dialer.data.CallLogItem
import voxity.org.dialer.domain.usecases.CallUseCases

sealed class SearchResult {
    data class ContactResult(val contact: Contact) : SearchResult()
    data class CallHistoryResult(val callLog: CallLogItem) : SearchResult()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SearchScreen(
    onBack: () -> Unit,
    callUseCases: CallUseCases,
    modifier: Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            val results = performSearch(context, searchQuery)
            searchResults = results
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search contacts and call history") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { /* Handle search submit */ }
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }
        }

        // Search results
        when {
            isSearching -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            searchQuery.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Start typing to search contacts and call history")
                }
            }
            searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found for \"$searchQuery\"")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { result ->
                        when (result) {
                            is SearchResult.ContactResult -> {
                                SearchContactItem(
                                    contact = result.contact,
                                    searchQuery = searchQuery,
                                    onClick = {
                                        result.contact.phoneNumbers.firstOrNull()?.let {
                                            callUseCases.makeCall(it)
                                        }
                                    }
                                )
                            }
                            is SearchResult.CallHistoryResult -> {
                                SearchCallHistoryItem(
                                    callLog = result.callLog,
                                    searchQuery = searchQuery,
                                    onClick = {
                                        callUseCases.makeCall(result.callLog.phoneNumber)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContactItem(
    contact: Contact,
    searchQuery: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightText(contact.name, searchQuery),
                    style = MaterialTheme.typography.bodyLarge
                )
                contact.phoneNumbers.firstOrNull()?.let { number ->
                    Text(
                        text = number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Icon(
                Icons.Default.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCallHistoryItem(
    callLog: CallLogItem,
    searchQuery: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (callLog.callType) {
                    "INCOMING" -> Icons.Default.CallReceived
                    "OUTGOING" -> Icons.Default.CallMade
                    "MISSED" -> Icons.Default.CallMissed
                    else -> Icons.Default.History
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (callLog.callType) {
                    "INCOMING" -> androidx.compose.ui.graphics.Color.Green
                    "OUTGOING" -> androidx.compose.ui.graphics.Color.Blue
                    "MISSED" -> androidx.compose.ui.graphics.Color.Red
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightText(
                        callLog.contactName.ifEmpty { callLog.phoneNumber },
                        searchQuery
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${callLog.date} • ${callLog.callType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private suspend fun performSearch(context: Context, query: String): List<SearchResult> {
    val results = mutableListOf<SearchResult>()

    // Search contacts
    val contacts = ContactsReader(context).getContacts()
    val matchingContacts = contacts.filter { contact ->
        contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumbers.any { it.contains(query) }
    }
    results.addAll(matchingContacts.map { SearchResult.ContactResult(it) })

    // Search call history
    val callHistory = CallLogReader(context).getCallHistory()
    val matchingCallHistory = callHistory.filter { callLog ->
        callLog.contactName.contains(query, ignoreCase = true) ||
                callLog.phoneNumber.contains(query)
    }
    results.addAll(matchingCallHistory.map { SearchResult.CallHistoryResult(it) })

    return results.sortedBy {
        when (it) {
            is SearchResult.ContactResult -> it.contact.name
            is SearchResult.CallHistoryResult -> it.callLog.contactName.ifEmpty { it.callLog.phoneNumber }
        }
    }
}

private fun highlightText(text: String, query: String): String {
    // Simple highlighting - in a real app you might use AnnotatedString
    return text // For now, return plain text - implement highlighting as needed
}