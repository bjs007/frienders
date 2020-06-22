//package com.frienders.main.activity.group
//
//import android.content.Context
//import android.content.Intent
//import android.database.Cursor
//import android.net.Uri
//import android.os.Environment
//import android.os.Handler
//import android.provider.MediaStore
//import android.view.View
//import android.widget.ProgressBar
//import com.abedelazizshe.lightcompressorlibrary.CompressionListener
//import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
//import com.abedelazizshe.lightcompressorlibrary.VideoQuality
//import java.io.File
//import java.io.IOException
//
//class VideoCompressorHelper
//{
//
//
//    fun getMediaPath(context: Context?, uri: Uri?): String {
//
//        val projection = arrayOf(MediaStore.Video.Media.DATA)
//        var cursor: Cursor? = null
//
//        if(uri != null && context!= null)
//        {
//            try {
//
//                cursor = context.contentResolver.query(uri, projection, null, null, null)
//                return if (cursor != null) {
//                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
//                    cursor.moveToFirst()
//                    cursor.getString(columnIndex)
//
//                } else ""
//            }finally {
//                cursor?.close()
//            }
//
//        }
//       return "";
//    }
//
//
//    fun compress(context: Context, data: Intent?, progressBar: ProgressBar?, path: String)
//    {
//
//        if(data != null && data.data != null) {
//            val uri = data.data;
//
//
//            var  path = getMediaPath(context, uri)
//            val file = File(path)
//
//
////        GlideApp.with(this).load(uri).into(videoImage)
//
//            val downloadsPath =
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val desFile = File(downloadsPath, "${System.currentTimeMillis()}_${file.name}")
//            if (desFile.exists()) {
//                desFile.delete()
//                try {
//                    desFile.createNewFile()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//
//            }
//
//            var time = 0L
//
//
//
//            VideoCompressor.start(
//                    path,
//                    desFile.path,
//                    object : CompressionListener {
//                        override fun onProgress(percent: Float) {
//                            //Update UI
//
//                        }
//
//                        override fun onStart() {
//                            time = System.currentTimeMillis()
//
//                        }
//
//                        override fun onSuccess() {
//                            val newSizeValue = desFile.length()
//
//
//                            path = desFile.path
//
//                            Handler().postDelayed({
//
//                            }, 50)
//
//                        }
//
//                        override fun onFailure() {
//
//                        }
//
//                        override fun onCancelled() {
////                        Log.wtf("TAG", "compression has been cancelled")
//                            // make UI changes, cleanup, etc
//                        }
//                    }, VideoQuality.MEDIUM, isMinBitRateEnabled = false)
//        }
//    }
//
//}
//
