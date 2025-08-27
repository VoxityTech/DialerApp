package voxity.org.dialer.domain.repository

import voxity.org.dialer.domain.models.CallHistoryItem

interface CallHistoryRepository {
    suspend fun getCallHistory(): List<CallHistoryItem>
}