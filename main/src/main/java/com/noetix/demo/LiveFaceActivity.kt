package com.noetix.demo

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.libnoetix.demo.R
import com.noetix.libnoetix.IRobotSDKManager
import com.noetix.libnoetix.LiveFaceService


class LiveFaceActivity : AppCompatActivity() {


    private var tvIP: AppCompatTextView?=null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //去掉标题栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //去掉信息栏
        val params = window.attributes
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
        window.attributes = params
        setContentView( R.layout.activity_face_live)
        tvIP = findViewById(R.id.tv_ip)
        LiveFaceService.instance().start()
        IRobotSDKManager.getInstance().chatMode(false)
        updateIP()
    }

    private fun updateIP() {
        TaskExecutors.get().mainHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!TextUtils.isEmpty(LiveFaceService.IP_PORT)){
                    try {
                        val array = LiveFaceService.IP_PORT.split(":")
                        tvIP?.text ="IP地址: ${array[0]} , 端口: ${array[1]}"
                    }catch (e:Exception){
                        KLog.d("updateIP",e.message)
                    }

                }
            }
        },1000)
    }

    override fun onStop() {
        super.onStop()
        LiveFaceService.instance().stop()
    }


}
