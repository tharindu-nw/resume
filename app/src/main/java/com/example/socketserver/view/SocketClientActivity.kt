package com.example.socketserver.view

import android.content.Intent
import android.drm.DrmStore
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.socketserver.R
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_socket_client.*
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class SocketClientActivity : AppCompatActivity() {

    private lateinit var qrScan : IntentIntegrator
    private var ytLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_client)

        qrScan = IntentIntegrator(this)
        btnConnect.setOnClickListener {
            qrScan.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            val result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
            if(result != null){
                if(result.contents == null){
                    Toast.makeText(this, "No result found", Toast.LENGTH_LONG).show()
                }else{
                    val qrString = result.contents
                    val ip = qrString.split(",").first()
                    val port = qrString.split(",").last().toInt()
                    listenToServer(ip,port)
                }
            }else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }catch (e : Exception){
            Log.e("E:oAR", e.message)
        }

    }

    private fun listenToServer(ipAddress: String, port: Int){
        thread {
            try{
                val client = Socket(ipAddress, port)
                client.outputStream.write("Hello from the client!".toByteArray())

                val scanner = Scanner(client.inputStream)
                while(scanner.hasNextLine()){
                    ytLink = scanner.nextLine()
                    runOnUiThread {
                        tv1.text = ytLink
                    }
                    break
                }
                client.close()
                openYouTube(ytLink)
            }catch (e : Exception){
                Log.e("E:Lis", e.message)
            }
        }
    }

    private fun openYouTube(link : String){
        val ytIntent = Intent(Intent.ACTION_VIEW)
        ytIntent.data = Uri.parse(link)
        ytIntent.setPackage("com.google.android.youtube")
        startActivity(ytIntent)
    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, HomeActivity::class.java)
                .apply {
                })
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }
}
