package com.example.socketserver.view

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.socketserver.R
import com.example.socketserver.util.FileUtils
import com.example.socketserver.util.Parcel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_socket_client.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.net.Socket
import kotlin.concurrent.thread

class SocketClientActivity : AppCompatActivity() {

    private lateinit var qrScan : IntentIntegrator
    private var ytLink = ""
    private var messageParcel : Parcel? = null

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

                val inp = ObjectInputStream(client.getInputStream())
                messageParcel = inp.readObject() as Parcel
                client.close()

                if(messageParcel!!.isYTLink()){
                    openYouTube(messageParcel!!.getText())
                }else if(messageParcel!!.isPdf()){
                    openPdf()
                }
            }catch (e : Exception){
                Log.e("E:Lis", e.message)
            }
        }
    }

    private fun openYouTube(link : String){
        val ytIntent = Intent(Intent.ACTION_VIEW)
        ytIntent.data = Uri.parse(link)
        ytIntent.setPackage("com.google.android.youtube")
        try{
            startActivity(ytIntent)
            finish()
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this, "Youtube Application Not Found", Toast.LENGTH_LONG).show()
        }
    }

    private fun openPdf(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Downloads.TITLE, messageParcel!!.getFileName())
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, messageParcel!!.getFileName())
            contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)

            try{
                val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val uri = contentResolver.insert(contentUri, contentValues)
                if(uri != null){
                    val stream = contentResolver.openOutputStream(uri)
                    if(stream != null) {
                        stream.write(messageParcel!!.getFile())
                        stream.close()
                        val path = FileUtils.getPathFromUri(this, uri)
                        val extUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", File(path))
                        MediaScannerConnection.scanFile(
                            this,
                            arrayOf(path),
                            arrayOf("application/pdf")
                        ){_,_ ->
                            val snackbar = Snackbar.make(imgReceive, "Pdf File Received", Snackbar.LENGTH_INDEFINITE)
                                .setTextColor(ContextCompat.getColor(this, R.color.colorWhitePure))
                                .setActionTextColor(ContextCompat.getColor(this, R.color.colorReceive))
                                .setAction("OPEN"){
                                    openPdfExternal(extUri)
                                }
                            snackbar.show()
                        }
                    }else{
                        Toast.makeText(this, "Unable to Save File", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this, "Unable to Save File", Toast.LENGTH_LONG).show()
                }
            }catch (e: IOException){
                e.printStackTrace()
            }
        }else{
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), messageParcel!!.getFileName())
            try{
                file.createNewFile()
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(messageParcel!!.getFile())
                fileOutputStream.close()

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(messageParcel!!.getFileName(), messageParcel!!.getFileName(), true, "application/pdf", file.absolutePath, file.length(), true)
                val snackbar = Snackbar.make(imgReceive, "Pdf File Received", Snackbar.LENGTH_INDEFINITE)
                    .setTextColor(ContextCompat.getColor(this, R.color.colorWhitePure))
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorReceive))
                    .setAction("OPEN"){
                        openPdfExternal(Uri.fromFile(file))
                    }
                snackbar.show()
            }catch (e: IOException){
                e.printStackTrace()
            }
        }


    }

    private fun openPdfExternal(uri: Uri){
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.type = "application/pdf"
        pdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        pdfIntent.flags = Intent. FLAG_ACTIVITY_CLEAR_TOP
        pdfIntent.data = uri
        try{
            if(pdfIntent.data != null){
                startActivity(pdfIntent)
            }
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this, "No Application Available to View Pdf", Toast.LENGTH_LONG).show()
        }catch(e: Exception){
            e.printStackTrace()
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
