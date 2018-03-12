package com.android.db.library.view

import android.content.Context
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Inspired by [android.view.GestureDetector]
 *
 * Created by DengBo on 12/03/2018.
 */

class FlipDetector(context: Context, private val onFlipListener: OnFlipListener?) {

    private var mUpEventConsumed = false

    private val mInnerListener = InnerListener()
    private val mInnerDetector: InnerDetector

    init {
        mInnerDetector = InnerDetector(context, mInnerListener)
        mInnerDetector.setOnDoubleTapListener(mInnerListener)
    }

    interface OnFlipListener: GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        /**
         * Notified when a tap occurs with the up [MotionEvent]
         * that triggered it
         */
        fun onUp(e: MotionEvent)
    }

    /**
     * Convenient class which implements [OnFlipListener], with default
     * implementations of all the methods
     */
    open class SimpleOnFlipListener: OnFlipListener {
        override fun onUp                   (e:  MotionEvent )          {}
        override fun onShowPress            (e:  MotionEvent?)          {}
        override fun onLongPress            (e:  MotionEvent?)          {}
        override fun onDown                 (e:  MotionEvent?): Boolean { return false }
        override fun onDoubleTap            (e:  MotionEvent?): Boolean { return false }
        override fun onSingleTapUp          (e:  MotionEvent?): Boolean { return false }
        override fun onDoubleTapEvent       (e:  MotionEvent?): Boolean { return false }
        override fun onSingleTapConfirmed   (e:  MotionEvent?): Boolean { return false }
        override fun onScroll               (e1: MotionEvent?,
                                             e2: MotionEvent?,
                                             distanceX: Float,
                                             distanceY: Float): Boolean { return false }
        override fun onFling                (e1: MotionEvent?,
                                             e2: MotionEvent?,
                                             velocityX: Float,
                                             velocityY: Float): Boolean { return false }
    }

    /**
     * Inner listener to catch all events that can be listened
     * And also to send them onto new listener
     */
    private inner class InnerListener: GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        override fun onShowPress(e: MotionEvent?) {
            onFlipListener?.onShowPress(e)
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            mUpEventConsumed = true
            return onFlipListener?.onSingleTapUp(e) ?: false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return onFlipListener?.onDown(e) ?: false
        }

        // WARNING! android code doesn't care if e1/e2 is null, but kotlin does.
        // so, this listener must implement e1 as optional. or checkParameterIsNotNull
        // will happen!
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            mUpEventConsumed = true
            return null != e1 && (onFlipListener?.onFling(e1, e2, velocityX, velocityY) ?: false)
        }

        // WARNING! android code doesn't care if e1/e2 is null, but kotlin does.
        // so, this listener must implement e1 as optional. or checkParameterIsNotNull
        // will happen!
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return null != e1 && (onFlipListener?.onScroll(e1, e2, distanceX, distanceY) ?: false)
        }

        override fun onLongPress(e: MotionEvent?) {
            onFlipListener?.onLongPress(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            mUpEventConsumed = true
            return onFlipListener?.onDoubleTap(e) ?: false
        }

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            if (e?.action == MotionEvent.ACTION_UP) {
                mUpEventConsumed = true
            }
            return onFlipListener?.onDoubleTapEvent(e) ?: false
        }

        /**
         * This may be called in other place than [GestureDetector.onTouchEvent]
         * and also is preceeded by a call to
         * [android.view.GestureDetector.OnGestureListener.onSingleTapUp]
         * No need to mark as consumed.
         */
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            // mUpEventConsumed = true;
            return onFlipListener?.onSingleTapConfirmed(e) ?: false
        }

    }

    /**
     * Inner detector class to do the real work.
     */
    private inner class InnerDetector: GestureDetector {

        constructor(context: Context, listener: GestureDetector.OnGestureListener): super(context, listener)

        constructor(context: Context, listener: GestureDetector.OnGestureListener, handler: Handler): super(context, listener, handler)

        override fun onTouchEvent(ev: MotionEvent?): Boolean {
            mUpEventConsumed = false
            var handled = super.onTouchEvent(ev)
            if (ev?.action == MotionEvent.ACTION_UP && !mUpEventConsumed) {
                onFlipListener?.onUp(ev)
            } else if (ev?.action == MotionEvent.ACTION_DOWN) {
                handled = true
            }
            return handled
        }
    }

    /**
     * Outer onTouchEvent delegate
     */
    fun onTouchEvent(ev: MotionEvent?): Boolean {
        return mInnerDetector.onTouchEvent(ev)
    }
}
