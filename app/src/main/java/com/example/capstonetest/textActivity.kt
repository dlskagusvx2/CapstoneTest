package com.example.capstonetest

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.capstonetest.databinding.ActivityMainBinding
import com.example.capstonetest.databinding.ActivityTextBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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
        binding.exportBtn.setOnClickListener {
            val content = binding.textContent.text.toString()
            val filename = binding.textTitle.text.toString()

        }
    }


}