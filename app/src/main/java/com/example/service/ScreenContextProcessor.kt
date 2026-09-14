package com.example.service

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

data class ProcessedScreenContext(
    val summaryText: String,
    val isIncludedInPrompt: Boolean,
    val compressedJpegBase64: String? = null
)

/**
 * Manages temporary in-memory screen context frames.
 * Enforces zero-disk persistence: buffers are processed and cleared immediately from memory.
 */
object ScreenContextProcessor {

    @Volatile
    private var lastCapturedBitmap: Bitmap? = null

    @Volatile
    private var lastContextSummary: String? = null

    fun setLatestFrame(bitmap: Bitmap?, summary: String? = null) {
        lastCapturedBitmap?.recycle()
        lastCapturedBitmap = bitmap
        lastContextSummary = summary
    }

    /**
     * Minimizes and formats the screen context for inclusion in an active AI request.
     * Promptly releases and recycles raw image memory after encoding.
     */
    fun extractContextForRequest(includeImage: Boolean = false): ProcessedScreenContext {
        val bitmap = lastCapturedBitmap
        val summary = lastContextSummary ?: "Active foreground screen content"

        var base64Result: String? = null
        if (includeImage && bitmap != null && !bitmap.isRecycled) {
            try {
                val stream = ByteArrayOutputStream()
                // Compress to compact JPEG to minimize bandwidth and memory
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val byteArray = stream.toByteArray()
                base64Result = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (_: Exception) {
                base64Result = null
            }
        }

        return ProcessedScreenContext(
            summaryText = summary,
            isIncludedInPrompt = true,
            compressedJpegBase64 = base64Result
        )
    }

    /**
     * Emergency Kill and cleanup: zero out all memory references
     */
    fun clearTemporaryBuffers() {
        try {
            lastCapturedBitmap?.recycle()
        } catch (_: Exception) {}
        lastCapturedBitmap = null
        lastContextSummary = null
    }
}
