package com.example.image_loader

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val asyncImageView = findViewById<AsyncImageView>(R.id.asyncImageView)
        val handler = CoroutineExceptionHandler { _, throwable ->
            println("yayyay" + throwable.message)
        }
        lifecycleScope.launch(handler) {
            asyncImageView.setUrl(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Aurora_amaz%C3%B4nica_no_Lago_do_Cuni%C3%A3.jpg/1280px-Aurora_amaz%C3%B4nica_no_Lago_do_Cuni%C3%A3.jpg"
            )
        }
    }
}