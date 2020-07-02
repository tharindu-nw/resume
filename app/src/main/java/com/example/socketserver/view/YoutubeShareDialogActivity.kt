package com.example.socketserver.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.socketserver.R
import com.example.socketserver.util.Constants
import kotlinx.android.synthetic.main.activity_youtube_share_dialog.*
import java.lang.NumberFormatException

class YoutubeShareDialogActivity : AppCompatActivity() {

    private var link = ""
    private var hrs = 0
    private var mins = 0
    private var secs = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_share_dialog)

        link = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        tvLinkYT.text = link

        btnShare.setOnClickListener {
            //if user opts to share with timestamp, time input needs to be validated
            validateInput()
        }

        btnShareWT.setOnClickListener {
            //if there is no time input, share the link straight through
            openServer()
        }

        edtxtHr.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtxtHr.isActivated = false
            }
        })

        edtxtMin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtxtMin.isActivated = false
            }
        })

        edtxtSec.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtxtSec.isActivated = false
            }
        })
    }

    private fun validateInput(){
        try{
            //hide the keyboard and disable buttons
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            //take the user inputs
            if(edtxtHr.text.isNotEmpty()){
                hrs = edtxtHr.text.toString().toInt()
            }
            if(edtxtMin.text.isNotEmpty()){
                mins = edtxtMin.text.toString().toInt()
            }
            if(edtxtSec.text.isNotEmpty()){
                secs = edtxtSec.text.toString().toInt()
            }

            //validate the input values
            if(hrs==0 && mins==0 && secs==0){
                openServer()
            }else if(mins>59 || mins<0){
                edtxtMin.isActivated = true
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }else if(secs>59 || secs<0){
                edtxtSec.isActivated = true
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }else if(hrs<0){
                edtxtHr.isActivated = true
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }else{
                prepareLink(hrs,mins,secs)
            }
        }catch (e: NumberFormatException){
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_LONG).show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }catch (e: Exception){
            Log.e("E:validate", e.message)
        }
    }

    private fun prepareLink(hrs: Int, mins: Int, secs: Int){
        if(hrs>0 && mins>0 && secs>0){
            link += "?t=${hrs}h${mins}m${secs}s"
        }else if(mins>0 && secs>0){
            link += "?t=${mins}m${secs}s"
        }else if(secs>0){
            link += "?t=${secs}s"
        }
        openServer()
    }

    private fun openServer(){
        try{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            startActivity(Intent(this, SocketServerActivity::class.java)
                .apply {
                    putExtra(Constants.ORIGIN, YT_SHARE)
                    putExtra(Constants.LINK, link)
                })
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }catch (err : Exception){
            Log.e("Server", err.message)
        }
    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, HomeActivity::class.java)
                .apply {
                })
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    companion object{
        const val YT_SHARE = 901
    }
}
