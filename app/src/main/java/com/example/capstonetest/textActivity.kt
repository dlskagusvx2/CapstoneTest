package com.example.capstonetest

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonetest.databinding.ActivityTextBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class textActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityTextBinding.inflate(layoutInflater)
    }
    val l = "https://www.youtube.com/watch?v=If95bdcptEM"
    val CREATE_FILE = 1
    //val contentResolver = applicationContext.contentResolver


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

            //writeTextFile(filename,content)
            val initialDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val uri = Uri.fromFile(initialDirectory)
            createFile(uri,filename)
            val path = File(initialDirectory.toString()+"/text.txt")
            val fileUri = Uri.fromFile(path)
            alterDocument(fileUri,content)

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
    private fun createFile(pickerInitialUri: Uri, filename: String){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/txt"
            putExtra(Intent.EXTRA_TITLE, "text.txt")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI,pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    private fun alterDocument(uri:Uri,content:String){
        try {
            contentResolver.openFileDescriptor(uri,"w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                        content.toByteArray()
                    )
                }
            }
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }


}