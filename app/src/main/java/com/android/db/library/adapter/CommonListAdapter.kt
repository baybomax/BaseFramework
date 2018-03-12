package com.android.db.library.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Common {@link ListView} adapter
 *
 * Created by DengBo on 12/03/2018.
 */

abstract class CommonListAdapter<T: Any>(val context: Context): BaseListAdapter<T>() {

    abstract val layoutResId: Int

    @Suppress("NAME_SHADOWING")
    final override fun getViewImpl(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val holder: CommonHolder
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(layoutResId, parent, false)
            holder = CommonHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as CommonHolder
        }

        setViewHolder(holder, data[position])

        return convertView!!
    }

    /**
     * Set specify holder[CommonHolder]
     *
     * @param holder view holder
     * @param data T
     */
    abstract fun setViewHolder(holder: CommonHolder, data: T)

}
