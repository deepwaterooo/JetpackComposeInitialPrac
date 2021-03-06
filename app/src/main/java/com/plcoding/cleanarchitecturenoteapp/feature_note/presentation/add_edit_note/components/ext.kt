package com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.util

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine {
    continuation ->
        ProcessCameraProvider.getInstance(this).also {
            future ->
                future.addListener(
                    {
                        continuation.resume(future.get())
                    },
                    executor
                )
        }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

// 这里的这个方法它说找不到。。。。。。
suspend fun ImageCapture.takePicture(executor: Executor): File {
    val photoFile = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            Log.d("test takePicture", "runCatching create image.jpg file")
            File.createTempFile("image", "jpg")
        }.getOrElse { ex ->
                          Log.d("test takePicture ex: ", ex.toString())
                          Log.e("TakePicture", "Failed to create temporary file", ex)
                      File("/dev/null")
        }
    }

    return suspendCoroutine {
        continuation ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        takePicture(
            outputOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    continuation.resume(photoFile)
                }

                override fun onError(ex: ImageCaptureException) {
                    Log.e("TakePicture", "Image capture failed", ex)
                    continuation.resumeWithException(ex)
                }
            }
        )
    }
}
