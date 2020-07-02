package com.example.socketserver.view

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import com.example.socketserver.R
import com.example.socketserver.util.Constants
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_socket_server.*
import java.net.NetworkInterface
import java.net.ServerSocket
import java.nio.charset.Charset
import java.util.*

class SocketServerActivity : AppCompatActivity() {

    var ipAddress = ""
    var server = ServerSocket()

    private var fromHome = true
    private var youtubeLink = ""
    private var outLink = "default"
    private var multiFormatWriter = MultiFormatWriter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_server)

        val myIp = getIPAddress(true)

        server = ServerSocket(9990)

        val origin = intent.getIntExtra(Constants.ORIGIN, 0)
        if(origin == YoutubeShareDialogActivity.YT_SHARE){
            fromHome = false
            youtubeLink = intent.getStringExtra(Constants.LINK)
            outLink = youtubeLink + "\n"
            tvShareName.text = SpannableStringBuilder(youtubeLink)
            StartListening(this).execute()
        }

        val qrText = "$myIp,${server.localPort}"
        try{
            val bitMatrix = multiFormatWriter.encode(qrText, BarcodeFormat.QR_CODE, 150, 150)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imgQR.setImageBitmap(bitmap)
        }catch (e : Exception){
            Log.e("E:QR", e.message)
        }
        StartListening(this).execute()
    }

    class StartListening(private val activity: SocketServerActivity) : AsyncTask<Any?, Any?, Any?>(){
        override fun doInBackground(vararg params: Any?) {
            while(true){
                try{
                    val client = activity.server.accept()
                    /*activity.runOnUiThread{
                        activity.tvServerMessage.text = "Client connected: ${client.inetAddress.hostAddress}"
                    }*/

                    val writer = client.getOutputStream()
                    writer.write((activity.outLink).toByteArray(Charset.defaultCharset()))

                    /*val scanner = Scanner(client.inputStream)
                    while(scanner.hasNextLine()){
                        activity.runOnUiThread {
                            activity.tvMessage.text = scanner.nextLine()
                        }
                        break
                    }*/
                }catch (e : Exception){
                    Log.e("E:Lis", e.message)
                }

            }
        }
    }

    private fun getIPAddress(useIPv4 : Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses);
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress;
                        var isIPv4: Boolean
                        isIPv4 = sAddr.indexOf(':')<0
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) {
                                    sAddr.toUpperCase(Locale.ROOT)
                                } else {
                                    sAddr.substring(0, delim).toUpperCase(Locale.ROOT)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) { }
        return ""
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
