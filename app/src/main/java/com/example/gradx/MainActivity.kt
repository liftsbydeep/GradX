package com.example.gradx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)

        val videoUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.instant)
        videoView.setVideoURI(videoUri)

        // Create a MediaController to control video playback
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Start playing the video
        videoView.start()

        // Optional: Enable edge-to-edge display
        enableEdgeToEdge()
        Handler().postDelayed({
            val intent= Intent(this,Login_Page::class.java)

            startActivity(intent)
            finish()},2000  )

    }
}
