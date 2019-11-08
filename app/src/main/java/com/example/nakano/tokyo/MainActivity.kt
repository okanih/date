package com.example.nakano.tokyo

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.beacapp.FireEventListener
import com.beacapp.JBCPException
import com.beacapp.JBCPManager
import com.beacapp.ShouldUpdateEventsListener
import com.beacapp.UpdateEventsListener

import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var jbcpManager: JBCPManager? = null

    // リスナーを生成
    private val updateEventsListener = object : UpdateEventsListener {
        override fun onProgress(i: Int, i1: Int) {
            //何もしない

        }

        override fun onFinished(e: JBCPException?) {
            if (e != null) {
                jbcpManager!!.startScan()
            }

        }
    }

    // リスナーを生成
    private val shouldUpdateEventsListener = ShouldUpdateEventsListener { true }

    // リスナーを生成
    private val fireEventListener = FireEventListener { jsonObject ->
        val action_data = jsonObject.optJSONObject("action_data")
        val action = action_data.optString("action")


        // URLの場合
        if (action == "jbcp_open_url") {
            Log.d("DEBUG", action_data.optString("url"))
        } else if (action == "jbcp_open_image") {
            Log.d("DEBUG", action_data.optString("image"))
        } else if (action == "jbcp_custom_key_value") {
            Log.d("DEBUG", action_data.optString("key_values"))
        } else if (action == "jbcp_open_text") {
            Log.d("DEBUG", action_data.optString("text"))
        }//テキストの場合
        //カスタムの場合
        //画像の場合
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
            var needPermission = false
            for (permission in permissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            permission
                        )
                    ) {
                        needPermission = true
                    } else {
                        needPermission = true
                    }
                }
            }
            if (needPermission) {
                showExplanationDialog(permissions, 0)
            } else {
                activate()
            }
        }
    }

    private fun activate() {
        //通信が走るため、別スレッドでの処理にする
        Thread(Runnable {
            try {
                jbcpManager = JBCPManager.getManager(
                    this@MainActivity,
                    "アクティベーションキー",
                    "シークレットキー", null
                )
            } catch (e: JBCPException) {
                return@Runnable
            }

            if (jbcpManager == null) {
                return@Runnable
            }
            // リスナーを登録
            jbcpManager!!.updateEventsListener = updateEventsListener
            jbcpManager!!.shouldUpdateEventsListener = shouldUpdateEventsListener
            jbcpManager!!.fireEventListener = fireEventListener


            // イベントを更新する
            jbcpManager!!.startUpdateEvents()
        }).start()
    }

    private fun showExplanationDialog(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissions, requestCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (0 == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activate()
            }
        }
    }
}