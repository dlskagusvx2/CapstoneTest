package com.example.capstonetest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonetest.databinding.ActivityMainBinding
import com.example.capstonetest.databinding.ActivityTextBinding


class textActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityTextBinding.inflate(layoutInflater)
    }
    val l = "https://www.youtube.com/watch?v=If95bdcptEM"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val title = intent.getStringExtra("title")
        binding.textTitle.text = title
        binding.textContent.text = intent.getStringExtra("content")
        val intent = Intent(this@textActivity, SubActivity::class.java)
        binding.mButton.setOnClickListener {

            startActivity(intent)
            finish()
        }
        val url = intent.getStringExtra("url")

    }
}