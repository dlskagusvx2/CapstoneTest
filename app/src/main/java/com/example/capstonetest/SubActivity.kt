package com.example.capstonetest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonetest.databinding.ActivitySubBinding

class SubActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySubBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.mButton.setOnClickListener {
            finish()
        }
        val url = intent.getStringExtra("url")
        binding.txt.text = url.toString()
    }

}