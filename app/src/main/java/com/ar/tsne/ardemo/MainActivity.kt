package com.ar.tsne.ardemo

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.os.Build
import android.provider.Settings
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    var ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469 //Random value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun openWebView(v: View?) {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
            val intent = Intent(this, BackgroundService::class.java)
            Toast.makeText(this@MainActivity, "The T-SNE algorithm is starting...", Toast.LENGTH_LONG).show()
            startService(intent)
        }
    }

    fun openARView(v: View) {
        startActivity(Intent(this, ARActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName"))
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                } else {
                    val intent = Intent(this, BackgroundService::class.java)
                    Toast.makeText(this@MainActivity, "The T-SNE algorithm is starting...", Toast.LENGTH_LONG).show()
                    startService(intent)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}