package com.example.socketserver.view

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socketserver.R
import com.example.socketserver.util.Constants
import com.example.socketserver.util.Parcel
import kotlinx.android.synthetic.main.activity_pdf_share_dialog.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.NumberFormatException

class PdfShareDialogActivity : AppCompatActivity() {

    private lateinit var fileUri : Uri
    private var file : ByteArray? = null
    private var fileName : String? = null
    private var pageNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_share_dialog)

        try{
            fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
            fileName = getFileName(fileUri)
//            file = getBytes(this, fileUri)
            tvNamePdf.text = fileName ?: "File not found"
        }catch (e: IOException){
            Log.e("E:file", e.message)
        }catch (e: Exception){
            Log.e("E:intent", e.message)
        }

        btnShareWP.setOnClickListener {
            //hide the keyboard and disable buttons
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            if(fileName != null){
                val message = Parcel(fileName!!, true)
                openServer(message)
            }else{
                Toast.makeText(this, "Error Reading File", Toast.LENGTH_LONG).show()
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        }

        btnSharePdf.setOnClickListener {
            //hide the keyboard and disable buttons
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            if(edtxtPage.text.isNotEmpty()){
                try{
                    pageNumber = edtxtPage.text.toString().toInt()
                    if(fileName != null){
                        val message = Parcel(fileName!!, pageNumber)
                        openServer(message)
                    }else{
                        Toast.makeText(this, "Error Reading File", Toast.LENGTH_LONG).show()
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                }catch (e: NumberFormatException){
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_LONG).show()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }else{
                if(fileName != null){
                    val message = Parcel(fileName!!, true)
                    openServer(message)
                }else{
                    Toast.makeText(this, "Error Reading File", Toast.LENGTH_LONG).show()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
    }

    private fun openServer(message: Parcel){
        try{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            startActivity(Intent(this, SocketServerActivity::class.java)
                .apply {
                    putExtra(Constants.MESSAGE, message)
                    putExtra(Constants.URI, fileUri)
                })
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }catch (err : Exception){
            Log.e("Server", err.message)
        }
    }

    private fun getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_FILE -> File(uri.path).name
        ContentResolver.SCHEME_CONTENT -> getCursorContent(uri)
        else -> null
    }

    private fun getCursorContent(uri: Uri): String? = try {
        contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            cursor.run {
                if (moveToFirst()) getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
                else null
            }.also { cursor.close() }
        }
    } catch (e: Exception) {
        null
    }
}
