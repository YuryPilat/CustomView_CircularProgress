package com.example.circularprogress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity(), CircularProgress.OnProgressChangedListener {
    private lateinit var textView : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.tv)
        val circularProgress = findViewById<CircularProgress>(R.id.circularProgress)
        circularProgress.setOnProgressChangedListener(this)
    }

    override fun getProgressPercent(percent: Int) {
        textView.text = "$percent%"
    }
}