package com.android.db.library.util

import java.io.File
import java.util.*

/**
 * Directory utils
 *
 * Created by DengBo on 05/03/2018.
 */
object DirUtils {

    fun generatePath(root: String, prefix: String, file: File, suffix: String) {
        generatePath(root, prefix, file.name, suffix)
    }

    fun generatePath(root: String, prefix: String, date: Date, suffix: String): String {
        return generatePath(root, prefix, date.string, suffix)
    }

    fun generatePath(root: String, prefix: String, fileName: String, suffix: String): String {
        return (root + prefix + fileName + suffix).slashes.apply {
            File(this).parentFile.mkdirs()
        }
    }

}
