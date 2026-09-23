package io.github.ackeecz.gradle.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.ackee.sample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
