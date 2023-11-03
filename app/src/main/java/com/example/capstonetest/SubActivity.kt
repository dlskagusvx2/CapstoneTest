package com.example.capstonetest

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonetest.databinding.ActivitySubBinding
import com.example.capstonetest.databinding.ItemRecyclerBinding

class SubActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySubBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data = loadData()
        val customAdapter = CustomAdapter(data)
        binding.recyclerView.adapter = customAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val intent = Intent(this@SubActivity, ProcessActivity::class.java)


        binding.mButton.setOnClickListener {
            startActivity(intent)
            finish()
        }
        /*
        val intent = Intent(this@SubActivity, ProcessActivity::class.java)

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val addButton = findViewById<Button>(R.id.mButton)

        addButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                // 새로운 EditText를 생성합니다.
                val editText = EditText(this@SubActivity)
                // 생성된 EditText를 LinearLayout에 추가합니다.
                linearLayout.addView(editText)
            }
        })

        binding.mButton.setOnClickListener {
            startActivity(intent)
            finish()
        }*/


    }

    //임시 데이터 생성
    fun loadData() : MutableList<adapter>{
        val memoList= mutableListOf<adapter>()
        for (no in 1..3){
            val title ="제목 $no"
            val date =System.currentTimeMillis()
            val memo = adapter(no,title,date)
            memoList.add(memo)
        }
        return memoList
    }
}

class CustomAdapter(val listData:MutableList<adapter>) : RecyclerView.Adapter<CustomAdapter.Holder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        //데이터 꺼내기
        val adapter = listData.get(position)
        //데이터 전달
        holder.setMemo(adapter)
    }

    override fun getItemCount() = listData.size

    inner class Holder(val binding: ItemRecyclerBinding):RecyclerView.ViewHolder(binding.root){
        lateinit var currentMemo:adapter

        init {
            binding.root.setOnClickListener{
                val title = binding.textTitle.text
                Toast.makeText(binding.root.context, "${currentMemo.title}",Toast.LENGTH_SHORT).show()
            }
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun setMemo(adapter: adapter){
            currentMemo = adapter
            with(binding){
                textNo.text = "${adapter.no}"
                textTitle.text =adapter.title

                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val formattedData = sdf.format(adapter.timestamp)
                textDate.text= formattedData
            }
        }

    }
}



