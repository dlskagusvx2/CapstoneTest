package com.example.capstonetest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonetest.databinding.ActivityTextBinding
import java.io.File
import java.io.FileOutputStream


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

            writeTextFile(filename,content)

        }
    }

    private fun writeTextFile(filename: String, content: String){
        val path = "/data/data/com.example.capstonetest/files"
        val files = path+"/text.txt"

        val file: File = File(files)
        try {
            Log.d("파일생성 : ", files)
            val fos = FileOutputStream(file)
            val str = content
            fos.write(str.toByteArray())
            fos.close() //스트림 닫기
            Toast.makeText(this@textActivity, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}