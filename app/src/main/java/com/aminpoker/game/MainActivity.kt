package com.aminpoker.game

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.graphics.Color
import android.os.Build

class MainActivity : Activity() {
    private var gameWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.rgb(7, 26, 22)
        window.navigationBarColor = Color.rgb(7, 26, 22)

        try {
            val web = WebView(this)
            gameWebView = web
            web.setBackgroundColor(Color.rgb(6, 21, 17))
            web.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = true
                allowContentAccess = true
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
            }
            web.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        showStartupError("بارگذاری بازی ناموفق بود. لطفاً برنامه را ببند و دوباره باز کن.")
                    }
                }

                override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                    Log.e("AminPoker", "WebView renderer stopped: $detail")
                    showStartupError("موتور نمایش بازی متوقف شد. برنامه را دوباره باز کن.")
                    return true
                }
            }
            web.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    Log.d("AminPoker-Web", "${message.message()} at line ${message.lineNumber()}")
                    return true
                }
            }
            setContentView(web)
            web.loadUrl("file:///android_asset/index.html")
        } catch (e: Exception) {
            Log.e("AminPoker", "Startup failed", e)
            showStartupError("برنامه هنگام اجرا با خطا روبه‌رو شد. گوشی را یک‌بار راه‌اندازی مجدد کن و دوباره امتحان کن.\n\nجزئیات: ${e.javaClass.simpleName}: ${e.message ?: "نامشخص"}")
        } catch (e: LinkageError) {
            Log.e("AminPoker", "Startup linkage failure", e)
            showStartupError("اجزای لازم برای اجرای برنامه در گوشی در دسترس نیستند.")
        }
    }

    private fun showStartupError(message: String) {
        runOnUiThread {
            val text = TextView(this).apply {
                setTextColor(Color.WHITE)
                textSize = 16f
                text = message
                gravity = android.view.Gravity.CENTER
                setPadding(28, 28, 28, 28)
                setBackgroundColor(Color.rgb(7, 26, 22))
            }
            setContentView(text)
        }
    }

    override fun onDestroy() {
        gameWebView?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        gameWebView = null
        super.onDestroy()
    }
}
