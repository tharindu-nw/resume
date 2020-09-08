package com.example.socketserver.view

import android.Manifest
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.socketserver.R
import com.example.socketserver.util.Parcel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_socket_client.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.net.ConnectException
import java.net.Socket

class SocketClientActivity : AppCompatActivity() {

    private lateinit var qrScan : IntentIntegrator
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
                    ListenToServer(this,ip,port).execute()
                }
            }else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }catch (e : Exception){
            Log.e("E:oAR", e.message)
        }

    }

    class ListenToServer(private val activity: SocketClientActivity, private val ipAddress: String, private val port: Int) : AsyncTask<Any?, Any?, Any?>(){
        override fun doInBackground(vararg p0: Any?): Any? {
            try{
                val snackbarConnecting = Snackbar.make(activity.imgReceive, "Connecting... Do Not Close the App", Snackbar.LENGTH_INDEFINITE)
                activity.runOnUiThread { snackbarConnecting.show() }
                val client = Socket(ipAddress, port)

                val inp = ObjectInputStream(client.getInputStream())

                activity.runOnUiThread { snackbarConnecting.dismiss() }
                val snackbarReceiving = Snackbar.make(activity.imgReceive, "Receiving File... Do Not Close the App", Snackbar.LENGTH_INDEFINITE)
                activity.runOnUiThread { snackbarReceiving.show() }

                activity.messageParcel = inp.readObject() as Parcel
                client.close()

                activity.runOnUiThread { snackbarReceiving.dismiss() }

                if(activity.messageParcel!!.isYTLink()){
                    activity.openYouTube(activity.messageParcel!!.getText())
                }else if(activity.messageParcel!!.isPdf()){
                    if(activity.isStoragePermissionGranted()) {
                        activity.openPdf()
                    }
                }else if(activity.messageParcel!!.isWebLink()){
                    activity.openWebLink(activity.messageParcel!!.getText())
                }
            }catch (e : ConnectException){
                activity.runOnUiThread {
                    val snackbar = Snackbar.make(activity.imgReceive, "Failed to Connect", Snackbar.LENGTH_INDEFINITE)
                        .setTextColor(ContextCompat.getColor(activity, R.color.colorWhitePure))
                        .setActionTextColor(ContextCompat.getColor(activity, R.color.colorReceive))
                        .setAction("CLOSE"){
                            //do nothing
                        }
                    snackbar.show()
                }
            }catch (e : Exception){
                Log.e("E:Lis", e.message)
            }
            return null
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

    private fun openWebLink(link: String){
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.data = Uri.parse(link)
        try{
            startActivity(browserIntent)
            finish()
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this, "Application Not Found", Toast.LENGTH_LONG).show()
        }
    }

    private fun openPdf(){

        //this is the newer version of saving the file, cannot use due to bug
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
//                        val path2 = FileUtils.getPathFromUri(this, extUri)
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
        }else{*/
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), messageParcel!!.getFileName())
            try{
                file.createNewFile()
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(messageParcel!!.getFile())
                fileOutputStream.close()

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.addCompletedDownload(messageParcel!!.getFileName(), messageParcel!!.getFileName(), true, "application/pdf", file.absolutePath, file.length(), true)
                val snackMessage = if(messageParcel!!.getPageNumber() != null){
                    "File Received. Check Notifications for PDF. You were on page ${messageParcel!!.getPageNumber()}"
                }else{
                    "File Received. Check Notifications for PDF"
                }
                val snackbar = Snackbar.make(imgReceive, snackMessage, Snackbar.LENGTH_INDEFINITE)
                    .setTextColor(ContextCompat.getColor(this, R.color.colorWhitePure))
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorReceive))
                    .setAction("CLOSE"){
                        //do nothing
                    }
                snackbar.show()
            }catch (e: IOException){
                e.printStackTrace()
            }
//        }


    }

    //to open the pdf through intent from app (not used due to bug)
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

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).equals(PackageManager.PERMISSION_GRANTED)) {
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
            openPdf()
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
