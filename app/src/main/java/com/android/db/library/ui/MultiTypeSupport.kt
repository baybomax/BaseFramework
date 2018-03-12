package com.android.db.library.ui

/**
 * Multi type support
 *
 * Created by DengBo on 12/03/2018.
 */

interface MultiTypeSupport<in T> {

    /**
     * Get the different type layout resource id
     *
     * @param data T
     * @param position Int
     */
    fun getLayoutId(data: T, position: Int): Int

}
