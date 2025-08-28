package io.voxity.dialer.domain.repository

import io.voxity.dialer.domain.models.CallHistoryItem

interface CallHistoryRepository {
    suspend fun getCallHistory(): List<CallHistoryItem>
}