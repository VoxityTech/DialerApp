package org.voxity.dialer.blocking

import android.content.Context
import android.content.SharedPreferences
import org.voxity.dialer.domain.models.DialerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactBlockManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blocked_contacts", Context.MODE_PRIVATE)

    private val _blockedNumbers = MutableStateFlow(getBlockedNumbersSync())
    val blockedNumbers: StateFlow<Set<String>> = _blockedNumbers.asStateFlow()

    suspend fun blockNumber(phoneNumber: String): DialerResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val normalizedNumber = normalizePhoneNumber(phoneNumber)
            val currentBlocked = _blockedNumbers.value.toMutableSet()
            currentBlocked.add(normalizedNumber)

            val success = saveBlockedNumbers(currentBlocked)
            if (success) {
                _blockedNumbers.value = currentBlocked
                DialerResult.Success(true)
            } else {
                DialerResult.Error("Failed to save blocked number")
            }
        } catch (e: Exception) {
            DialerResult.Error("Failed to block number", e)
        }
    }

    suspend fun unblockNumber(phoneNumber: String): DialerResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val normalizedNumber = normalizePhoneNumber(phoneNumber)
            val currentBlocked = _blockedNumbers.value.toMutableSet()
            val wasRemoved = currentBlocked.remove(normalizedNumber)

            if (wasRemoved) {
                val success = saveBlockedNumbers(currentBlocked)
                if (success) {
                    _blockedNumbers.value = currentBlocked
                    DialerResult.Success(true)
                } else {
                    DialerResult.Error("Failed to save unblocked number")
                }
            } else {
                DialerResult.Success(false) // Number wasn't blocked
            }
        } catch (e: Exception) {
            DialerResult.Error("Failed to unblock number", e)
        }
    }

    suspend fun isNumberBlocked(phoneNumber: String): DialerResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val normalizedNumber = normalizePhoneNumber(phoneNumber)
            val isBlocked = _blockedNumbers.value.any { blockedNumber ->
                normalizedNumber.contains(blockedNumber) || blockedNumber.contains(normalizedNumber)
            }
            DialerResult.Success(isBlocked)
        } catch (e: Exception) {
            DialerResult.Error("Failed to check if number is blocked", e)
        }
    }

    suspend fun getBlockedNumbers(): DialerResult<List<String>> = withContext(Dispatchers.IO) {
        try {
            DialerResult.Success(_blockedNumbers.value.toList())
        } catch (e: Exception) {
            DialerResult.Error("Failed to get blocked numbers", e)
        }
    }

    private fun getBlockedNumbersSync(): Set<String> {
        return try {
            prefs.getStringSet("blocked_numbers", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveBlockedNumbers(blockedNumbers: Set<String>): Boolean {
        return try {
            prefs.edit()
                .putStringSet("blocked_numbers", blockedNumbers)
                .commit()
        } catch (e: Exception) {
            false
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^\\d]"), "")
            .let { cleaned ->
                when {
                    cleaned.startsWith("1") && cleaned.length == 11 -> cleaned.substring(1)
                    cleaned.startsWith("91") && cleaned.length == 12 -> cleaned.substring(2)
                    else -> cleaned
                }
            }
    }
}