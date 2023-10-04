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

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var webView: WebView

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


    // JavaScript에서 호출할 메서드
    @JavascriptInterface
    fun onTextExtracted(result: String) {
        runOnUiThread {
            // 추출된 텍스트를 사용하여 필요한 작업을 수행
            binding.textbox.text = if (result.isNullOrEmpty()) "텍스트 추출 실패" else result
        }
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
