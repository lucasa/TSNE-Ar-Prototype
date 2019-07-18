package io.ar.canabscript

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.ar.tsne.ardemo.TSNE
import com.google.ar.sceneform.math.Vector3
import org.json.JSONArray

class WebAppInterface// Instantiate the interface and set the context
internal constructor(internal var mContext: Context) {

    val androidVersion: Int
        @JavascriptInterface
        get() = android.os.Build.VERSION.SDK_INT

    // Show a toast from the web page
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun setWords(json: String) {
        //TSNE.WORDS = JSONArray(json)
        val words = JSONArray(json)
        for(i in 0..words.length()-1) {
            val word = words[i].toString()
            TSNE.WORDS.put(i, word)
        }
        Log.d(TSNE.TAG, "Words - " + TSNE.WORDS.toString())
    }

    @JavascriptInterface
    fun setPoints(jsonData: String, iteration: String) {
        TSNE.ITERATION = iteration.toInt()
        Log.d(TSNE.TAG, "ITERATION - " + TSNE.ITERATION)

        val points = JSONArray(jsonData)
        for(i in 0..points.length()-1) {
            val x = points.getJSONArray(i)[0].toString().toFloat()
            val y = points.getJSONArray(i)[1].toString().toFloat()
            val z = points.getJSONArray(i)[2].toString().toFloat()
            TSNE.POINTS.put(i, Vector3(x, y, z))
        }

    }

}