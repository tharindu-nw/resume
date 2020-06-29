package com.example.socketserver.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.socketserver.R
import android.util.Log
import kotlinx.android.synthetic.main.activity_client.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ClientActivity : AppCompatActivity() {

    private var serverIP = ""
    private var serverPort = 0

    private lateinit var input : BufferedReader
    private lateinit var output : PrintWriter

    private lateinit var thread1 : Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        btnConnect.setOnClickListener {
            tvMessages.text = "Trying to connect"
            serverIP = etIP.text.toString().trim()
            serverPort = etPort.text.toString().trim().toInt()

            thread1 = Thread(Thread1(this))
            thread1.start()
        }

        btnSend.setOnClickListener {
            val message = etMessage.text.toString()
            if(message.isNotEmpty()) {
                Thread(Thread3(message, this)).start()
            }
        }
    }

    class Thread1(val activity: ClientActivity) : Runnable {
        override fun run() {
            val socket : Socket
            try {

                socket = Socket(activity.serverIP, activity.serverPort)
                activity.output = PrintWriter(socket.getOutputStream())
                activity.input = BufferedReader(InputStreamReader(socket.getInputStream()))

                activity.runOnUiThread(object : Runnable{
                    override fun run() {
                        activity.tvMessages.text = "Connected\n"
                    }
                })

                Thread(Thread2(activity)).start()
            }catch(e : Exception){
                Log.e("Thread 1 Exception",e.message)
            }
        }
    }

    class Thread2(val activity: ClientActivity) : Runnable {
        override fun run() {
            while(true) {
                try {
                    val message = activity.input.readLine()
                    if(message.isNotEmpty()){
                        activity.runOnUiThread(object : Runnable{
                            override fun run() {
                                activity.tvMessages.append("server: "+message+"\n")
                            }
                        })
                    }else{
                        Thread(Thread1(activity)).start()
                        return
                    }
                }catch(e : Exception){
                    Log.e("Thread 2 Exception",e.message)
                }
            }
        }
    }

    class Thread3(val message : String, val activity: ClientActivity) : Runnable {
        override fun run() {
            activity.output.write(message)
            activity.output.flush()

            activity.runOnUiThread(object : Runnable{
                override fun run() {
                    activity.tvMessages.append("client: "+message+"\n")
                    activity.etMessage.text.clear()
                }
            })
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
