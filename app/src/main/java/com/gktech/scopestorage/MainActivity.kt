package com.gktech.scopestorage

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.gktech.scopestorage.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val bitmap=it.data?.extras?.get("data") as Bitmap
        binding.ivMain.setImageBitmap(bitmap)
        storeBitmap(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.btnCapture.setOnClickListener {
            openCamera()
        }

    }

    private fun openCamera(){
        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResult.launch(intent)
    }

    private fun storeBitmap(bitmap:Bitmap){
        val contentResolver = this.contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else{
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValue = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"${Date().time}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                put(MediaStore.Images.Media.IS_PENDING,1)
            }
        }

        val imageUri = contentResolver.insert(imageCollection,contentValue)
        imageUri?.let {
            val outputStream = contentResolver.openOutputStream(it)
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream)
            outputStream?.close()
            contentValue.clear()
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                contentValue.put(MediaStore.Images.Media.IS_PENDING,0)
            }
            contentResolver.update(it,contentValue,null,null)
            outputStream?.close()
        }
    }
}