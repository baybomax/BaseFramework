package com.android.db.library.ui

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Base {@link RecycleView} adapter
 *
 * Created by DengBo on 12/03/2018.
 */

abstract class BaseRecycleAdapter<T: Any, R: RecyclerView.ViewHolder>: RecyclerView.Adapter<R>() {

    open var data = mutableListOf<T>()

    open var multiTypeSupport: MultiTypeSupport<T>? = null

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

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        multiTypeSupport?.apply {
            return getLayoutId(data[position], position)
        }

        return super.getItemViewType(position)
    }

    final override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): R {
        return onCreateViewHolderImpl(parent, viewType)
    }

    final override fun onBindViewHolder(holder: R, position: Int) {
        onBindViewHolderImpl(holder, position)
    }

    /**
     * Impl onCreateViewHolder
     *
     * @see onCreateViewHolder
     */
    abstract fun onCreateViewHolderImpl(parent: ViewGroup?, viewType: Int): R

    /**
     * Impl onBindViewHolder
     *
     * @see onBindViewHolder
     */
    abstract fun onBindViewHolderImpl(holder: R, position: Int)

}
