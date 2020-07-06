package com.example.socketserver.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.socketserver.R
import com.example.socketserver.util.Constants
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    private var backPressedTime : Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnReceiveHome.setOnClickListener {
            openSocket()
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

    override fun onBackPressed() {
        if(backPressedTime != null){
            val currentTime = Calendar.getInstance().time
            val timeDiff = currentTime.time - backPressedTime!!.time
            if(timeDiff > 5000){
                backPressedTime = currentTime
                Toast.makeText(this, "Press back once again to quit", Toast.LENGTH_LONG).show()
            }else{
                finish()
            }
        }else{
            backPressedTime = Calendar.getInstance().time
            Toast.makeText(this, "Press back once again to quit", Toast.LENGTH_LONG).show()
        }
    }

    companion object{
        const val HOME = 900
    }
}
