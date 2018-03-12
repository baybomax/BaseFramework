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
import android.widget.RelativeLayout
import android.widget.ScrollView

/**
 * Infinite fullscreen listView
 *
 * Created by DengBo on 12/03/2018.
 */

open class InfiniteFullscreenListView : ScrollView {

    companion object {
        private val TAG = "InfiniteFullscreenListView"
        private val VELOCITY_FACTOR_ADJUST = 0.0625f
        private val DELAY_SELECTION_INTERVAL = 800L
    }

    open val velocityFactor = 1.5f
    open val actualItemCount = 3

    var mCurrentActual = 0
    var mCurrentVirtual = 0
    var mScrollDragged = false

    private var pendingGoto = -1
    private var mSizeReady = false

    private lateinit var mContainer: RelativeLayout

    private var mDelaySelectHandler: Handler? = null
    private var mFlipDetector: FlipDetector? = null

    private var initY = 0f       // coordinate of last press DOWN
    private var lastScrollY = 0  // The scroll position when last press DOWN.

    /**
     * Register [OnItemSelectedListener] listener
     * that listens one item selection event.
     */
    var onItemSelectedListener: OnItemSelectedListener? = null

    private var localItemHeight = 0

    /**
     * This is important, should call after view initialized.
     * Calling this method will cause the [InfiniteFullscreenListView] view to refresh.
     * @param adapter new Adapter instance.
     */
    var adapter: InfiniteFullscreenListViewAdapter? = null
        set(value){
            field = value
            value?.apply {
                localItemHeight = itemHeight
                mContainer.removeAllViews()
                (0 until actualItemCount).forEach { i ->
                    mContainer.addView(getView(i, mContainer).apply {
                        layoutParams = RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, itemHeight).apply {
                            setMargins(0, i * itemHeight, 0, 0)
                        }
                    })
                }
            }
        }

    /**
     * init
     *
     */
    fun init() {
        overScrollMode = View.OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        isFillViewport = false

        mContainer = RelativeLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        super.addView(mContainer)

        //
        mDelaySelectHandler = Handler()

        mFlipDetector = FlipDetector(context, object : FlipDetector.SimpleOnFlipListener() {
            // Direction:
            //  0: idle;
            //  1: left/right
            //  2: up/down.
            var direction = 0

            override fun onDown(e: MotionEvent?): Boolean {
                // Log.i(TAG, "onDown()");
                e?.apply {
                    initY = e.y
                    lastScrollY = scrollY
                    direction = 0
                    mScrollDragged = true
                }
                return super.onDown(e)
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                // Log.i(TAG, "onFling(): mSpinEnabled=" + mSpinEnabled);
                val sy = scrollY
                val vy = velocityY * velocityFactor * VELOCITY_FACTOR_ADJUST
                if (direction == 2) {
                    gotoItem(calculateNextTarget(sy - vy), true)
                }
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                // Log.i(TAG, "onScroll(): mSpinEnabled=" + mSpinEnabled);
                if (0 == direction) {
                    direction = if (Math.abs(distanceX) > Math.abs(distanceY)) 1 else 2
                } else if (2 == direction) {
                    scrollTo(0, lastScrollY + (initY - (e2?.y ?: 0f)).toInt())
                }
                return true
            }

            override fun onUp(e: MotionEvent) {
                val sy = scrollY
                // Log.i(TAG, "onUp(): mSpinEnabled=" + mSpinEnabled + "; sy=" + sy);
                gotoItem(calculateNextTarget(sy.toFloat()), true)
                super.onUp(e)
            }

            /**
            * This may be called in other place than {@link GestureDetector#onTouchEvent(MotionEvent)}
            * and also is preceeded by a call to
            * {@link android.view.GestureDetector.OnGestureListener#onSingleTapUp(MotionEvent)}
            * No need to mark as consumed.
            */
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                //
                mScrollDragged = false
                onItemSelectedListener?.onSingleTapUp(this@InfiniteFullscreenListView, mCurrentVirtual, mCurrentActual)
                return true
            }
        })
    }

    constructor(context: Context): super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init()
    }


    /**
     * The runnable to send delay selection event
     * @see [OnItemSelectedListener.onItemSelectedDelay]
     */
    private val mDelaySelectSender = Runnable {
        mScrollDragged = false
        onItemSelectedListener?.onItemSelectedDelay(this, mCurrentVirtual, mCurrentActual)
    }

    /**
     * The adding child view operation of wheel spinner is disabled.
     * @param child Ignored
     */
    override fun addView(child: View) {
        throw UnsupportedOperationException("Please use InfiniteFullscreenListView adapter")
    }

    /**
     * The adding child view to container layout
     *
     * @param child the child view
     */
    fun addChildView(child: View) {
        mContainer.addView(child)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mFlipDetector?.onTouchEvent(ev) ?: false
    }

    @SuppressLint("LongLogTag")
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.w(TAG, "onSizeChanged(): h=" + h + "; mContainer.height=" + mContainer.height)

        if(h != oldh && adapter?.itemHeight == LinearLayout.LayoutParams.MATCH_PARENT){
            localItemHeight = h
            val virtual = if (mCurrentVirtual == 0) {
                mCurrentVirtual + 1
            } else {
                mCurrentVirtual
            }
            onSizeChanged(h, (virtual - 1) % 3, virtual - 1)
            onSizeChanged(h, (virtual    ) % 3,       virtual    )
            onSizeChanged(h, (virtual + 1) % 3, virtual + 1)

            mContainer.requestLayout()
        }

        if (pendingGoto >= 0) {
            postDelayed({
                // scrollTo(0, (mContainer.getHeight() - h) / 2);
                gotoItem(pendingGoto, false)
                pendingGoto = -1
            }, 100L)
        }
        mSizeReady = true
    }

    /**
     * Change item height and set margin
     * @param h the new height
     * @param index the item index
     * @param count the count to top
     */
    private fun onSizeChanged(h: Int, index: Int, count: Int) {
        mContainer.getChildAt(index).layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h).apply {
            setMargins(0, h * count, 0, 0)
        }
    }

    @SuppressLint("LongLogTag")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setContainerSize(width)

        Log.w(TAG, "onLayout(): mContainer.height=" + mContainer.height)
    }

    /**
     *
     */
    @SuppressLint("LongLogTag")
    private fun setContainerSize(myW: Int) {
        var childrenHeight = 0
        val padding = 0
        adapter?.apply {
            childrenHeight = localItemHeight * totalCount
//            Log.w(TAG, "setContainerSize.childrenHeight - $childrenHeight")
//            Log.w(TAG, "setContainerSize.virtualCount   - $totalCount")
        }
        mContainer.setPadding(0, padding, 0, padding)
        mContainer.measure(
                ViewGroup.getChildMeasureSpec(View.MeasureSpec.EXACTLY, 0, myW),
                ViewGroup.getChildMeasureSpec(View.MeasureSpec.EXACTLY, padding, childrenHeight))
    }

    /**
     * This method is public to use, to calculate the
     * container' size when the scrollview will not request
     * layout itself, should call when onLayout() not invoke
     * if you need request layout to calculate the container size.
     */
//    fun setContainerSize() {
//        setContainerSize(width)
//    }

    /**
     * Calculate which item should goto, when user releases the finger.
     * @param endScroll The scroll amount when scroll stops.
     * @return The index the item should goto.
     */
    private fun calculateNextTarget(endScroll: Float): Int {
        return adapter?.run {
            val T = height
            val I = localItemHeight
            val M = mContainer.paddingTop
            val S = endScroll.toInt()
            val L = S + T / 2 - M

            L / I
        } ?: 0
    }

    /**
     * Scroll to the item view specified by index
     * @param index The index of the item to scroll to, if less than zero or
     * greater than item count - 1, will automatically adjust.
     * @param smooth true if scroll to item smoothly, false otherwise.
     * @param specify true if specify index to scrollTo by outer, default is false.
     */
    fun gotoItem(index: Int, smooth: Boolean, specify: Boolean = false) {
        if (!mSizeReady) {
            pendingGoto = index
            return
        }
        var a = index

        val M = mContainer.paddingTop
        val T = height
        val I = localItemHeight
        val n = adapter?.virtualCount ?: 0

        a = if (a <= 0 || n <= 0) 0 else if (a >= n) n - 1 else a
        val L = I * a + I / 2
        val S = L + M - T / 2

        //        Log.i(TAG, "a = " + a + "; S = " + S);
        //        Log.i(TAG, "    M=" + M);
        //        Log.i(TAG, "    T=" + T);
        //        Log.i(TAG, "    I=" + I);
        //        Log.i(TAG, "    n=" + n);
        //        Log.i(TAG, "    L=" + L);

        val changed = mCurrentVirtual != a

        if (smooth) {
            smoothScrollTo(0, S)
        } else {
            scrollTo(0, S)
        }

        if (changed) {
            changeItem(a, specify)
        } else {
            mScrollDragged = false
        }
    }

//    /**
//     * Scroll to the specify postion
//     * @param smooth
//     * @param S
//     */
//    private fun scrollTo(smooth: Boolean, S: Int) {
//
//    }

    /**
     * Change item location then call listener
     * @param virtual The virtual item to go
     * @param specify True/False
     * @see gotoItem
     */
    private fun changeItem(virtual: Int, specify: Boolean) {
        val lastVirtual = mCurrentVirtual
        mCurrentVirtual = virtual
        mCurrentActual = mCurrentVirtual % 3

        ///////////////
        // relocate
        changeItemViewMargins(mCurrentActual, mCurrentVirtual)
        changeItemViewMargins((mCurrentVirtual - 1) % 3, mCurrentVirtual - 1)
        changeItemViewMargins((mCurrentVirtual + 1) % 3, mCurrentVirtual + 1)

        /////////////////////////////////////
        /// listener callbacks <ordered !! >

        if (specify) {
            onItemSelectedListener?.onItemRelocated(this, mCurrentVirtual, mCurrentVirtual % 3)
            onItemSelectedListener?.onItemRelocated(this, mCurrentVirtual - 1, (mCurrentVirtual - 1) % 3)
            onItemSelectedListener?.onItemRelocated(this, mCurrentVirtual + 1, (mCurrentVirtual + 1) % 3)
        } else {
            if (lastVirtual > mCurrentVirtual) {
                onItemSelectedListener?.onItemLeaved(this, mCurrentVirtual + 1, (mCurrentVirtual + 1) % 3)
                onItemSelectedListener?.onItemRelocated(this, mCurrentVirtual - 1, (mCurrentVirtual - 1) % 3)
            } else {
                onItemSelectedListener?.onItemLeaved(this, mCurrentVirtual - 1, (mCurrentVirtual - 1) % 3)
                onItemSelectedListener?.onItemRelocated(this, mCurrentVirtual + 1, (mCurrentVirtual + 1) % 3)
            }
        }
        onItemSelectedListener?.onItemSelected(this, mCurrentVirtual, mCurrentActual)

        mDelaySelectHandler?.removeCallbacks(mDelaySelectSender)
        mDelaySelectHandler?.postDelayed(mDelaySelectSender, DELAY_SELECTION_INTERVAL)
    }

    /**
     * Change the container item view' margin, same to translate
     *
     * @param actual The item index at container 0, 1, 2
     * @param virtual The item should set how many itemHeights margin.
     */
    private fun changeItemViewMargins(actual: Int, virtual: Int) {
        if (actual < 0 || virtual < 0) { return }
        //
        mContainer.getChildAt(actual)?.apply {
            layoutParams = (layoutParams as RelativeLayout.LayoutParams).apply {
                setMargins(0, virtual * localItemHeight, 0, 0)
            }
        }
    }

    /**
     * The adapter to feed view to this infinite fullscreen listView.
     */
    interface InfiniteFullscreenListViewAdapter {

        /**
         * @return The total count of items.
         */
        var totalCount: Int

        /**
         * @return The virtual count of items.
         */
        var virtualCount: Int

        /**
         * @return The height of each item.
         */
        val itemHeight: Int

        /**
         * No, you are not provided with convert view. Just create a new one.
         * @param position The position of generated view in parent view.
         * @param container The parent view of generated View.
         * @return The created view.
         */
        fun getView(position: Int, container: RelativeLayout): View
    }

    /**
     * Listens to the item selection event.
     */
    interface OnItemSelectedListener {

        /**
         * Notifies that one item has been selected.
         * @param listView This [InfiniteFullscreenListView] instance.
         * @param virtual The index of virtual selected item.
         * @param actual The index of actual selected item.
         */
        fun onItemSelected(listView: InfiniteFullscreenListView, virtual: Int, actual: Int)

        /**
         * Notifies that one item has been selected a while earlier.
         * @param listView This [InfiniteFullscreenListView] instance.
         * @param virtual The index of virtual selected item.
         * @param actual The index of actual selected item.
         */
        fun onItemSelectedDelay(listView: InfiniteFullscreenListView, virtual: Int, actual: Int)

        /**
         * Notifies that one actual item has been relocated to new location to prepare for user
         * traveling.
         * <pre>
         *     |     |          |     |
         *     | ... |          | ... |
         *     +-----+          |     |
         * n   |  A  |          |     |
         *     +-----+          +-----+
         * n+1 |  B  | ==>  n+1 |  B  |
         *     +-----+          +-----+
         * n+2 |  C  |      n+2 |  C  |
         *     +-----+          +-----+
         *     | ... |      n+3 |  A  | <- be work
         *     |     |          +-----+
         *                      | ... |
         *                      |     |
         *  A is relocated to new place
         *  onItemRelocated() will be called parameter virtual=n+3, actual=A
         *
         * </pre>
         * @param listView This [InfiniteFullscreenListView] instance.
         * @param virtual The index of virtual relocated item.
         * @param actual The index of actual relocated item.
         */
        fun onItemRelocated(listView: InfiniteFullscreenListView, virtual: Int, actual: Int)

        /**
         * Notifies that one actual item has been leaved to new location.
         * <pre>
         *     |     |          |     |
         *     | ... |          | ... |
         *     +-----+          +-----+
         * n   |  A  |      n+1 |  B  | <- be work
         *     +-----+          +-----+
         * n+1 |  B  | ==>  n+2 |  C  |
         *     +-----+          +-----+
         * n+2 |  C  |      n+3 |  A  |
         *     +-----+          +-----+
         *     | ... |          | ... |
         *     |     |          |     |
         *
         *
         *  B is leaved to new place
         *  onItemLeaved() will be called parameter virtual=n+1, actual=B
         *
         * </pre>
         * @param listView This [InfiniteFullscreenListView] instance.
         * @param virtual The index of virtual leaved item.
         * @param actual The index of actual leaved item.
         */
        fun onItemLeaved(listView: InfiniteFullscreenListView, virtual: Int, actual: Int)

        /**
         * This will be called when touch down event @see{@link GestureDetector#onTouchEvent(MotionEvent)
         * #TouchEvent.onDown()} trigger, and if dragged is true, present that current item was dragged.
         * Also will be called when touch up event @see{@link GestureDetector#onTouchEvent(MotionEvent)
         * #TouchEvent.onUp()} trigger, and if dragged is false, present that current item was released.
         * @param listView This [InfiniteFullscreenListView] instance
         * @param virtual The index of virtual drag item.
         * @param actual The index of actual drag item.
         * @param dragged T/F when dragged will be true, otherwise false.
         */
        fun onItemDragged(listView: InfiniteFullscreenListView, virtual: Int, actual: Int)

        /**
         * This may be called in other place than {@link GestureDetector#onTouchEvent(MotionEvent)}
         * and also is preceeded by a call to
         * {@link android.view.GestureDetector.OnGestureListener#onSingleTapUp(MotionEvent)}
         * No need to mark as consumed.
         * @param listView This [InfiniteFullscreenListView] instance
         * @param virtual The index of current virtual
         * @param actual The index of current actual
         */
        fun onSingleTapUp(listView: InfiniteFullscreenListView, virtual: Int, actual: Int)
    }

}
