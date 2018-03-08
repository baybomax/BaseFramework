package com.android.db.library.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import java.io.FileOutputStream
import java.io.IOException

/**
 * View utils
 *
 * Created by DengBo on 05/03/2018.
 */
object ViewUtils {

    /**
     * Utility function to get the screen width of the device.
     * @param activity The activity
     * @return Screen width.
     */
    fun getScreenWidth(activity: Activity): Int {
        return DisplayMetrics().apply {
            activity.windowManager.defaultDisplay.getMetrics(this)
        }.widthPixels
    }

    /**
     * Utility function to get the screen width of the device.
     * @param activity The activity
     * @return Screen width.
     */
    fun getScreenHeight(activity: Activity): Int {
        return DisplayMetrics().apply {
            activity.windowManager.defaultDisplay.getMetrics(this)
        }.heightPixels
    }

    /**
     * Convert SP to Pixels.
     * @param context The context
     * @param sp size value in SPs.
     * @return size value in pixels
     */
    fun sp2Px(context: Context, sp: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return sp * scale
    }

    /**
     * Convert from DIP (Device Independent Pixel) to Pixels
     * @param context The context
     * @param dp size value in dip
     * @return size value in pixels.
     */
    fun dp2Px(context: Context, dp: Float): Float {
        val density = context.resources.displayMetrics.densityDpi
        return dp * (density.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * Calculate and set the X/Y offset of outer rectangle, based on the inner rectangle's
     * aspect ratio.
     *
     * Condition A:
     * <pre>
     * +--------------------+
     * | Outer              | offsetY
     * +--------------------+
     * | Inner              |
     * |                    |
     * |                    |
     * +--------------------+
     * |                    |
     * +--------------------+
    </pre> *
     *
     * or Condition B:
     * <pre>
     * offsetX
     * +----+--------------------+----+
     * |    | Inner              | Outer
     * |    |                    |    |
     * |    |                    |    |
     * +----+--------------------+----+
    </pre> *
     * @param outerRect [in/out] The rectangle of outer element, also holds the result.
     * @param innerRect [in] The rectangle of inner element, it can be scaled.
     * @param landscape true if the outer rectangle is in landscape mode, inner rectangle is
     * always in landscape.
     */
    fun calculateOuterOffset(outerRect: Rect, innerRect: Rect, landscape: Boolean) {
        var oh = outerRect.height()
        var ow = outerRect.width()
        val ih = innerRect.height()
        val iw = innerRect.width()

        if (!landscape) {
            val temp = ow
            ow = oh
            oh = temp
        }

        val sRatio = oh.toFloat() / ow.toFloat()
        val vRatio = ih.toFloat() / iw.toFloat()

        var deltaX = 0f
        var deltaY = 0f
        if (sRatio > vRatio) {
            // Condition A)
            deltaY = (oh.toFloat() - vRatio * ow) / 2
        } else {
            // Condition B)
            deltaX = (ow.toFloat() - oh.toFloat() / vRatio) / 2
        }

        if (landscape) {
            outerRect.left = (-deltaX).toInt()
            outerRect.top = (-deltaY).toInt()
        } else {
            outerRect.top = (-deltaX).toInt()
            outerRect.left = (-deltaY).toInt()
        }
    }

    /**
     * Equivalent to `showError(activity, e.getMessage(), e)`
     * @see showError
     */
    fun showError(activity: Activity, e: Throwable) {
        showError(activity, e.message, e)
    }

    /**
     * Equivalent to `showError(activity, message, null)`
     * @see showError
     */
    fun showError(activity: Activity, message: String) {
        showError(activity, message)
    }

    /**
     * Helper function to show the message as toast, and printStackTrace of the throwable.
     * @param activity The activity
     * @param message The String message to show.
     * @param e e Throwable instance.
     */
    fun showError(activity: Activity, message: String? = null, e: Throwable? = null) {
        message?.let {
            activity.runOnUiThread {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
            }
            Log.e(activity.localClassName, it)
        }

        e?.printStackTrace()
    }

    /**
     * Generate a SQUARE (both width & height smaller than the original bitmap) bitmap, and draw
     * the original bitmap inside a circle in the new bitmap, and the outside of the circle will
     * be transparent.
     * <pre>
     * +--+---+--+
     * |  |dst|  | src
     * +--+---+--+
     *
     * +---+
     * |   | src
     * +---+
     * |dst|
     * |   |
     * +---+
     * |   |
     * +---+
    </pre> *
     * @param bitmap the original bitmap.
     * @return the generated bitmap.
     */
    fun getRoundBitmap(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val s = Math.min(w, h)
        val d = 100

        val output = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = -0x1

        val x = if (w > h) (w - h) / 2 else 0
        val y = if (w > h) 0 else (h - w) / 2
        val rectSrc = Rect(x, y, x + s, y + s)
        val rectDst = Rect(0, 0, d, d)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle((d / 2).toFloat(), (d / 2).toFloat(), (d / 2).toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rectSrc, rectDst, paint)

        return output
    }

    /**
     * Generate a SQUARE bitmap with origninal bitmap inside acircle in the middle, and save it to
     * to disk using parameter `key` as the key to it.
     * @param bitmap the original bitmap.
     * @param filename The path to the generated PNG file.
     */
    fun getRoundBitmap(bitmap: Bitmap, filename: String) {
        val b = getRoundBitmap(bitmap)

        try {
            val fos = FileOutputStream(filename)
            b.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Create a screenshot and save the JPEG, to the place represent by `key`.
     * @param v The view whose content will be screenshot.
     * @param filename The path to the generated JPEG file.
     */
    fun takeScreenshot(v: View, filename: String) {
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache(true)
        val b = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false

        try {
            val fos = FileOutputStream(filename)
            b.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Test if a rectangle, designated by `rect`, and a circle, designated by
     * `cy` `cy` and `cr`, are collision.
     *
     *
     * <pre>
     * (.)         (.)         (.)
     * \          |          /
     * +---+   o---+   +-o-+   +---o   +---+
     * (.)- o   |   |   |   |   |   |   |   |   o-(.)
     * |   |   |   |   |   |   |   |   |   |
     * +---+   +---+   +---+   +---+   +---+
    </pre> *
     * @param rect The rectangle.
     * @param cx x coordinate of the center of the circle.
     * @param cy y coordinate of the center of the circle.
     * @param cr radius of the circle.
     * @return true if they collision, false otherwise.
     */
    fun rectCircleCollision(rect: Rect, cx: Int, cy: Int, cr: Int): Boolean {
        if (rect.contains(cx, cy)) {
            return true
        }

        val l = rect.left
        val r = rect.right
        val t = rect.top
        val b = rect.bottom

        val ax: Int
        val ay: Int

        ax = if (cx < l) l else if (cx > r) r else cx
        ay = if (cy < t) t else if (cy > b) b else cy

        val dx = ax - cx
        val dy = ay - cy

        return Math.sqrt((dx * dx + dy * dy).toDouble()) < cr
    }

    /**
     * Equivalent to `setTextViewHTML(TextView, String, (String)->Unit)`
     * @see setTextViewHTML
     */
    fun setTextViewHTML(activity: Activity, tv: TextView, resId: Int) {
        setTextViewHTML(tv, activity.resources.getString(resId)) { url->
            activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

    /**
     * Make text view with html tags clickable.
     * @param tv The text view.
     * @param html The html string.
     */
    fun setTextViewHTML(tv: TextView, html: String, listener: (String)->Unit) {
        val sequence = Html.fromHtml(html)
        val builder = SpannableStringBuilder(sequence)
        builder.getSpans(0, sequence.length, URLSpan::class.java).forEach { urlSpan->
            val start = builder.getSpanStart(urlSpan)
            val end   = builder.getSpanEnd  (urlSpan)
            val flags = builder.getSpanFlags(urlSpan)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View?) {
                    listener(urlSpan.url)
                }
            }

            builder.setSpan(clickableSpan, start, end, flags)
            builder.removeSpan(urlSpan)
        }

        tv.text = builder
        tv.movementMethod = LinkMovementMethod.getInstance()
    }
}
