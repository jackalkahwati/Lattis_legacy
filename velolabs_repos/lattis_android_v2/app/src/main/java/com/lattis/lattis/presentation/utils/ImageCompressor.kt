package com.lattis.lattis.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object ImageCompressor {
    fun resizeAndCompressImageBeforeSend(
        context: Context,
        filePath: String?,
        fileName: String?
    ): String? {
        if (filePath == null || fileName == null) {
            return null
        }
        val MAX_IMAGE_SIZE = 512 * 512 // max final file size in kilobytes
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        var bmpPic = BitmapFactory.decodeFile(filePath, options) ?: return null
        var compressQuality = 100 // quality decreasing by 5 every loop.
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength >= MAX_IMAGE_SIZE)

        bmpPic = rotateImageIfRequired(bmpPic, Uri.fromFile(File(filePath)))

        try {
            val bmpFile =
                FileOutputStream(context.cacheDir.toString() + fileName)
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile)
            bmpFile.flush()
            bmpFile.close()
        } catch (e: Exception) {
            Log.e("compressBitmap", "Error on saving file")
        }
        //return the path of resized and compressed file
        return context.cacheDir.toString() + fileName
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {
        try {
            val ei = ExifInterface(selectedImage.getPath()!!)
            val orientation: Int =
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
                else -> img
            }
        }catch (e:Exception){

        }
        return img
    }
}