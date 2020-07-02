package com.example.socketserver.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.socketserver.R
import com.example.socketserver.util.Constants
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnSendHome.setOnClickListener {
            openServer()
        }

        btnReceiveHome.setOnClickListener {
            openSocket()
        }

        btnShareHome.setOnClickListener {
            openShare()
        }
    }

    private fun openServer(){
        try{
            startActivity(Intent(this, SocketServerActivity::class.java)
                .apply {
                    putExtra(Constants.ORIGIN, HOME)
                })
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }catch (err : Exception){
            Log.e("Server", err.message)
        }
    }

    private fun openSocket(){
        try{
            startActivity(Intent(this, SocketClientActivity::class.java)
                .apply {
                })
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }catch (err : Exception){
            Log.e("Server", err.message)
        }
    }

    private fun openShare(){
        startActivity(Intent(this, YoutubeShareDialogActivity::class.java)
            .apply {
            })
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onBackPressed() {
        //do nothing
    }

    companion object{
        const val HOME = 900
    }
}
