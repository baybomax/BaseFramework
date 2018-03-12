package com.android.db.library.ui

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.db.library.util.visibleOrGone
import com.android.db.library.util.visibleOrInvisible
import org.jetbrains.anko.imageResource

/**
 * Common {@link ListView} viewHolder
 *
 * Created by DengBo on 12/03/2018.
 */

open class CommonHolder(private val rootView: View): RecyclerView.ViewHolder(rootView) {

    var views =  SparseArray<View>()

    /**
     * Find view from 'views' or 'rootView'
     *
     * @param viewId the item view resource id
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: View> getView(viewId: Int): T {
        var child = views.get(viewId)
        if (null == child) {
            child = rootView.findViewById(viewId)
            views.put(viewId, child)
        }
        return child as T
    }

    /**
     * Set specify view text content
     *
     * @param viewId the item view resource id
     * @param text the content sequence
     */
    open fun <T: TextView> setText(viewId: Int, text: CharSequence): CommonHolder {
        getView<T>(viewId).text = text
        return this
    }

    /**
     * Set specify view image resource
     *
     * @param viewId the item view resource id
     * @param resId the image resource id
     */
    open fun <T: ImageView> setImageResource(viewId: Int, resId: Int): CommonHolder {
        getView<T>(viewId).imageResource = resId
        return this
    }

    /**
     * Set specify view visibility
     *
     * @param viewId the item view resource id
     * @param visible visibleOrInvisible
     */
    open fun setViewVisibleOrInVisible(viewId: Int, visible: Boolean): CommonHolder {
        getView<View>(viewId).visibility = visible.visibleOrInvisible
        return this
    }

    /**
     * Set specify view visibility
     *
     * @param viewId the item view resource id
     * @param visible visibleOrGone
     */
    open fun setViewVisibleOrGone(viewId: Int, visible: Boolean): CommonHolder {
        getView<View>(viewId).visibility = visible.visibleOrGone
        return this
    }

    /**
     * Set specify view onClickListener
     *
     * @param viewId the item view resource id
     * @param onClick
     */
    open fun setOnClickListener(viewId: Int, onClick: (View)->Unit): CommonHolder {
        getView<View>(viewId).setOnClickListener {
            onClick(it)
        }
        return this
    }

}
