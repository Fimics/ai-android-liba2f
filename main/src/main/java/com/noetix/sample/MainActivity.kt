package com.noetix.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.libnoetix.sample.R
import com.noetix.core.CloudSetting
import com.noetix.libnoetix.IRobotSDKManager
import com.noetix.libnoetix.RobotConfig
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private var btnControl by Delegates.notNull<AppCompatButton>()
    private var btnLive by Delegates.notNull<AppCompatButton>()
    private var btnChat by Delegates.notNull<AppCompatButton>()
    private var btnSettings by Delegates.notNull<AppCompatImageView>()


    @SuppressLint("NewApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //去掉标题栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val params = window.attributes
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
        window.attributes = params
        setContentView(R.layout.activity_main_1024)

        btnSettings = this.findViewById(R.id.iv_settings)
        btnControl = this.findViewById(R.id.btn_control)
        btnLive = this.findViewById(R.id.btn_live)
        btnChat = this.findViewById(R.id.btn_chat)
        btnSettings.setOnClickListener {
        }
        btnControl.setOnClickListener {
        }

        btnLive.setOnClickListener {
            CloudSetting.getInstance().updateApk(this)
            CloudSetting.getInstance().updateConfig()
        }
        btnChat.setOnClickListener {
        }
        initSDK()
    }

    private fun initSDK(){
        //init sdk start
//        IRobotSDKManager.getInstance().enableLog(true)
//        IRobotSDKManager.getInstance().init(this,"","",false)
//        IRobotSDKManager.getInstance().setNeckRadiosDuration(floatArrayOf(0.0f,0.0f,0.0f),5.0f)
//        IRobotSDKManager.getInstance().saveAudioData(true)

//        IRobotSDKManager.getInstance().stopAudio2Face()
        //init sdk end

        val robotConfig = RobotConfig.Builder(this)
            .enableLog(true)
            .setSerialPort("")
            .setCan("")
            .setZeroDuration(5.0f)
            .setUAppKey("uAppKey")
            .setUChannel("uChannel")
            .setExt("")
            .build()
        IRobotSDKManager.getInstance().init(robotConfig)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        IRobotSDKManager.getInstance().unInit()
    }

}
