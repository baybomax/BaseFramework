package com.android.db.library.base

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.text.util.Linkify
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import com.android.db.library.R
import com.android.db.library.util.detached
import org.jetbrains.anko.*

/**
 * Base activity
 *
 * Created by DengBo on 08/03/2018.
 */
open class BaseActivity: FragmentActivity() {

    open val permissionsRequested = listOf<String>()

    open val defWgDrawableResId: Int? = null

    open val registerEventBus = false

    /**
     * Same as [.onCreate] but called for those activities created with
     * the attribute [android.R.attr.persistableMode] set to
     * `persistAcrossReboots`.
     *
     * @param savedInstanceState if the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [.onSaveInstanceState].
     * ***Note: Otherwise it is null.***
     *
     * @see .onCreate
     * @see .onStart
     * @see .onSaveInstanceState
     * @see .onRestoreInstanceState
     * @see .onPostCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (registerEventBus) {
            defaultEventBus.register(this)
        }

    }

    /**
     * Destroy all fragments and loaders.
     */
    override fun onDestroy() {
        if (defaultEventBus.isRegistered(this)) {
            defaultEventBus.unregister(this)
        }

        super.onDestroy()
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     *
     * **WARNING:** Do NOT call this function directly.
     */
    override fun onBackPressed() {
        if (waitingGlassDialog?.isShowing != true) {
            if (onBackPressedImpl()) {
                super.onBackPressed()
            }
        }
    }

    /**
     * Children classes should override this method to implement functionality of back key press.
     * @return true if it should call super.onBackPressed(); false will ignore this press action.
     */
    protected open fun onBackPressedImpl(): Boolean {
        return true
    }

    ////////////////////
    // Waiting glass

    /**
     * The waiting glass dialog.
     */
    private var waitingGlassDialog: Dialog? = null

    /**
     * Subclasses may override this method to create different UI for waiting glass.
     */
    protected open fun createWaitingGlass(message: String?): View = WaitingGlass().run {
        this.message = message
        createView(AnkoContext.Companion.create(this@BaseActivity))
    }

    /**
     * The waiting glass state.
     */
    fun isWaitingGlassShowing(): Boolean {
        return waitingGlassDialog?.isShowing == true
    }

    /**
     * Show the waiting glass
     *
     * @param message Set the message of the waiting glass
     * @param action If action is set, the waiting glass will dismiss after the action finished.
     * Note that the action will be executed in a separated thread.
     */
    fun showWaitingGlass(message: String? = null, action: (() -> Unit)? = null) {
        runOnUiThread {
            if (null != waitingGlassDialog) {
                return@runOnUiThread
            }

            waitingGlassDialog = Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen).apply {
                window.setBackgroundDrawable(ColorDrawable(0x33000000))
                setContentView(createWaitingGlass(message))
                setCancelable(false)
                show()
            }

            action?.let {
                Thread {
                    it()
                }.start()
            }
        }
    }

    /**
     * Force canceling the waiting glass dialog previously shown.
     */
    fun hideWaitingGlass() {
        waitingGlassDialog?.dismiss()
        waitingGlassDialog = null
    }

    /**
     * Default waiting glass.
     */
    inner class WaitingGlass : AnkoComponent<Context> {
        var message: String? = null
        @SuppressLint("NewApi")
        override fun createView(ui: AnkoContext<Context>): View = with(ui.owner) {
            detached {
                linearLayout {
                    lparams {
                        width = matchParent
                        height = matchParent
                    }
                    gravity = Gravity.CENTER
                    backgroundColor = 0x33000000
                    orientation = LinearLayout.VERTICAL

                    progressBar {
                        defWgDrawableResId?.let {
                            indeterminateDrawable = resources.getDrawable(it, null)
                        }
                    }.lparams {
                        width = dip(60)
                        height = dip(60)
                    }
                    textView {
                        text = message
                        textColor = Color.WHITE
                    }.lparams {
                        width = wrapContent
                        height = wrapContent
                        topMargin = dip(10)
                    }
                }
            }
        }
    }

    ////////////////////
    // Error dialog

    /**
     * Subclasses may override this method to create different UI for error dialog.
     */
    protected open fun createErrorDialog(message: String?): View = ErrorDialog().run {
        this.message = message
        createView(AnkoContext.Companion.create(this@BaseActivity))
    }

    /**
     * Show an error message dialog and then exit current activity.
     *
     * @param message The message to show in the dialog
     */
    fun showErrorDialog(message: String?, action: (() -> Unit)? = null) {
        runOnUiThread {
            AlertDialog.Builder(this)
                    .setView(createErrorDialog(message))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (null != action) {
                            action()
                        } else {
                            finish()
                        }
                    }
                    .create()
                    .apply {
                        window.setBackgroundDrawable(ColorDrawable(0x33000000))
                        requestWindowFeature(Window.FEATURE_NO_TITLE)
                        show()
                    }
        }
    }

    /**
     * The error dialog.
     */
    inner class ErrorDialog : AnkoComponent<Context> {
        var message: CharSequence? = null
        override fun createView(ui: AnkoContext<Context>): View = with(ui.owner) {
            detached {
                linearLayout {
                    lparams {
                        width = wrapContent
                        height = wrapContent
                    }

                    backgroundColor = 0x999999.opaque
                    gravity = Gravity.CENTER
                    orientation = LinearLayout.VERTICAL
                    padding = dip(15)

                    imageView {
                        setImageResource(R.drawable.ico_warning_white)
                    }.lparams {
                        width = dip(50)
                        height = dip(50)
                    }

                    view {
                        backgroundColor = 0xcccccc.opaque
                    }.lparams {
                        width = matchParent
                        height = dip(1)
                        margin = dip(5)
                    }

                    textView {
                        autoLinkMask = Linkify.ALL
                        gravity = Gravity.CENTER
                        text = message ?: "Error Warning!"
                        textColor = 0xFFFFFF.opaque
                        textSize = 16f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        margin = dip(10)
                    }
                }
            }
        }
    }

    ////////////////////
    // Permissions

    private val permissionsRequestCode = 123
    private val permissionsNotGranted = arrayListOf<String>()

    /**
     * Subclasses accessible
     */
    @Synchronized
    protected fun checkPermissions(permissionsGranted: (()->Unit)? = null) {
        permissionsNotGranted.clear()
        permissionsNotGranted.addAll(permissionsRequested.filter {
            PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, it)
        })
        if (permissionsNotGranted.size > 0) {
            requestPermissions()
        } else {
            permissionsGranted?.invoke()
        }
    }

    private fun requestPermissions() {
        if (permissionsNotGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsNotGranted.toTypedArray(),
                    permissionsRequestCode
            )
        }
    }

    /**
     * Subclasses may override this method to handle action when permissions all granted.
     */
    protected open fun permissionsGranted() {
//        recreate()
    }

    /**
     * Subclasses may override this method to show different request permission dialog.
     */
    protected open fun showRequestPermissionDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.please_grant_permissions)
                .setTitle(R.string.permissions)
                .setIcon(applicationInfo.loadIcon(packageManager))
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel) { _, _->
                    finish()
                }
                .setPositiveButton(R.string.grant_again) { _, _ ->
                    requestPermissions()
                }
                .show()
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode The request code passed in [.requestPermissions].
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [android.content.pm.PackageManager.PERMISSION_GRANTED]
     * or [android.content.pm.PackageManager.PERMISSION_DENIED]. Never null.
     *
     * @see .requestPermissions
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode != permissionsRequestCode) {
            return
        }

        for ((i, p) in grantResults.withIndex()) {
            if (PackageManager.PERMISSION_GRANTED == p) {
                permissionsNotGranted.remove(permissions[i])
            }
        }

        if (permissionsNotGranted.isNotEmpty()) {
            showRequestPermissionDialog()
        } else {
            permissionsGranted()
        }
    }
}
