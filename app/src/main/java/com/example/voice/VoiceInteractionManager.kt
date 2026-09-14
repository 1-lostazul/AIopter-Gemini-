package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.model.MicrophoneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceInteractionManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _micState = MutableStateFlow(MicrophoneState.IDLE)
    val micState: StateFlow<MicrophoneState> = _micState.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onTranscriptionComplete: ((String) -> Unit)? = null

    fun startListening() {
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _micState.value = MicrophoneState.ERROR
                    _errorMessage.value = "Speech recognition is not available on this device"
                    return@post
                }

                stopListening() // Cleanup any previous instance

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _micState.value = MicrophoneState.LISTENING
                            _errorMessage.value = null
                        }

                        override fun onBeginningOfSpeech() {
                            _micState.value = MicrophoneState.LISTENING
                        }

                        override fun onRmsChanged(rmsdB: Float) {}

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _micState.value = MicrophoneState.TRANSCRIBING
                        }

                        override fun onError(error: Int) {
                            val errorDesc = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                else -> "Recognition error (code $error)"
                            }
                            _micState.value = MicrophoneState.ERROR
                            _errorMessage.value = errorDesc
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            _transcription.value = text
                            _micState.value = MicrophoneState.IDLE
                            if (text.isNotBlank()) {
                                onTranscriptionComplete?.invoke(text)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partialText = matches?.firstOrNull() ?: ""
                            _transcription.value = partialText
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                _micState.value = MicrophoneState.LISTENING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _micState.value = MicrophoneState.ERROR
                _errorMessage.value = "Failed to start speech recognition: ${e.message}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
            if (_micState.value != MicrophoneState.ERROR) {
                _micState.value = MicrophoneState.IDLE
            }
        }
    }

    /**
     * Emergency Kill: immediately stop and zero out recognition
     */
    fun resetOnEmergencyKill() {
        stopListening()
        _micState.value = MicrophoneState.IDLE
        _transcription.value = ""
        _errorMessage.value = null
    }
}
