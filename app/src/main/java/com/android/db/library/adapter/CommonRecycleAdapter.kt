package com.android.db.library.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Common {@link RecycleView} adapter
 *
 * Created by DengBo on 12/03/2018.
 */

abstract class CommonRecycleAdapter<T: Any>(val context: Context): BaseRecycleAdapter<T, CommonHolder>() {

    abstract val layoutResId: Int

    final override fun onCreateViewHolderImpl(parent: ViewGroup?, viewType: Int): CommonHolder {
        var layoutId = layoutResId
        multiTypeSupport?.apply { layoutId = viewType }

        val rootView = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return CommonHolder(rootView)
    }

    final override fun onBindViewHolderImpl(holder: CommonHolder, position: Int) {
        setViewHolder(holder, data[position])
    }

    /**
     * Set specify holder[CommonHolder]
     *
     * @param holder view holder
     * @param data T
     */
    abstract fun setViewHolder(holder: CommonHolder, data: T)

}
