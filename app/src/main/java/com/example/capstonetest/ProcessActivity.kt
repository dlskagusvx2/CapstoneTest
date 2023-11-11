package com.example.capstonetest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.example.capstonetest.databinding.ActivityProcessBinding
import com.example.capstonetest.databinding.ActivitySubBinding

class ProcessActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProcessBinding.inflate(layoutInflater)
    }
    val l = "https://www.youtube.com/watch?v=If95bdcptEM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val intent = Intent(this@ProcessActivity,MainActivity::class.java)
        val aaa = Intent(this@ProcessActivity, SubActivity::class.java)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        binding.button1.setOnClickListener {
            startActivity(aaa)
        }
        //intent.putExtra("url",l)
        binding.triangleButton.setOnClickListener {
            startActivity(intent)
            finish()
        }
        //val url = intent.getStringExtra("url")
    }
}