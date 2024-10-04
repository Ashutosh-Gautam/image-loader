package com.example.image_loader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URL
import java.nio.ByteBuffer

class AsyncImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var sourceValue: Deferred<Bitmap>? = null
    private val urlMap = mutableMapOf<String, Bitmap>()
    private val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
        println(throwable.message + "pop")
    })
    private val mutex = Mutex()

    suspend fun setUrl(url: String) {
        val bitmap = urlMap[url]
        if (bitmap != null) {
            setImageBitmap(bitmap)
        } else {
            val bitmapCreated = loadBitmap(url)
            bitmapCreated?.let {
                setImageBitmap(it)
                mutex.withLock { urlMap[url] = it }
            }
        }
    }

    private fun <T> retry(times: Int, block: () -> T): T {
        var attempt = 0
        while (attempt < times) {
            try {
                return block()
            } catch (e: Exception) {
                println(e.message + "yay")
                attempt++
                if (attempt >= times) throw e
            }
        }
        throw Exception("Max retries reached")
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        if (sourceValue?.isActive == true) {
            sourceValue?.cancel()
        }

        sourceValue = scope.async {
            retry(3) {
                getBitmapImage(url)
            }
        }

        return try {
            sourceValue?.await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println(e.message + "yo")
            null
        }
    }

    private fun getBitmapImage(url: String): Bitmap {
        val data = URL(url).readBytes()
        val source = ImageDecoder.createSource(ByteBuffer.wrap(data))
        return ImageDecoder.decodeBitmap(source)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }
}
