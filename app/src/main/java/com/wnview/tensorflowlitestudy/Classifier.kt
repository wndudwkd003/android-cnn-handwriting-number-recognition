package com.wnview.tensorflowlitestudy

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Classifier(private val context: Context) {

    private var interpreter: Interpreter
    private var modelInputWidth = 0
    private var modelInputHeight = 0
    private var modelInputChannel = 0
    private var modelOutputClasses = 0

    init {
        val model = loadModel(MODEL_NAME)
        model.order(ByteOrder.nativeOrder())
        interpreter = Interpreter(model)

        initModelShape()
    }

    private fun initModelShape() {
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        modelInputChannel = inputShape[0]
        modelInputWidth = inputShape[1]
        modelInputHeight = inputShape[2]

        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()
        modelOutputClasses = outputShape[1]

    }

    fun classify(bitmap: Bitmap) : Pair<Int, Float> {
        val buffer = convertBitmapToGrayByteBuffer(resizeBitmap(bitmap))
        val result = arrayOf(FloatArray(modelOutputClasses))

        interpreter.run(buffer, result)

        return argmax(result[0])
    }

    private fun argmax(array: FloatArray) : Pair<Int, Float> {
        var argmax = 0
        var max = array[0]

        for(i in 1 until array.size) {
            val f = array[i]
            if (f > max) {
                argmax = i
                max = f
            }
        }

        return Pair(argmax, max)
    }

    private fun resizeBitmap(bitmap: Bitmap) : Bitmap {
        return Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, false)
    }

    private fun convertBitmapToGrayByteBuffer(bitmap: Bitmap) : ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(bitmap.byteCount)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val avgPixelValue = (r + g + b) / 3.0f
            val normalizedPixelValue = avgPixelValue / 255.0f

            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    private fun loadModel(modelName: String) : ByteBuffer {
        val am = context.assets
        val afd = am.openFd(modelName)
        val fis = FileInputStream(afd.fileDescriptor)
        val fc = fis.channel
        val startOffset = afd.startOffset
        val declaredLength = afd.declaredLength
        return fc.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    companion object {
        const val MODEL_NAME = "mlp_model.tflite"
    }

    fun finish() {
        interpreter.close()
    }
}