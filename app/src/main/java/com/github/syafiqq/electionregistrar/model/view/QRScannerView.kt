package com.github.syafiqq.electionregistrar.model.view


import android.content.Context
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import com.github.syafiqq.electionregistrar.model.finder.SquareFinderView
import com.google.zxing.Result
import me.dm7.barcodescanner.core.BarcodeScannerView
import me.dm7.barcodescanner.core.IViewFinder
import java.io.ByteArrayOutputStream


class QRScannerView : BarcodeScannerView {
    var mResultHandler: ResultHandler? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    interface ResultHandler {
        fun handleResult(image: ByteArray?, result: Result)
    }

    override fun createViewFinderView(context: Context?): IViewFinder {
        return SquareFinderView(context!!)
    }

    internal fun <T> send(obj: T, resultHandler: ResultHandler) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            // Stopping the preview can take a little long.
            // So we want to set result handler to null to discard subsequent calls to
            // onPreviewFrame.
            mResultHandler = null

            stopCameraPreview()
            resultHandler.run {
                when (this) {
                    else -> {
                    }
                }
            }
        }
    }

    @Synchronized
    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (mResultHandler == null) {
            return
        }
        try {
            val parameters = camera.parameters
            val format = parameters.previewFormat
            if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {
                val w = parameters.previewSize.width
                val h = parameters.previewSize.height
                val yuvimage = YuvImage(data, format, w, h, null)
                val rect = getFramingRectInPreview(w, h)
                val baos = ByteArrayOutputStream()
                yuvimage.compressToJpeg(rect, 100, baos)
                val jdata = baos.toByteArray()
                if (jdata != null) {
                    send(jdata, mResultHandler!!)
                }
            }
        } catch (e: RuntimeException) {
            // TODO: Terrible hack. It is possible that this method is invoked after camera is released.
            Log.e(TAG, e.toString(), e)
        }
    }

    companion object {
        private val TAG = "QRScannerView"
    }
}
