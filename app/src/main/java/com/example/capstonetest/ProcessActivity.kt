package com.example.capstonetest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonetest.databinding.ActivityProcessBinding
import com.example.capstonetest.databinding.ActivitySubBinding

class ProcessActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProcessBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val aaa = Intent(this@ProcessActivity, SubActivity::class.java)



        binding.button1.setOnClickListener {
            startActivity(aaa)
        }
        //intent.putExtra("url",l)
        binding.triangleButton.setOnClickListener {
            val intent = Intent(this@ProcessActivity,MainActivity::class.java)
            val url = binding.urlEditText.text
            //editText의 url을 mainActivity로 전송
            intent.putExtra("url",url.toString())
            startActivity(intent)
            finish()
        }
        //val url = intent.getStringExtra("url")
    }
}