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
import java.nio.file.Files.createFile


class textActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityTextBinding.inflate(layoutInflater)
    }
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

            val initialDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val uri = Uri.fromFile(initialDirectory)

            val path = File(initialDirectory.toString()+"/text.txt")
            val fileUri = Uri.fromFile(path)

            createDocument(uri,filename,fileUri)
            alterDocument(fileUri,content)

        }
    }


    private fun createDocument(pickerInitialUri: Uri, filename: String, fileUri:Uri){
        if (DocumentsContract.isDocumentUri(this,fileUri)){
            //동일한 이름의 파일이 있으면 삭제 후 생성
            deleteDocument(fileUri)
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/txt"
            putExtra(Intent.EXTRA_TITLE, "text.txt")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI,pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_FILE)
        Toast.makeText(this,"파일 생성 완료",Toast.LENGTH_SHORT).show()
        Log.d("aaa","파일 생성 완료")

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

    private fun deleteDocument(uri:Uri){
        DocumentsContract.deleteDocument(applicationContext.contentResolver, uri)

        Toast.makeText(this,"파일 삭제 완료",Toast.LENGTH_SHORT).show()
        Log.d("aaa","파일 삭제 완료")
    }

}