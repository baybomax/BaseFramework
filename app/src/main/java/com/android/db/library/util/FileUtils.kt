package com.android.db.library.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * File utils
 *
 * Created by DengBo on 05/03/2018.
 */
object FileUtils {

    /**
     *
     * @param src Source file.
     * @param dst Destination file.
     */
    fun copy(src: File, dst: File) {
        val fis = FileInputStream(src)
        val fos = FileOutputStream(dst)

        val buf = ByteArray(1024 * 100)
        var len = 0
        while (fis.read(buf).apply { len = this } > 0) {
            fos.write(buf, 0, len)
        }

        fos.flush()
        fis.close()
        fos.close()
    }

    /**
     *
     * @param context  The context.
     * @param fileName The filename inside the assets.
     * @param dst      The destination file.
     */
    fun copyAssetFile(context: Context, fileName: String, dst: File) {
        val afd = context.assets.openFd(fileName)
        val fis = afd.createInputStream()
        val fos = FileOutputStream(dst)

        val buf = ByteArray(1024 * 100)
        var len = 0
        while (fis.read(buf).apply { len = this } > 0) {
            fos.write(buf, 0, len)
        }

        fos.flush()
        fis.close()
        fos.close()
    }

}
