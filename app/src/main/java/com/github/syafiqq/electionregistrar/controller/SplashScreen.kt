package com.github.syafiqq.electionregistrar.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.syafiqq.electionregistrar.R
import kotlinx.android.synthetic.main.activity_splash_screen.*
import timber.log.Timber
import java.util.concurrent.CountDownLatch

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashScreen : AppCompatActivity() {
    private var latch: CountDownLatch? = null
    private val OPEN_CAMERA_PERMISSION: Int = 1

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate [saveInstanceState]")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        mVisible = true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate [saveInstanceState]")
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        Timber.d("hide []")
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    override fun onStart() {
        Timber.d("onStart []")
        super.onStart()
        checkAppPermission()
    }

    override fun onRestart() {
        Timber.d("onRestart []")
        super.onRestart()
        checkAppPermission()
    }

    private fun checkAppPermission() {
        Timber.d("checkAppPermission []")
        latch = CountDownLatch(2)
        PermissionAsyncTask {
            Intent(this, RegistrarActivity::class.java).run {
                startActivity(this)
                finish()
            }
        }.execute(latch)
        askCameraPermission()
        dummyWaiting()
    }

    private fun dummyWaiting() {
        Timber.d("dummyWaiting []")
        Handler().postDelayed({ latch?.countDown() }, 500)
    }


    private fun askCameraPermission() {
        Timber.d("askCameraPermission []")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    OPEN_CAMERA_PERMISSION
                )
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    OPEN_CAMERA_PERMISSION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            this.latch?.countDown()
        }
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        Timber.d("delayedHide [delayMillis:$delayMillis]")

        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Timber.d("onRequestPermissionsResult [requestCode:$requestCode, permissions, grantResults]")

        when (requestCode) {
            OPEN_CAMERA_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    this.latch?.countDown()
                } else {
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}


class PermissionAsyncTask(private var callback: (() -> Unit)?) : AsyncTask<CountDownLatch, Unit, Unit>() {
    override fun doInBackground(vararg params: CountDownLatch?) {
        Timber.d("doInBackground [params]")
        val latch = params[0]!!
        try {
            latch.await()
        } catch (e: Exception) {
        }
        Thread.sleep(500)
    }

    override fun onPostExecute(result: Unit?) {
        Timber.d("onPostExecute [result]")
        callback?.invoke()
    }
}
