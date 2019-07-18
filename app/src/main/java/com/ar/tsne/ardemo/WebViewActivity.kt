package com.ar.tsne.ardemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebResourceResponse
import io.ar.canabscript.WebAppInterface
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL


class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView = findViewById(R.id.webview) as WebView


        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface") // To call methods in Android from using js in the html, AndroidInterface.showToast, AndroidInterface.getAndroidVersion etc
        webView.settings.javaScriptEnabled = true

        webView.settings.allowUniversalAccessFromFileURLs = true;
        webView.webViewClient = MyWebViewClient()
        //webView.webChromeClient = MyWebChromeClient()

        webView.loadUrl("file:///android_asset/tsnejs-word2vec.html")
    }


    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            //Calling a javascript function in html page
            //view.loadUrl("javascript:alert(showVersion('called by Android'))")
        }

//        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//            return true
//            //return if (url.endsWith(".json")) true else false
//        }

        override fun shouldInterceptRequest(webview: WebView, webrequest: WebResourceRequest): WebResourceResponse? {
            Log.d("test", "shouldInterceptRequest")
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
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return null
            } catch (e: ProtocolException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {
//        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
//            Log.d("LogTag", message)
//            result.confirm()
//            return true
//        }
    }
}