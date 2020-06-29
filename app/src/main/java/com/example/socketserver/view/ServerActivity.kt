package com.example.socketserver.view

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.socketserver.R
import kotlinx.android.synthetic.main.activity_server.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ServerActivity : AppCompatActivity() {

    private lateinit var serverSocket : ServerSocket
//    private var thread1 : Thread? = null
    private lateinit var message : String

    private lateinit var output : PrintWriter
    private lateinit var input : BufferedReader

    companion object {
        var SERVER_IP = ""
        const val SERVER_PORT = 8080
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        try{
            SERVER_IP = getLocalIpAddress()
        }catch (e : Exception){
            Log.e("Error getting IP", e.message)
        }

        Thread(Thread1(this)).start()

        btnSend.setOnClickListener {
            message = etMessage.text.toString().trim()
            if(message.isNotEmpty()){
                Thread(
                    Thread3(
                        message,
                        this
                    )
                ).start()
            }
        }
    }

    private fun getLocalIpAddress() : String{
        try{
            val wifiManager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).hostAddress
        }catch (e : Exception){
            Log.e("Exception IP address", e.message)
            return ""
        }
    }

    class Thread1(val activity : ServerActivity) : Runnable {
        override fun run() {
            val socket : Socket

            try {
                val serverSocket = ServerSocket(SERVER_PORT)
                activity.runOnUiThread(object : Runnable{
                    override fun run() {
                        activity.tvMessages.text = "Not connected"
                        activity.tvIP.text = "IP: " + SERVER_IP
                        activity.tvPort.text = "Port: " + SERVER_PORT.toString()
                    }
                })
                try{
                    socket = serverSocket.accept()
                    activity.output = PrintWriter(socket.getOutputStream())
                    activity.input = BufferedReader(InputStreamReader(socket.getInputStream()))

                    activity.runOnUiThread(object : Runnable{
                        override fun run() {
                            activity.tvMessages.text = "Connected\n"
                        }
                    })
                    Thread(
                        Thread2(
                            activity
                        )
                    ).start()
                }catch (e : Exception){
                    Log.e("Thread1 Inner Exception",e.message)
                }
            }catch (e : Exception){
                Log.e("Thread1 Exception",e.message)
            }
        }
    }

    class Thread2(val activity: ServerActivity) : Runnable {
        override fun run() {
            while (true) {
                try {
                    val message = activity.input.readLine()
                    if(message.isNotEmpty()){
                        activity.runOnUiThread(object : Runnable{
                            override fun run() {
                                activity.tvMessages.append("client: " + message + "\n")
                            }
                        })
                    }else {
                        val Thread1 = Thread(
                            Thread1(
                                activity
                            )
                        )
                        Thread1.start()
                        return
                    }
                }catch (e : Exception){
                    Log.e("Thread2 Exception",e.message)
                }
            }
        }
    }

    class Thread3(val message : String, val activity: ServerActivity) : Runnable {
        override fun run() {
            activity.output.write(message)
            activity.output.flush()

            activity.runOnUiThread(object : Runnable{
                override fun run() {
                    activity.tvMessages.append("server: " + message + "\n")
                    activity.etMessage.text.clear()
                }
            })
        }
    }
}
