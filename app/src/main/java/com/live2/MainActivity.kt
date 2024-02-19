package com.live2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.live2.databinding.ActivityMainBinding
import com.live2.media.Live2SDK
import com.live2.media.ui.story.StoryView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Live2SDK.installer(application, this).authToken("4564332kfoms5543finvno2n1").install()
        binding.storyView.init(context = this, embedId = "3bfjbg3xvc")
        binding.storyWindowView.init(context = this, embedId = "3xl9ic3lvl")
        binding.carouselView.init(context = this, embedId = "zvk3mycfhd")
    }
}