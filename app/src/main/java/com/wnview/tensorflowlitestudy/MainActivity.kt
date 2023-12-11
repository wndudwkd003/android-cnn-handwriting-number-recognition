package com.wnview.tensorflowlitestudy

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wnview.tensorflowlitestudy.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.drawView.setStrokeWidth(40.0f)
        viewBinding.drawView.setBackgroundColor(Color.BLACK)
        viewBinding.drawView.setColor(Color.WHITE)

        viewBinding.btnClassify.setOnClickListener {
            val bitmap = viewBinding.drawView.getBitmap()

            val res = classifier.classify(bitmap)
            val outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f)
            viewBinding.textView.text = outStr
        }

        viewBinding.btnClear.setOnClickListener {
            viewBinding.drawView.clearCanvas()
        }

        classifier = Classifier(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.finish()
    }
}