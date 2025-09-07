package org.voxity.dialer

import android.content.Context
import org.voxity.dialer.core.DialerCore
import org.voxity.dialer.di.androidCoreModule
import org.voxity.dialer.di.commonModule
import org.voxity.dialer.domain.models.DialerConfig
import org.voxity.dialer.domain.models.CallResult
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDialerCore private constructor() : DialerCore {

    private var isInitialized = false
    private var currentConfig: DialerConfig? = null

    companion object {
        @Volatile
        private var INSTANCE: AndroidDialerCore? = null

        fun getInstance(): AndroidDialerCore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidDialerCore().also { INSTANCE = it }
            }
        }
    }

    override suspend fun initialize(config: DialerConfig): CallResult = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) {
                return@withContext CallResult.Error("DialerCore is already initialized")
            }

            currentConfig = config
            isInitialized = true
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to initialize DialerCore", e)
        }
    }

    override suspend fun shutdown(): CallResult = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized) {
                return@withContext CallResult.Error("DialerCore is not initialized")
            }

            try {
                stopKoin()
            } catch (e: Exception) {
                // Koin might not be started, ignore
            }

            currentConfig = null
            isInitialized = false
            INSTANCE = null
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to shutdown DialerCore", e)
        }
    }

    override fun isInitialized(): Boolean = isInitialized
}

object DialerCoreManager {
    fun initialize(context: Context, config: DialerConfig = DialerConfig()): CallResult {
        return try {
            initializeDialerCore(context)
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to initialize dialer core", e)
        }
    }

    val coreModules: List<Module> get() = listOf(
        commonModule,
        androidCoreModule
    )
}

actual fun initializeDialerCore(context: Any) {
    require(context is Context) { "Expected Android Context" }
}

actual val dialerAndroidModules: List<Module> = listOf(androidCoreModule)