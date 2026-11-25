package com.fitpulse.ai.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    fun saveImageFromUri(context: Context, uri: Uri, fileNamePrefix: String): String? {
        return try {
            val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
            val outFile = File(photosDir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")

            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = inputStream.use { BitmapFactory.decodeStream(it) } ?: return null

            val maxDim = 1024
            val scale = minOf(1f, maxDim.toFloat() / maxOf(original.width, original.height).coerceAtLeast(1))
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt().coerceAtLeast(1),
                    (original.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                original
            }

            FileOutputStream(outFile).use { fos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 88, fos)
            }
            if (scaled !== original) scaled.recycle()
            original.recycle()

            outFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadImageBitmap(path: String): ImageBitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return runCatching {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }.getOrNull()
    }
}
