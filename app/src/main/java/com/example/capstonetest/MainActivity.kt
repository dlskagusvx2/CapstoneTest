package com.example.capstonetest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.GONE
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.capstonetest.databinding.ActivityMainBinding
import com.example.capstonetest.db.AppDatabase
import com.example.capstonetest.db.SummaryDao
import com.example.capstonetest.db.SummaryEntity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var webView: WebView
    val apiKey = ""// api 키 입력해야함
    val endpoint = "https://api.openai.com/v1/chat/completions"
    val model = "gpt-3.5-turbo" // 사용할 모델 (GPT-3 Turbo)

    var scriptSummary: String = ""
    var scriptTitle:String = ""

    lateinit var db: AppDatabase
    lateinit var summaryDao: SummaryDao
    /*
    val db:AppDatabase = AppDatabase.getInstance(this)!!
    val SummaryDao: SummaryDao = db.getSummaryDao()*/



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        webView = binding.webView

        // WebView 설정
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화
        webSettings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3" // User Agent 설정


        // 페이지를 원하는 스케일로 로드
        webView.setInitialScale(80); // 80% 스케일로 페이지 로드
        webView.webChromeClient = WebChromeClient()


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 페이지 로딩이 완료되면 5초 기다린 후 JavaScript 코드를 실행
                Handler(Looper.getMainLooper()).postDelayed({
                    runJavaScriptCode(
                        """
                (function() {
                    // id가 description-inner인 요소를 찾아서 클릭
                    var descriptionInner = document.getElementById('description-inner');
                    if (descriptionInner) {
                        descriptionInner.click();
                    }
                    // 스크립트 표시 버튼을 찾아서 클릭
                    var  buttonElement= document.getElementById('primary-button').querySelector('button');
                    buttonElement.click();
                    
                    setTimeout(function() {
                        // 영상의 제목 추출
                        var titleBox = document.getElementById('above-the-fold').querySelector('h1');
                        var titleText = titleBox.textContent||titleBox.innerText;
                        
                        // segment-text.style-scope ytd-transcript-segment-renderer 요소를 찾아서 추출
                        var elementsInterval = setInterval(function() {
                            var element = document.getElementById('segments-container');
                            var divElements = element.getElementsByTagName('div');
                            var textContents = [];
                            
                            if (divElements.length > 0) {
                                for (var i = 0; i < divElements.length; i++) {
                                    textContents.push(divElements[i].textContent || divElements[i].innerText);
                                }
                                if (textContents.length > 0) {
                                    var allText = textContents.join('');
                                    var pattern = /\b\d{1,3}:\d{2}\b/g;
                                    var modifiedString = allText.replace(pattern, '').replace(/\n/g, '');
                                    titleText = titleText.replace(pattern, '').replace(/\n/g, '');
                                    window.android.onTextExtracted(titleText,modifiedString); // 결과를 Android 앱에 전달
                                    clearInterval(elementsInterval); // setInterval 중지
                                }
                            }
                        }, 1000); // 1초마다 요소를 체크
                    },3000);
                    
                })();
                """
                    )
                }, 3000) // 3초 지연
            }
        }

        // 웹 페이지 로드
        //스크립트의 언어를 한국어를 디폴트로 설정하기 위해 URL에 ?cc_lang_pref=ko&cc_load_policy=1 추가
        webView.loadUrl("https://www.youtube.com/watch?v=Bgq2Pr_xYg4?cc_lang_pref=ko&cc_load_policy=1")

        // WebView에서 JavaScript 코드 실행 결과를 처리하는 인터페이스
        webView.addJavascriptInterface(this, "android")

        db = AppDatabase.getInstance(this)!!
        summaryDao = db.getSummaryDao()
        val intent = Intent(this@MainActivity,SubActivity::class.java)


        binding.mButton.setOnClickListener {

            Thread{
                summaryDao.insertSummary(SummaryEntity(null,scriptTitle,scriptSummary))
                //summaryDao.insertSummary(SummaryEntity(null,"title","스크립트"))
            }.start()

            startActivity(intent)
            finish()
        }
    }

    /*
    private fun askToChatGPT(q: String) {
        Toast.makeText(this@MainActivity,"askToChatGPT함수 시작",Toast.LENGTH_SHORT).show()
        val messagesArray = JsonArray()
        val userMessage = JsonObject()
        userMessage.addProperty("role", "user")
        userMessage.addProperty("content", "${q} 유튜브 영상 스크립트인데 요약해줘.") // 사용자 메시지를 추가
        messagesArray.add(userMessage)

        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // 연결 시간 초과 설정
            .readTimeout(300, TimeUnit.SECONDS)    // 읽기 시간 초과 설정
            .build()
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = JsonObject()
        requestBody.add("messages", messagesArray)
        requestBody.addProperty("model", model) // 모델 명시

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toString().toRequestBody(jsonMediaType))
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val jsonResponse = JsonParser.parseString(responseBody) as JsonObject
                val choicesArray = jsonResponse.getAsJsonArray("choices")

                if (choicesArray != null && choicesArray.size() > 0) {
                    val assistantMessage = choicesArray[0].asJsonObject.getAsJsonObject("message")
                    val content = assistantMessage.getAsJsonPrimitive("content").asString
                    Log.d("aaa","askToChatGPT함수 content => ${content}")
                    runOnUiThread {
                        if(binding.textbox.text != "텍스트가 여기에 표시됩니다."){
                            var currentText = binding.textbox.text.toString()
                            var newText = currentText + content
                            binding.textbox.text = newText
                        }else{
                            binding.textbox.text = content
                        }
                    }

                } else {
                    runOnUiThread {
                        // UI 업데이트를 메인 스레드에서 수행
                        binding.textbox.text = "실패2"
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    // UI 업데이트를 메인 스레드에서 수행
                    binding.textbox.text = "오류 발생 ${e}"
                }
            }
        }.start()


    } */

    private fun askToMultiChatGPT(title:String,qList: List<String>){
        Toast.makeText(this@MainActivity,"askToMultiChatGPT함수 시작",Toast.LENGTH_SHORT).show()
        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // 연결 시간 초과 설정
            .readTimeout(300, TimeUnit.SECONDS)    // 읽기 시간 초과 설정
            .writeTimeout(300,TimeUnit.SECONDS)
            .build()

        val responses = runBlocking {
            val deferredResponses = qList.map { q ->
                async(Dispatchers.IO) {
                    val messagesArray = JsonArray()
                    val message = JsonObject()
                    message.addProperty("role", "user")
                    message.addProperty("content", "${title}이라는 제목의 ${q} 유튜브 영상 스크립트인데 요약해줘.") // 사용자 메시지를 추가
                    messagesArray.add(message)

                    val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = JsonObject()
                    requestBody.add("messages", messagesArray)
                    requestBody.addProperty("model", model) // 모델 명시

                    val request = Request.Builder()
                        .url(endpoint)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBody.toString().toRequestBody(jsonMediaType))
                        .build()

                    val response:Response  = client.newCall(request).execute()
                    response

                }
            }

            deferredResponses.awaitAll()
        }


        var responseList = mutableListOf<String>()

        for (response in responses){
            val responseBody = response.body?.string()
            val jsonResponse = JsonParser.parseString(responseBody) as JsonObject
            val choicesArray = jsonResponse.getAsJsonArray("choices")

            val assistantMessage = choicesArray[0].asJsonObject.getAsJsonObject("message")
            val content = assistantMessage.getAsJsonPrimitive("content").asString

            responseList.add(content)

        }
        scriptSummary = responseList.joinToString("")
        binding.textbox.text = responseList.joinToString("")
        binding.webView.visibility = GONE


    }

    // JavaScript에서 호출할 메서드
    @JavascriptInterface
    fun onTextExtracted(title:String,result: String) {
        Toast.makeText(this,title,Toast.LENGTH_LONG).show()
        scriptTitle = title.trim()
        askToMultiChatGPT(title,substringToken(result))
    }

    private fun substringToken(t:String): MutableList<String> {

        Toast.makeText(this@MainActivity,"substringToken함수 시작 ${t.length}",Toast.LENGTH_SHORT).show()
        Log.d("aaa","substringToken함수 시작 ${t.length}")
        var result = mutableListOf<String>()
        if (getTokenSize(t) <= 4000){
            result.add(t)
        }else{
            val tLen = t.length
            val frontToken = t.substring(0,tLen/2)
            val backToken = t.substring(tLen/2,tLen)
            val frontLen = getTokenSize(frontToken)
            val backLen = getTokenSize(backToken)
            if(frontLen <= 4000 && backLen <= 4000){
                result.add(frontToken)
                result.add(backToken)
            }else{
                if (frontLen > 4000){
                    for (tkn in substringToken(frontToken)){
                        result.add(tkn)
                    }
                }else {
                    result.add(frontToken)
                }

                if(backLen > 4000){
                    for (tkn in substringToken(backToken)){
                        result.add(tkn)
                    }
                }else{
                    result.add(backToken)
                }

            }
        }

        return result
    }


    // JavaScript 코드 실행 메서드
    private fun runJavaScriptCode(code: String) {
        webView.evaluateJavascript(code) { result ->
            // JavaScript 실행 결과를 처리
        }
    }
    fun getTokenSize(text: String?): Int {
        val registry = Encodings.newDefaultEncodingRegistry()
        val enc: Encoding = registry.getEncodingForModel(com.knuddels.jtokkit.api.ModelType.GPT_3_5_TURBO)
        val encoded: List<Int> = enc.encode(text)
        return encoded.size
    }
}