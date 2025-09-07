package org.voxity.dialer.domain.repository

import org.voxity.dialer.domain.models.CallHistoryItem

interface CallHistoryRepository {
    suspend fun getCallHistory(): List<CallHistoryItem>
}