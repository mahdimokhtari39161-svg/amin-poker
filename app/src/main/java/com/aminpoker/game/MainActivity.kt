package com.aminpoker.game

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

class MainActivity : Activity() {
    private var gameWebView: WebView? = null
    private var showingError = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.rgb(7, 26, 22)
        window.navigationBarColor = Color.rgb(7, 26, 22)
        openGame()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openGame() {
        try {
            showingError = false
            val web = WebView(applicationContext).also { gameWebView = it }
            // Software compositing avoids GPU/WebView renderer crashes on some devices.
            web.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            web.setBackgroundColor(Color.rgb(6, 21, 17))
            web.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = true
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            }
            web.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    super.onReceivedError(view, request, error)
                    if (request.isForMainFrame) {
                        Log.e("AminPoker", "Main-frame load error: ${error.errorCode} ${error.description}")
                        showStartupError("بارگذاری بازی ناموفق بود. برنامه را ببند و دوباره باز کن.")
                    }
                }

                override fun onRenderProcessGone(view: WebView, detail: android.webkit.RenderProcessGoneDetail): Boolean {
                    Log.e("AminPoker", "WebView renderer stopped; crashed=${detail.didCrash()}")
                    if (gameWebView === view) gameWebView = null
                    view.destroy()
                    showStartupError("موتور نمایش بازی متوقف شد. لطفاً دوباره برنامه را باز کن.")
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
            showStartupError("برنامه هنگام اجرا با خطا روبه‌رو شد. لطفاً دوباره تلاش کن.\n\n${e.javaClass.simpleName}: ${e.message ?: "نامشخص"}")
        } catch (e: LinkageError) {
            Log.e("AminPoker", "Startup linkage failure", e)
            showStartupError("اجزای لازم برای اجرای برنامه در گوشی در دسترس نیستند.")
        }
    }

    private fun showStartupError(message: String) {
        if (isFinishing || isDestroyed || showingError) return
        showingError = true
        runOnUiThread {
            val text = TextView(this).apply {
                setTextColor(Color.WHITE)
                textSize = 16f
                this.text = message
                gravity = Gravity.CENTER
                setPadding(28, 28, 28, 28)
                setBackgroundColor(Color.rgb(7, 26, 22))
            }
            setContentView(text)
        }
    }

    override fun onPause() {
        gameWebView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        gameWebView?.onResume()
    }

    override fun onDestroy() {
        gameWebView?.let { web ->
            try {
                web.stopLoading()
                web.loadUrl("about:blank")
                web.clearHistory()
                web.removeAllViews()
                web.destroy()
            } catch (e: Exception) {
                Log.w("AminPoker", "WebView cleanup failed", e)
            }
        }
        gameWebView = null
        super.onDestroy()
    }
}
