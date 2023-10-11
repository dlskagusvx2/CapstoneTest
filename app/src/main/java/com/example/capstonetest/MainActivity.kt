package com.example.capstonetest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonetest.databinding.ActivityMainBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var webView: WebView
    val apiKey = "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"// api 키 입력해야함
    val endpoint = "https://api.openai.com/v1/chat/completions"
    val model = "gpt-3.5-turbo" // 사용할 모델 (GPT-3 Turbo)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        webView = findViewById(R.id.webView)

        // WebView 설정
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화
        webSettings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3" // User Agent 설정

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
                    var parentElement = document.getElementById('primary-button');
                    var buttonElement = parentElement.querySelector('button');
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
                                var allText = textContents.join('\\n');
                                window.android.onTextExtracted(allText); // 결과를 Android 앱에 전달
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
        webView.loadUrl("https://www.youtube.com/watch?v=fkS-nARvbbs")

        // WebView에서 JavaScript 코드 실행 결과를 처리하는 인터페이스
        webView.addJavascriptInterface(this, "android")
    }

    private fun askToChatGPT(q: String) {
        val messagesArray = JsonArray()
        val userMessage = JsonObject()
        userMessage.addProperty("role", "user")
        userMessage.addProperty("content", "${q} 유튜브 영상 스크립트인데 요약해줘.") // 사용자 메시지를 추가
        messagesArray.add(userMessage)

        val client = OkHttpClient()
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
                        // UI 업데이트를 메인 스레드에서 수행
                        binding.textbox.text = content ?: "실패"
                    }
                } else {
                    runOnUiThread {
                        // UI 업데이트를 메인 스레드에서 수행
                        binding.textbox.text = "실패"
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
        askToChatGPT(result)
    }



    // JavaScript 코드 실행 메서드
    private fun runJavaScriptCode(code: String) {
        webView.evaluateJavascript(code) { result ->
            // JavaScript 실행 결과를 처리
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show() // 테스트 용도
            onTextExtracted(result)
        }
    }

}
