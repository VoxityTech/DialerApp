package io.voxity.dialer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.voxity.dialer.domain.models.CallHistoryItem
import io.voxity.dialer.domain.models.CallType
import io.voxity.dialer.domain.models.Contact

sealed class SearchResult {
    data class ContactResult(val contact: Contact) : SearchResult()
    data class CallHistoryResult(val callLog: CallHistoryItem) : SearchResult()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    contacts: List<Contact>,
    callHistory: List<CallHistoryItem>,
    onBack: () -> Unit,
    onCallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }

    LaunchedEffect(searchQuery, contacts, callHistory) {
        if (searchQuery.length >= 2) {
            val results = performSearch(contacts, callHistory, searchQuery)
            searchResults = results
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

        when {
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
                                    onClick = {
                                        result.contact.phoneNumbers.firstOrNull()?.let {
                                            onCallClick(it)
                                        }
                                    }
                                )
                            }
                            is SearchResult.CallHistoryResult -> {
                                SearchCallHistoryItem(
                                    callLog = result.callLog,
                                    onClick = {
                                        onCallClick(result.callLog.phoneNumber)
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
                    text = contact.name,
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
    callLog: CallHistoryItem,
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
                    CallType.INCOMING -> Icons.Default.CallReceived
                    CallType.OUTGOING -> Icons.Default.CallMade
                    CallType.MISSED -> Icons.Default.CallMissed
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (callLog.callType) {
                    CallType.INCOMING -> Color.Green
                    CallType.OUTGOING -> Color.Blue
                    CallType.MISSED -> Color.Red
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = callLog.contactName.ifEmpty { callLog.phoneNumber },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${callLog.timestamp} • ${callLog.callType}",
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

private fun performSearch(
    contacts: List<Contact>,
    callHistory: List<CallHistoryItem>,
    query: String
): List<SearchResult> {
    val results = mutableListOf<SearchResult>()

    val matchingContacts = contacts.filter { contact ->
        contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumbers.any { it.contains(query) }
    }
    results.addAll(matchingContacts.map { SearchResult.ContactResult(it) })

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