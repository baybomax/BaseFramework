package com.android.db.library.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView

/**
 * Wheel Spinner
 *
 * Created by DengBo on 12/03/2018.
 */

open class WheelSpinner: ScrollView {

    companion object {
        private val TAG = "WheelSpinner"
        private val VELOCITY_FACTOR_ADJUST = 0.0625f
        private val DELAY_SELECTION_INTERVAL = 800L
    }

    /**
     * The adapter to feed view to this wheel spinner
     */
    interface WheelSpinnerAdapter {

        /**
         * @return The count of items
         */
        var count: Int

        /**
         * No, all items have same height, no exception
         * @return The height of each item
         */
        var itemHeight: Int

        /**
         * No, you are not provided with convert view. Just create a new one.
         * @param position Position of this view.
         * @param container The parent view of generated View.
         * @return The created view.
         */
        fun getView(position: Int, container: LinearLayout): View

    }

    /**
     * Listens to the item selection event.
     */
    interface OnItemSelectedListener {

        /**
         * Notifies that one item has been selected.
         * @param spinner This wheel spinner instance.
         * @param index The index of selected item.
         */
        fun onItemSelected(spinner: WheelSpinner, index: Int)

        /**
         * Notifies that one item has been selected a while earlier.
         * @param spinner This wheel spinner instance.
         * @param index The index of selected item.
         */
        fun onItemSelectedDelay(spinner: WheelSpinner, index: Int)
    }

    /**
     * The adapter.
     * This is important, should call after view initialized.
     * Calling this method will cause the WheelSpinner view to refresh.
     */
    open var adapter: WheelSpinnerAdapter? = null
        set(value){
            // Check if adapter is available.
            field = value?.apply {
                localItemHeight = itemHeight
                // Add child views.
                mContainer.removeAllViews()
                for (i in 0 until count) {
                    val v = getView(i, mContainer)
                    v.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, itemHeight)
                    mContainer.addView(v)
                }
            }
        }

    private var localItemHeight = 0

    /**
     * Set the alpha (transparent) value of the items which are not active.
     * Default is 0.1f
     */
    open var inactiveAlpha = 1f

    /**
     * Set the factor which will applied to the velocity.
     * Default is 0.2f
     */
    open var velocityFactor = 0.2f

    /**
     * Set if this wheel spinner has no border.
     */
    open var noBorder = false

    /**
     *
     */
    open var enabledSpin = true

    /**
     *
     */
    open var disableNextSpin = false

    /**
     *
     */
    open var onItemSelectedListener: OnItemSelectedListener? = null

    protected lateinit var mContainer: LinearLayout
    protected lateinit var mFlipDetector: FlipDetector

    var currentIndex: Int = -1
        protected set

    protected var pendingGoto = -1
    protected var mSizeReady = false

    protected var mDelaySelectHandler: Handler? = null

    protected val mDelaySelectSender = Runnable {
        onItemSelectedListener?.onItemSelectedDelay(this@WheelSpinner, currentIndex)
    }

    private var initY = 0f          // coordinate of last press DOWN
    private var lastScrollY = 0     // The scroll position when last press DOWN.


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * The real constructor.
     */
    private fun init() {
        // set super variables
        isFillViewport = false
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = View.OVER_SCROLL_NEVER

        // Initialize container.
        mContainer = LinearLayout(context)
        mContainer.orientation = LinearLayout.VERTICAL
        mContainer.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        super.addView(mContainer)

        // Initialize handler
        mDelaySelectHandler = Handler()

        // Initialize FlipDetector
        mFlipDetector = FlipDetector(context, object : FlipDetector.SimpleOnFlipListener() {
            // Direction:
            //      0: idle;
            //      1: left/right
            //      2: up/down.
            var direction = 0

            override fun onDown(e: MotionEvent?): Boolean {
                 Log.i(TAG, "onDown()")
                // initX = e.getX();
                e?.apply {
                    initY = e.y
                    lastScrollY = scrollY
                    direction = 0
                    mDelaySelectHandler?.removeCallbacks(mDelaySelectSender)
                }
                return super.onDown(e)
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                 Log.i(TAG, "onFling(): enabledSpin=$enabledSpin")
                if (enabledSpin) {
                    val sy = scrollY
                    val vy = velocityY * velocityFactor * VELOCITY_FACTOR_ADJUST
                    if (direction == 2) {
                        gotoItem(calculateNextTarget(sy - vy), true)
                    }
                }
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                 Log.i(TAG, "onScroll(): enabledSpin=$enabledSpin")
                if (enabledSpin) {
                    if (0 == direction) {
                        direction = if (Math.abs(distanceX) > Math.abs(distanceY)) 1 else 2
                    } else if (2 == direction) {
                        scrollTo(0, lastScrollY + (initY - (e2?.y ?: 0f)).toInt())
                    }
                }
                return true
            }

            override fun onUp(e: MotionEvent) {
                val sy = scrollY
                Log.i(TAG, "onUp(): enabledSpin=$enabledSpin; sy=$sy")
                if (enabledSpin) {
                    gotoItem(calculateNextTarget(sy.toFloat()), true)
                }
                super.onUp(e)
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return onClick(e)
            }
        })
    }

    protected fun onClick(e: MotionEvent?): Boolean {
        return false
    }

    private fun setContainerSize(myW: Int) {
        var childrenHeight = 0
        var padding = 0
        if (!noBorder && null != adapter) {
            // int I = adapter.getItemHeight();
            val T = height
            val M = T / 2 // (T - I) / 2;

            padding = M
            childrenHeight = localItemHeight * adapter!!.count
        }
        mContainer.setPadding(0, padding, 0, padding)
        mContainer.measure(
                ViewGroup.getChildMeasureSpec(View.MeasureSpec.EXACTLY, 0, myW),
                ViewGroup.getChildMeasureSpec(View.MeasureSpec.EXACTLY, padding, childrenHeight))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if(h != oldh && adapter?.itemHeight == LinearLayout.LayoutParams.MATCH_PARENT){
            localItemHeight = h
            0.until(mContainer.childCount).forEach { i->
                mContainer.getChildAt(i).layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, h)
            }
            mContainer.requestLayout()
        }

        mSizeReady = true
        if (pendingGoto >= 0) {
            postDelayed({
                // scrollTo(0, (mContainer.getHeight() - h) / 2);
                gotoItem(pendingGoto, false)
                pendingGoto = -1
                onScrollChanged()
            }, 100L)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setContainerSize(width)
        Log.d(TAG, "onLayout(): mContainer.height=" + mContainer.height)
    }

    /**
     * The adding child view operation of wheel spinner is disabled.
     * @param child Ignored
     */
    override fun addView(child: View) {
        throw UnsupportedOperationException("Please use WheelSpinner adapter")
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mFlipDetector.onTouchEvent(ev)
    }

    /**
     * Calculate which item should goto, when user releases the finger.
     * @param endScroll The scroll amount when scroll stops.
     * @return The index the item should goto.
     */
    private fun calculateNextTarget(endScroll: Float): Int = adapter?.run {

        val T = height
        val I = localItemHeight
        val n = count
        val M = mContainer.paddingTop
        val S = endScroll.toInt()
        val L = S + T / 2 - M

        L / I
    } ?: 0


    /**
     * This is called in response to an internal scroll in this view (i.e., the
     * view scrolled its own contents). This is typically as a result of
     * [.scrollBy] or [.scrollTo] having been
     * called.
     *
     * @param l    Current horizontal scroll origin.
     * @param t    Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollChanged()
    }

    fun onScrollChanged() = adapter?.apply {

        val S = scrollY
        val M = mContainer.paddingTop
        val T = height
        val I = localItemHeight
        val n = count
        val L = S + T / 2 - M

        val a = L / I
        for (i in 0 until n) {
            val v = mContainer.getChildAt(i)
            v.alpha = if (i == a) 1.0f else inactiveAlpha
        }
    }

    /**
     * Scroll to the item view specified by index
     * @param index The index of the item to scroll to, if less than zero or
     * greater than item count - 1, will automatically adjust.
     * @param smooth true if scroll to item smoothly, false otherwise.
     */
    fun gotoItem(index: Int, smooth: Boolean) {
        if (!mSizeReady || null == adapter || index < 0) {
            pendingGoto = index
            return
        }
        var a = index

        val M = mContainer.paddingTop
        val T = height
        val I = localItemHeight
        val n = adapter!!.count

        a = if (a < 0) 0 else if (a >= n) n - 1 else a
        val L = I * a + I / 2
        val S = L + M - T / 2

        //        Log.i(TAG, "a = " + a + "; S = " + S);
        //        Log.i(TAG, "    M=" + M);
        //        Log.i(TAG, "    T=" + T);
        //        Log.i(TAG, "    I=" + I);
        //        Log.i(TAG, "    n=" + n);
        //        Log.i(TAG, "    L=" + L);

        val changed = currentIndex != a
        currentIndex = a

        if (smooth) {
            smoothScrollTo(0, S)
        } else {
            scrollTo(0, S)
        }

        if (disableNextSpin) {
            disableNextSpin = false
        } else if ( changed) {
            onItemSelectedListener?.onItemSelected(this, currentIndex)
            mDelaySelectHandler?.postDelayed(mDelaySelectSender, DELAY_SELECTION_INTERVAL)
        }
    }
}
