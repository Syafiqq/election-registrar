package com.github.syafiqq.electionregistrar.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.syafiqq.electionregistrar.model.view.QRScannerView
import com.google.zxing.Result

/**
 * A placeholder fragment containing a simple view.
 */
class RegistrarActivityFragment : Fragment(), QRScannerView.ResultHandler {
    private var mScannerView: QRScannerView? = null
    private var mCameraId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mScannerView = QRScannerView(this.activity!!)
        return mScannerView as QRScannerView
    }

    override fun handleResult(image: ByteArray?, result: Result) {
        Toast.makeText(this.context!!, result.text, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.mResultHandler = this
        mScannerView?.startCamera(mCameraId)
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }
}