package com.aleksandar.novic.tflite

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace

import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.min

class AnimationResponseClassifier private constructor(activity: Activity) {

    private val MAX_NUMBER_OF_RESULTS = 1
    private val DIM_BATCH_SIZE = 1
    private val DIM_PIXEL_SIZE = 3
    private val IMAGE_MEAN = 1.0f
    private val IMAGE_STD = 127.5f
    private val intValues = IntArray(224 * 224)
    private val tfliteOptions = Interpreter.Options()
    private var tfliteModel: MappedByteBuffer?
    private var labels: List<String>

    var imageData: ByteBuffer
    private var tflite: Interpreter?

    private var labelProbArray: Array<FloatArray>? = null

    init {
        tfliteModel = loadModelFile(activity)
        tfliteOptions.setNumThreads(1)
        tflite = Interpreter(tfliteModel!!, tfliteOptions)
        labels = loadLabelList(activity)
        imageData = ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE
                    * 224
                    * 224
                    * DIM_PIXEL_SIZE
                    * 4
        )
        imageData.order(ByteOrder.nativeOrder())
        labelProbArray = Array(1){ FloatArray(labels.size) }
    }

    companion object {
        fun create(activity: Activity): AnimationResponseClassifier =
            AnimationResponseClassifier(activity)
    }

    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd("mobilenet_v1_1.0_224.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imageData.rewind()
        bitmap.getPixels(
            intValues,
            0,
            bitmap.getWidth(),
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight()
        )
        // Convert the image to floating point.
        var pixel = 0
        val startTime = SystemClock.uptimeMillis()
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val `val` = intValues[pixel++]
                addPixelValue(`val`)
            }
        }
    }

    private fun loadLabelList(activity: Activity): List<String> {
        val labels = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(activity.assets.open("labels.txt")))
        var line: String? = reader.readLine()
        while (line != null) {
            labels.add(line)
            line = reader.readLine()
        }
        reader.close()
        return labels
    }

    private fun addPixelValue(pixelValue: Int) {
        imageData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        imageData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        imageData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
    }

    fun close() {
        if (tflite != null) {
            tflite?.close()
            tflite = null
        }
        tfliteModel = null
    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")

        Trace.beginSection("preprocessBitmap")
        convertBitmapToByteBuffer(bitmap)
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("runInference")
        val startTime = SystemClock.uptimeMillis()
        tflite?.run(imageData, labelProbArray)
        val endTime = SystemClock.uptimeMillis()
        Trace.endSection()

        // Find the best classifications.
        val pq = PriorityQueue(
            3,
            Comparator<Recognition> { lhs, rhs ->
                // Intentionally reversed to put high confidence at the head of the queue.
                (rhs.confidence).compareTo(lhs.confidence)
            })
        for (i in labels.indices) {
            pq.add(
                Recognition(
                    "" + i,
                    if (labels.size > i) labels[i] else "unknown",
                    getNormalizedProbability(i),
                    null
                )
            )
        }
        val recognitions = ArrayList<Recognition>()
        val recognitionsSize = min(pq.size, MAX_NUMBER_OF_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        Trace.endSection()
        return recognitions
    }

    private fun getNormalizedProbability(labelIndex: Int): Float {
        return labelProbArray?.let {
            it[0][labelIndex]
        } ?: 0f
    }
}

data class Recognition(
    val id: String,
    val title: String,
    val confidence: Float,
    val location: RectF?
)