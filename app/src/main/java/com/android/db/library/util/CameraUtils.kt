package com.android.db.library.util

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Camera utils
 *
 * Created by DengBo on 09/03/2018.
 */
object CameraUtils {

    val TAKE_PICTURE_FROM_CAMERA = 100
    val TAKE_PICTURE_FROM_PICK   = 200
    val TAKE_PICTURE_FROM_CROP   = 300

    /**
     * Open camera to take a picture.
     *
     * @param activity activity
     * @param path pic storage path
     * @return file which save the picture
     */
    fun openCamera(activity: Activity, path: String): File? {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            if (null != resolveActivity(activity.packageManager)) {
                val file = File(path)
                val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(activity, "camera.provider", file)
                } else {
                    Uri.fromFile(file)
                }
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                activity.startActivityForResult(this, TAKE_PICTURE_FROM_CAMERA)

                return file
            }
        }

        return null
    }

    /**
     * Open album to take a picture.
     *
     * @param activity activity
     */
    fun openAlbum(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, TAKE_PICTURE_FROM_PICK)
    }

    /**
     * Crop the photo
     *
     * @param activity activity
     * @param uri photo uri
     * @param output output path
     * @return
     */
    fun cropPhoto(activity: Activity, uri: Uri, output: String): String = Intent("com.android.camera.action.CROP").run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        setDataAndType(uri, "image/*")

        putExtra("crop", "true") //This crop equals true set the view can be crop in the intent
        putExtra("aspectX", 1)   // aspectX aspectY is the aspect of width and height
        putExtra("aspectY", 1)
        putExtra("outputX", 400) // outputX outputY is the crop width and height
        putExtra("outputY", 400)
        putExtra("return-data", false)   //Whether save data to return-data
        putExtra("scale", true)  // There will appear black border when width/height not enough, set to disappear this
        putExtra("scaleUpIfNeeded", true)
        putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(output)))
        putExtra("outputFormat", Bitmap.CompressFormat.PNG)

        activity.startActivityForResult(this, TAKE_PICTURE_FROM_CROP)

        output
    }

    /**
     * Different phone take a picture with different degree, so do rotate to this picture
     *
     * @param filePath rotate file path which need
     * @return true/false
     */
    fun rotateImageIdNeed(filePath: String): Boolean = try {
        var degree = 0
        val exifInterface = ExifInterface(filePath)//obtain the prefix of the pic
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90  -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
        }
        if (degree != 0) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var bitmap = BitmapFactory.decodeFile(filePath, options)
            val width = options.outWidth
            val height = options.outHeight
            val hh = 360.0f
            val ww = 360.0f
            var be = 1// default 1 mean not scale
            if (width > height && width > ww) {
                be = (width / ww).toInt() + 1
            }
            if (height > width && height > hh) {
                be = (height / hh).toInt() + 1
            }
            if (be <= 0) {
                be = 1
            }
            options.inSampleSize = be
            options.inJustDecodeBounds = false
            //
            bitmap = BitmapFactory.decodeFile(filePath, options)
            //rotate matrix
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            //create a new bitmap
            val resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            saveBitmap2File(resizedBitmap, filePath, 100)
        }

        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    /***
     * Save bitmap to file
     *
     * @param bm bitmap
     * @param path file path
     * @param quality quality of picture
     * @return file
     */
    fun saveBitmap2File(bm: Bitmap?, path: String, quality: Int): File? {
        if (null == bm || bm.isRecycled) {
            return null
        }
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            val bos = BufferedOutputStream(
                    FileOutputStream(file))
            bm.compress(Bitmap.CompressFormat.JPEG, quality, bos)
            bos.flush()
            bos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            if (!bm.isRecycled) {
                bm.recycle()
            }
        }
    }

}
