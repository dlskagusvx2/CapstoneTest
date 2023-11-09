package com.example.capstonetest

import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonetest.databinding.ActivitySubBinding
import com.example.capstonetest.databinding.ItemRecyclerBinding
import com.example.capstonetest.db.AppDatabase
import com.example.capstonetest.db.SummaryDao
import com.example.capstonetest.db.SummaryEntity

class SubActivity : AppCompatActivity(),OnItemLongClickListener {
    private val binding by lazy {
        ActivitySubBinding.inflate(layoutInflater)
    }
    private lateinit var db:AppDatabase
    private lateinit var SummaryDao:SummaryDao
    private lateinit var SummaryList: ArrayList<SummaryEntity>
    private lateinit var adapter: CustomAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = Intent(this@SubActivity, ProcessActivity::class.java)

        binding.mButton.setOnClickListener {
            startActivity(intent)
            finish()
        }

        db = AppDatabase.getInstance(this)!!
        SummaryDao = db.getSummaryDao()
        getAllSummaryList()

        binding.swipe.setOnRefreshListener {
            getAllSummaryList()
            binding.swipe.isRefreshing = false
        }

    }

    override fun onRestart() {
        super.onRestart()
        getAllSummaryList()
    }

    private fun getAllSummaryList(){
        Thread{
            SummaryList = ArrayList(SummaryDao.getAll())
            setRecyclerView()
        }.start()
    }

    private fun setRecyclerView(){
        runOnUiThread {
            adapter = CustomAdapter(SummaryList,this)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
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

    override fun onLongClick(position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("액션 선택")
            .setNegativeButton("삭제",DialogInterface.OnClickListener { dialog, which ->
                deleteSummary(position)
            })
            .setPositiveButton("취소",DialogInterface.OnClickListener { dialog, which ->

            })
        builder.show()

    }

    private fun deleteSummary(position: Int){
        Thread{
            SummaryDao.deleteSummary(SummaryList[position])
            SummaryList.removeAt(position)
            runOnUiThread{
                Toast.makeText(this,"삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }.start()
        getAllSummaryList()
    }
}

class CustomAdapter(val SummaryList:ArrayList<SummaryEntity>,
                    private val listener: OnItemLongClickListener) : RecyclerView.Adapter<CustomAdapter.Holder>(){
    var content:String = ""
    //lateinit var contentList:ArrayList<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,false)
        return Holder(binding)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        /*
        //데이터 꺼내기
        val adapter = listData.get(position)
        //데이터 전달
        //holder.setMemo(adapter)
        */
        val SummaryData = SummaryList[position]

        holder.summaryTitle.text = SummaryData.title
        holder.summaryContent.text = SummaryData.summary
        holder.root.setOnLongClickListener{
            listener.onLongClick(position)
            false
        }
    }

    override fun getItemCount() = SummaryList.size

    inner class Holder(binding: ItemRecyclerBinding):RecyclerView.ViewHolder(binding.root){
        var summaryTitle = binding.summaryTitle
        var summaryContent = binding.content
        var root = binding.root

        init {
            binding.root.setOnClickListener{
                val intent = Intent(binding.root.context, textActivity::class.java)
                // 선택한 아이템의 제목 정보를 인텐트에 추가
                intent.putExtra("title",summaryTitle.text)
                intent.putExtra("content",summaryContent.text)
                binding.root.context.startActivity(intent)
            }
        }

    }
}



