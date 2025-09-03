package org.voxity.dialer.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import org.voxity.dialer.domain.models.CallResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CallRecordingManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentRecordingFile: File? = null

    suspend fun startRecording(phoneNumber: String): CallResult = withContext(Dispatchers.IO) {
        try {
            if (!hasRecordingPermissions()) {
                return@withContext CallResult.Error("Recording permissions required")
            }

            if (isRecording) {
                return@withContext CallResult.Error("Recording already in progress")
            }

            val recordingFile = createRecordingFile(phoneNumber)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFile.absolutePath)

                prepare()
                start()
            }

            currentRecordingFile = recordingFile
            isRecording = true
            CallResult.Success

        } catch (e: Exception) {
            CallResult.Error("Failed to start recording: ${e.message}", e)
        }
    }

    suspend fun stopRecording(): CallResult = withContext(Dispatchers.IO) {
        try {
            if (!isRecording || mediaRecorder == null) {
                return@withContext CallResult.Error("No recording in progress")
            }

            mediaRecorder?.apply {
                stop()
                release()
            }

            mediaRecorder = null
            isRecording = false

            currentRecordingFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    CallResult.Success
                } else {
                    file.delete()
                    CallResult.Error("Recording file is empty")
                }
            } ?: CallResult.Error("No recording file found")

        } catch (e: Exception) {
            CallResult.Error("Failed to stop recording: ${e.message}", e)
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    private fun hasRecordingPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createRecordingFile(phoneNumber: String): File {
        val recordingsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "CallRecordings"
        )

        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())

        return File(recordingsDir, "call_${phoneNumber}_${timestamp}.3gp")
    }
}