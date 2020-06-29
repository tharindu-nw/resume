package com.example.socketserver.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.socketserver.R
import kotlinx.android.synthetic.main.activity_socket_client.*
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class SocketClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_client)

        btnConnect.setOnClickListener {
            val ipAddress = edtxtIp.text.trim().toString()
            thread {
                val client = Socket(ipAddress, 9990)
                client.outputStream.write("Hello from the client!".toByteArray())

                val scanner = Scanner(client.inputStream)
                while(scanner.hasNextLine()){
                    runOnUiThread {
                        tv1.text = scanner.nextLine()
                    }
                    break
                }

                client.close()
            }
        }
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
