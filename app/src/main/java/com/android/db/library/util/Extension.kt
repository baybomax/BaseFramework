package com.android.db.library.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import com.android.db.library.Const
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension
 *
 * Created by DengBo on 01/03/2018.
 */

fun <A: Any, B: Any, R> let(a: A?, b: B?, handler: (A, B)->R): R? {
    return if (null != a && null != b) {
        handler(a, b)
    } else {
        null
    }
}

fun <A: Any, B: Any, C: Any, R> let(a: A?, b: B?, c: C?, handler: (A, B, C) -> R): R? {
    return if (null != a && null != b && null != c) {
        handler(a, b, c)
    } else {
        null
    }
}

val Boolean.visibleOrInvisible: Int
    get() = if (this) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }

val Boolean.visibleOrGone: Int
    get() = if (this) {
        View.VISIBLE
    } else {
        View.GONE
    }

/**
 * @see Const.simpleDataFormat
 */
val String.date: Date?
    @SuppressLint("SimpleDateFormat")
    get() = try {
        SimpleDateFormat(Const.simpleDataFormat).parse(this)
    } catch (e: Exception) {
        null
    }

/**
 * @see Const.simpleDataFormat
 */
val Date.string: String
    @SuppressLint("SimpleDateFormat")
    get() = SimpleDateFormat(Const.simpleDataFormat).format(this)

val String.slashes: String
    get() = replace("/", File.separator)


/**
 * Detach view from root
 * @param handler
 */
inline fun Context.detached(handler: ViewManager.()->View): View {
    val root = LinearLayout(this)
    root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
    )
    val view = root.handler()
    root.removeView(view)
    return view
}
