package com.realeyesit.neldemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.realeyesit.nel.EmotionID
import com.realeyesit.nel.ImageHeader
import com.realeyesit.nel.NelTracker
import java.io.File
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<LinearLayout>(R.id.rootLayout)

        val realModelFile = copyModelToRealFile()
        val tracker = NelTracker(realModelFile)

        val header = ImageHeader().apply {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.lena)
            data = bitmap.convertToBuffer()
            format = com.realeyesit.nel.ImageFormat.RGBA
            height = bitmap.height
            width = bitmap.width
            stride = bitmap.width
        }

        val result = tracker.track(header, System.nanoTime()).get()

        result.emotions.forEach { emotionData ->
            val view = TextView(this)
            view.text = "${emotionData.emotionID.name} ${emotionData.probability}"
            view.gravity = Gravity.CENTER
            root.addView(view)
        }
    }

    /**
     * Converts a bitmap into a byte buffer
     * */
    private fun Bitmap.convertToBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(this.width * this.height * 4)
        this.copyPixelsToBuffer(buffer)
        return buffer
    }

    /**
     * This method copies the model file from the assets folder to a real file on the file system.
     * The NEL library requires a model file path as an input, but the
     * assets packed into an APK are not real files on the file system.
     * */
    private fun Context.copyModelToRealFile(): String {
        val modelName = "model.realZ" // this is the name of the model file in the assets folder
        val target = File(filesDir, modelName)
        val cached = target.exists()
        if (!cached) {
            val modelAsset = assets.open(modelName)
            modelAsset.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return target.absolutePath
    }
}