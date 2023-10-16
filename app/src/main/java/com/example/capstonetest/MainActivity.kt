package com.example.capstonetest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.capstonetest.databinding.ActivityMainBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.mlkit.common.sdkinternal.ModelType
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import kotlinx.android.synthetic.main.activity_main.indicator
import kotlinx.android.synthetic.main.activity_main.viewpager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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



    private var vpAdapter: FragmentStatePagerAdapter? = null





    class CustomPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val PAGENUMBER = 4

        override fun getCount(): Int {
            return PAGENUMBER
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TestFragment.newInstance(R.raw.img00, "test 00")
                1 -> TestFragment.newInstance(R.raw.img01, "test 01")
                2 -> TestFragment.newInstance(R.raw.img02, "test 02")
                else -> TestFragment.newInstance(R.raw.img03, "test 03")
            }
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_main)

        vpAdapter = CustomPagerAdapter(supportFragmentManager)
        viewpager.adapter = vpAdapter

        indicator.setViewPager(viewpager)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        webView = findViewById(R.id.webView)

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
                                    window.android.onTextExtracted(modifiedString); // 결과를 Android 앱에 전달
                                    clearInterval(elementsInterval); // setInterval 중지
                                }
                            }
                        }, 1000); // 1초마다 요소를 체크
                    },3000);
                    
                })();
                """
                    )
                }, 3000) // 5초 지연
            }
        }

        // 웹 페이지 로드
        webView.loadUrl("https://www.youtube.com/watch?v=ayOvfqFvrfE")

        // WebView에서 JavaScript 코드 실행 결과를 처리하는 인터페이스
        webView.addJavascriptInterface(this, "android")
    }

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


    }
    // JavaScript에서 호출할 메서드
    @JavascriptInterface
    fun onTextExtracted(result: String) {
        //webView.destroy() // WebView 종료
        sliceToken(result)

    }

    //토큰 제한 해결 메서드
    private fun sliceToken(tkn:String){
        Toast.makeText(this@MainActivity,"sliceToken함수 시작 ${tkn.length}",Toast.LENGTH_SHORT).show()
        Log.d("aaa","sliceToken함수 시작 ${tkn.length}")
        //binding.textbox.visibility = GONE
        val tkn_len = tkn.length
        if(getTokenSize(tkn) <= 4000){
            askToChatGPT(tkn)
        }else{
            var front_Text = tkn.substring(0,tkn_len / 2)
            var back_Text = tkn.substring(tkn_len/2, tkn_len)
            sliceToken(front_Text)
            sliceToken(back_Text)
            lastSummary()
        }
        //binding.textbox.visibility = VISIBLE
    }

    private fun lastSummary(){
        val postResult = binding.textbox.text.toString()
        Toast.makeText(this@MainActivity,"lastSummary함수 시작 ${postResult.length}",Toast.LENGTH_SHORT).show()
        Log.d("aaa","lastSummary함수 시작 ${postResult.length}")
        binding.textbox.text = "텍스트가 여기에 표시됩니다."
        sliceToken(postResult)
    }



    // JavaScript 코드 실행 메서드
    private fun runJavaScriptCode(code: String) {
        webView.evaluateJavascript(code) { result ->
            // JavaScript 실행 결과를 처리
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show() // 테스트 용도
        }
    }
    fun getTokenSize(text: String?): Int {
        val registry = Encodings.newDefaultEncodingRegistry()
        val enc: Encoding = registry.getEncodingForModel(com.knuddels.jtokkit.api.ModelType.GPT_3_5_TURBO)
        val encoded: List<Int> = enc.encode(text)
        return encoded.size
    }
}