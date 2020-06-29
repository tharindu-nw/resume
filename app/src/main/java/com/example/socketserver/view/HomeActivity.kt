package com.example.socketserver.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.socketserver.R
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
    }

    private fun openServer(){
        try{
            startActivity(Intent(this, ServerActivity::class.java)
                .apply {
                })
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }catch (err : Exception){
            Log.e("Server", err.message)
        }
    }

    private fun openSocket(){
        try{
            startActivity(Intent(this, ClientActivity::class.java)
                .apply {
                })
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }catch (err : Exception){
            Log.e("Server", err.message)
        }
    }

    override fun onBackPressed() {
        //do nothing
    }
}
