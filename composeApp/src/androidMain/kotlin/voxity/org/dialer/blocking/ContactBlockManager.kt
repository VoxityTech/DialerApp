package voxity.org.dialer.blocking

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ContactBlockManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blocked_contacts", Context.MODE_PRIVATE)

    private val _blockedNumbers = MutableStateFlow(getBlockedNumbers())
    val blockedNumbers: StateFlow<Set<String>> = _blockedNumbers.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: ContactBlockManager? = null

        fun getInstance(context: Context): ContactBlockManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactBlockManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun blockNumber(phoneNumber: String): Boolean {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        val currentBlocked = _blockedNumbers.value.toMutableSet()
        currentBlocked.add(normalizedNumber)

        return saveBlockedNumbers(currentBlocked).also { success ->
            if (success) {
                _blockedNumbers.value = currentBlocked
            }
        }
    }

    fun unblockNumber(phoneNumber: String): Boolean {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        val currentBlocked = _blockedNumbers.value.toMutableSet()
        currentBlocked.remove(normalizedNumber)

        return saveBlockedNumbers(currentBlocked).also { success ->
            if (success) {
                _blockedNumbers.value = currentBlocked
            }
        }
    }

    fun isNumberBlocked(phoneNumber: String): Boolean {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        return _blockedNumbers.value.any { blockedNumber ->
            normalizedNumber.contains(blockedNumber) || blockedNumber.contains(normalizedNumber)
        }
    }

    private fun getBlockedNumbers(): Set<String> {
        return prefs.getStringSet("blocked_numbers", emptySet()) ?: emptySet()
    }

    private fun saveBlockedNumbers(blockedNumbers: Set<String>): Boolean {
        return try {
            prefs.edit()
                .putStringSet("blocked_numbers", blockedNumbers)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^\\d]"), "")
            .let { cleaned ->
                // Remove country code if present
                when {
                    cleaned.startsWith("1") && cleaned.length == 11 -> cleaned.substring(1)
                    cleaned.startsWith("91") && cleaned.length == 12 -> cleaned.substring(2)
                    else -> cleaned
                }
            }
    }

    fun getBlockedNumbersList(): List<String> {
        return _blockedNumbers.value.toList()
    }
}