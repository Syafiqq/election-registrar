package com.github.syafiqq.electionregistrar.model.view


import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import com.github.syafiqq.electionregistrar.model.finder.SquareFinderView
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import me.dm7.barcodescanner.core.BarcodeScannerView
import me.dm7.barcodescanner.core.DisplayUtils
import me.dm7.barcodescanner.core.IViewFinder
import timber.log.Timber
import java.util.*


class QRScannerView : BarcodeScannerView {
    var mResultHandler: ResultHandler? = null
    private val hints: EnumMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)

    private val reader: QRCodeReader = QRCodeReader()

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        hints[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
    }

    override fun createViewFinderView(context: Context?): IViewFinder {
        return SquareFinderView(context!!)
    }

    @Synchronized
    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        var data = data
        if (mResultHandler == null) {
            return
        }
        try {
            val parameters = camera.parameters
            val size = parameters.previewSize
            var width = size.width
            var height = size.height

            if (DisplayUtils.getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
                val rotationCount = rotationCount
                if (rotationCount == 1 || rotationCount == 3) {
                    val tmp = width
                    width = height
                    height = tmp
                }
                data = getRotatedData(data, camera)
            }

            var rawResult: Result? = null
            var rect = getFramingRectInPreview(width, height) ?: return
            rect = getScaledFramingRect(rect)
            val source = buildLuminanceSource(data, rect)

            if (source != null) {
                var bitmap = BinaryBitmap(HybridBinarizer(source))
                try {
                    rawResult = reader.decode(bitmap, hints)
                } catch (re: ReaderException) {
                    // continue
                } catch (npe: NullPointerException) {
                    // This is terrible
                } catch (aoe: ArrayIndexOutOfBoundsException) {

                } finally {
                    reader.reset()
                }

                if (rawResult == null) {
                    val invertedSource = source.invert()
                    bitmap = BinaryBitmap(HybridBinarizer(invertedSource))
                    try {
                        rawResult = reader.decode(bitmap, hints)
                    } catch (e: NotFoundException) {
                        // continue
                    } catch (e: FormatException) {
                        e.printStackTrace()
                    } catch (e: ChecksumException) {
                        e.printStackTrace()
                    } finally {
                        reader.reset()
                    }
                }
            }

            val finalRawResult = rawResult

            if (finalRawResult != null) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    // Stopping the preview can take a little long.
                    // So we want to set result handler to null to discard subsequent calls to
                    // onPreviewFrame.
                    val tmpResultHandler = mResultHandler
                    mResultHandler = null

                    stopCameraPreview()
                    tmpResultHandler?.handleResult(null, finalRawResult)
                }
            } else {
                camera.setOneShotPreviewCallback(this)
            }
        } catch (e: RuntimeException) {
            Timber.e(e)
        }
    }

    fun resumeCameraPreview(resultHandler: ResultHandler) {
        mResultHandler = resultHandler
        super.resumeCameraPreview()
    }

    private fun buildLuminanceSource(data: ByteArray, rect: Rect): PlanarYUVLuminanceSource? {
        var source: PlanarYUVLuminanceSource? = null
        try {
            source = PlanarYUVLuminanceSource(
                data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false
            )
        } catch (e: Exception) {
        }

        return source
    }

    companion object {
        private val TAG = "QRScannerView"
    }

    interface ResultHandler {
        fun handleResult(image: ByteArray?, result: Result)
    }
}
