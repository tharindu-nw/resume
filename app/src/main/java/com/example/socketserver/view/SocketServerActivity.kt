package com.example.socketserver.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.socketserver.R
import com.example.socketserver.util.Constants
import com.example.socketserver.util.Parcel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_socket_server.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectOutputStream
import java.net.NetworkInterface
import java.net.ServerSocket
import java.nio.charset.Charset
import java.util.*

class SocketServerActivity : AppCompatActivity() {

    var server = ServerSocket()

    private var youtubeLink = ""
    private var webLink = ""
    private var pdfName = ""
    private var fileUri : Uri? = null
    private var backPressedTime : Date? = null
    private lateinit var messageParcel : Parcel
    private var multiFormatWriter = MultiFormatWriter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_server)

        val myIp = getIPAddress(true)
        server = ServerSocket(9990)

        messageParcel = intent.getSerializableExtra(Constants.MESSAGE) as Parcel
        processParcel()

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

    private fun processParcel(){
        if(isStoragePermissionGranted()){
            when {
                messageParcel.isYTLink() -> {
                    youtubeLink = messageParcel.getText()
                    tvShareName.text = SpannableStringBuilder(youtubeLink)
                    StartListening(this).execute()
                }
                messageParcel.isPdf() -> {
                    fileUri = intent.getParcelableExtra(Constants.URI)
                    pdfName = messageParcel.getFileName()
                    tvShareName.text = SpannableStringBuilder(pdfName)
                    StartListening(this).execute()
                }
                messageParcel.isWebLink() -> {
                    webLink = messageParcel.getText()
                    tvShareName.text = SpannableStringBuilder(webLink)
                    StartListening(this).execute()
                }
            }
        }
    }

    class StartListening(private val activity: SocketServerActivity) : AsyncTask<Any?, Any?, Any?>(){
        override fun doInBackground(vararg params: Any?) {
            while(true){
                try{
                    val client = activity.server.accept()

                    if(activity.messageParcel.isPdf()){
                        val snackbarFile = Snackbar.make(activity.imgShare, "Preparing File. Do Not Close the App", Snackbar.LENGTH_INDEFINITE)
                        activity.runOnUiThread { snackbarFile.show() }
                        val file = activity.getBytes(activity, activity.fileUri!!)
                        activity.messageParcel.setFile(file!!)
                        activity.runOnUiThread { snackbarFile.dismiss() }
                    }
                    val snackbar = Snackbar.make(activity.imgShare, "Sending... Socket is busy, Please Wait", Snackbar.LENGTH_INDEFINITE)
                    activity.runOnUiThread { snackbar.show() }
                    val out = ObjectOutputStream(client.getOutputStream())
                    out.writeObject(activity.messageParcel)
                    out.flush()

                    activity.runOnUiThread { snackbar.dismiss() }
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

    private fun getBytes(context: Context, uri: Uri): ByteArray? {
        val iStream = context.contentResolver.openInputStream(uri)
        return try {
            getBytes(iStream!!)
        } finally {
            // close the stream
            try {
                iStream!!.close()
            } catch (ignored: IOException) {
                /* do nothing */
                ignored.printStackTrace()
            }
        }
    }

    private fun getBytes(inputStream: InputStream): ByteArray? {
        var bytesResult: ByteArray? = null
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        try {
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            bytesResult = byteBuffer.toByteArray()
        } finally {
            // close the stream
            try {
                byteBuffer.close()
            } catch (ignored: IOException) {
                /* do nothing */
                ignored.printStackTrace()
            }
        }
        return bytesResult
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).equals(
                    PackageManager.PERMISSION_GRANTED)) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            processParcel()
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
}
