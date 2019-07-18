package com.ar.tsne.ardemo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.webkit.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import android.os.Build
import android.widget.Toast
import io.ar.canabscript.WebAppInterface


class BackgroundService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val LAYOUT_FLAG: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        )

//        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = 0
        params.height = 0

        val webView = WebView(this)
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface") // To call methods in Android from using js in the html, AndroidInterface.showToast, AndroidInterface.getAndroidVersion etc
        webView.settings.javaScriptEnabled = true
        webView.settings.allowUniversalAccessFromFileURLs = true

        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                Log.d(TSNE.TAG, "loading web view: request: $request error: $error")
            }

            override fun shouldInterceptRequest(webview: WebView, webrequest: WebResourceRequest): WebResourceResponse? {
                Log.d(TSNE.TAG, "shouldInterceptRequest")
                val url = webrequest.url.toString()

                if(url.endsWith(".json"))
                    return this.handleRequest(url)
                else
                    return super.shouldInterceptRequest(webview, webrequest);
            }

            private fun handleRequest(urlString: String): WebResourceResponse? {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "")
                    connection.setRequestMethod("GET")
                    connection.setDoInput(true)
                    connection.connect()

                    val inputStream = connection.getInputStream()
                    return WebResourceResponse("text/json", "utf-8", inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            /*override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (request.url.toString().contains("/endProcess")) {
                    windowManager.removeView(webView)
                    webView.post { webView.destroy() }
                    stopSelf()
                    return WebResourceResponse("bgsType", "someEncoding", null)
                } else {
                    return null
                }
            }*/
        }

        webView.webChromeClient = CallbackWebChromeClient()

        webView.loadUrl("file:///android_asset/tsnejs-word2vec.html")
        windowManager.addView(webView, params)

        Toast.makeText(this@BackgroundService, "The T-SNE algorithm is running at background", Toast.LENGTH_LONG).show()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TSNE.TAG, "The service is no longer used and is being destroyed")
        Toast.makeText(this@BackgroundService, "The T-SNE service is no longer used and is being destroyed", Toast.LENGTH_LONG).show()
    }

    private inner class CallbackWebChromeClient : WebChromeClient() {
    }

}