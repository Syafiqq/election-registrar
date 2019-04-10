package com.github.syafiqq.electionregistrar.controller

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.syafiqq.electionregistrar.model.view.QRScannerView
import com.google.zxing.Result
import timber.log.Timber

/**
 * A placeholder fragment containing a simple view.
 */
class RegistrarActivityFragment : Fragment(), QRScannerView.ResultHandler {
    private var mScannerView: QRScannerView? = null
    private var mCameraId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Timber.d("onCreateView [inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?]")

        mScannerView = QRScannerView(this.activity!!)
        return mScannerView as QRScannerView
    }

    override fun handleResult(image: ByteArray?, result: Result) {
        Timber.d("handleResult [image: ByteArray?, result: Result]")

        Toast.makeText(this.context!!, result.text, Toast.LENGTH_SHORT).show()

        val handler = Handler()
        handler.postDelayed({ mScannerView?.resumeCameraPreview(this@RegistrarActivityFragment) }, 2000)
    }

    override fun onResume() {
        Timber.d("onResume")

        super.onResume()
        mScannerView?.mResultHandler = this
        mScannerView?.startCamera(mCameraId)
    }

    override fun onPause() {
        Timber.d("onPause")

        super.onPause()
        mScannerView?.stopCamera()
    }
}