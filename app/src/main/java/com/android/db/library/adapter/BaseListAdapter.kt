package com.android.db.library.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * Base {@link ListView} adapter
 *
 * Created by DengBo on 12/03/2018.
 */

abstract class BaseListAdapter<T: Any>: BaseAdapter() {

    open var data = mutableListOf<T>()

    /**
     * NotifyDataSetChanged when load more data
     *
     * @param _data more data
     */
    open fun notifyDataSetLoadMore(_data: List<T>) {
        data.addAll(_data)
        notifyDataSetChanged()
    }

    /**
     * NotifyDataSetChanged when refresh data
     *
     * @param _data refresh data
     */
    open fun notifyDataSetRefresh(_data: List<T>) {
        data.clear()
        data.addAll(_data)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    final override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getViewImpl(position, convertView, parent)
    }

    /**
     * Impl getView
     *
     * @see getView
     */
    abstract fun getViewImpl(position: Int, convertView: View?, parent: ViewGroup?): View

}
