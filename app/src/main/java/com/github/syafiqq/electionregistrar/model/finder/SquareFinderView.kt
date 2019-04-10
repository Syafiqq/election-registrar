package com.github.syafiqq.electionregistrar.model.finder

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import me.dm7.barcodescanner.core.DisplayUtils
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.R

class SquareFinderView : View, IViewFinder {

    private var mFramingRect: Rect? = null
    private var scannerAlpha: Int = 0

    private val mDefaultLaserColor = ContextCompat.getColor(context, R.color.viewfinder_laser)
    private val mDefaultMaskColor = ContextCompat.getColor(context, R.color.viewfinder_mask)
    private val mDefaultBorderColor = ContextCompat.getColor(context, R.color.viewfinder_border)
    private val mDefaultBorderStrokeWidth = resources.getInteger(R.integer.viewfinder_border_width)
    private val mDefaultBorderLineLength = resources.getInteger(R.integer.viewfinder_border_length)

    private lateinit var mLaserPaint: Paint
    private lateinit var mFinderMaskPaint: Paint
    private lateinit var mBorderPaint: Paint
    private var mBorderLineLength: Int = 0
    private var mSquareViewFinder: Boolean = false
    private var mBordersAlpha: Float = 0.toFloat()

    @ColorInt
    private var mLaserColor = ContextCompat.getColor(context, R.color.viewfinder_laser)
    @ColorInt
    private var mBorderColor = ContextCompat.getColor(context, R.color.viewfinder_border)
    private var mIsLaserEnabled = true
    private var mBorderWidth = resources.getInteger(R.integer.viewfinder_border_width)
    private var mBorderLength = resources.getInteger(R.integer.viewfinder_border_length)
    private var mMaskColor = ContextCompat.getColor(context, R.color.viewfinder_mask)
    private var mRoundedCorner = false
    private var mCornerRadius = 0
    private var mSquaredFinder = false
    private var mViewFinderOffset = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    private fun init() {
        //set up laser paint
        mLaserPaint = Paint()
        mLaserPaint.color = mDefaultLaserColor
        mLaserPaint.style = Paint.Style.FILL

        //finder mask paint
        mFinderMaskPaint = Paint()
        mFinderMaskPaint.color = mDefaultMaskColor

        //border paint
        mBorderPaint = Paint()
        mBorderPaint.color = mDefaultBorderColor
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = mDefaultBorderStrokeWidth.toFloat()
        mBorderPaint.isAntiAlias = true

        mBorderLineLength = mDefaultBorderLineLength

        this.setBorderColor(mBorderColor)
        this.setLaserColor(mLaserColor)
        this.setLaserEnabled(mIsLaserEnabled)
        this.setBorderStrokeWidth(mBorderWidth)
        this.setBorderLineLength(mBorderLength)
        this.setMaskColor(mMaskColor)

        this.setBorderCornerRounded(mRoundedCorner)
        this.setBorderCornerRadius(mCornerRadius)
        this.setSquareViewFinder(mSquaredFinder)
        this.setViewFinderOffset(mViewFinderOffset)
    }

    override fun setLaserColor(laserColor: Int) {
        mLaserPaint.color = laserColor
        this.setupViewFinder()
    }

    override fun setMaskColor(maskColor: Int) {
        mFinderMaskPaint.color = maskColor
        this.setupViewFinder()
    }

    override fun setBorderColor(borderColor: Int) {
        mBorderPaint.color = borderColor
        this.setupViewFinder()
    }

    override fun setBorderStrokeWidth(borderStrokeWidth: Int) {
        mBorderPaint.strokeWidth = borderStrokeWidth.toFloat()
        this.setupViewFinder()
    }

    override fun setBorderLineLength(borderLineLength: Int) {
        mBorderLineLength = borderLineLength
        this.setupViewFinder()
    }

    override fun setLaserEnabled(isLaserEnabled: Boolean) {
        mIsLaserEnabled = isLaserEnabled
        this.setupViewFinder()
    }

    override fun setBorderCornerRounded(isBorderCornersRounded: Boolean) {
        if (isBorderCornersRounded) {
            mBorderPaint.strokeJoin = Paint.Join.ROUND
        } else {
            mBorderPaint.strokeJoin = Paint.Join.BEVEL
        }
        this.setupViewFinder()
    }

    override fun setBorderAlpha(alpha: Float) {
        val colorAlpha = (255 * alpha).toInt()
        mBordersAlpha = alpha
        mBorderPaint.alpha = colorAlpha
        this.setupViewFinder()
    }

    override fun setBorderCornerRadius(borderCornersRadius: Int) {
        mBorderPaint.pathEffect = CornerPathEffect(borderCornersRadius.toFloat())
        this.setupViewFinder()
    }

    override fun setViewFinderOffset(offset: Int) {
        mViewFinderOffset = offset
        this.setupViewFinder()
    }

    // TODO: Need a better way to configure this. Revisit when working on 2.0
    override fun setSquareViewFinder(set: Boolean) {
        mSquareViewFinder = set
    }

    override fun setupViewFinder() {
        updateFramingRect()
        invalidate()
    }

    override fun getFramingRect(): Rect? {
        return mFramingRect
    }

    public override fun onDraw(canvas: Canvas) {
        if (framingRect == null) {
            return
        }

        drawViewFinderMask(canvas)
        drawViewFinderBorder(canvas)

        if (mIsLaserEnabled) {
            drawLaser(canvas)
        }
    }

    fun drawViewFinderMask(canvas: Canvas) {
        val width = canvas.width
        val height = canvas.height
        val framingRect = framingRect

        canvas.drawRect(0f, 0f, width.toFloat(), framingRect!!.top.toFloat(), mFinderMaskPaint)
        canvas.drawRect(
            0f,
            framingRect.top.toFloat(),
            framingRect.left.toFloat(),
            (framingRect.bottom + 1).toFloat(),
            mFinderMaskPaint
        )
        canvas.drawRect(
            (framingRect.right + 1).toFloat(),
            framingRect.top.toFloat(),
            width.toFloat(),
            (framingRect.bottom + 1).toFloat(),
            mFinderMaskPaint
        )
        canvas.drawRect(0f, (framingRect.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), mFinderMaskPaint)
    }

    fun drawViewFinderBorder(canvas: Canvas) {
        val framingRect = framingRect

        // Top-left corner
        val path = Path()
        path.moveTo(framingRect!!.left.toFloat(), (framingRect.top + mBorderLineLength).toFloat())
        path.lineTo(framingRect.left.toFloat(), framingRect.top.toFloat())
        path.lineTo((framingRect.left + mBorderLineLength).toFloat(), framingRect.top.toFloat())
        canvas.drawPath(path, mBorderPaint)

        // Top-right corner
        path.moveTo(framingRect.right.toFloat(), (framingRect.top + mBorderLineLength).toFloat())
        path.lineTo(framingRect.right.toFloat(), framingRect.top.toFloat())
        path.lineTo((framingRect.right - mBorderLineLength).toFloat(), framingRect.top.toFloat())
        canvas.drawPath(path, mBorderPaint)

        // Bottom-right corner
        path.moveTo(framingRect.right.toFloat(), (framingRect.bottom - mBorderLineLength).toFloat())
        path.lineTo(framingRect.right.toFloat(), framingRect.bottom.toFloat())
        path.lineTo((framingRect.right - mBorderLineLength).toFloat(), framingRect.bottom.toFloat())
        canvas.drawPath(path, mBorderPaint)

        // Bottom-left corner
        path.moveTo(framingRect.left.toFloat(), (framingRect.bottom - mBorderLineLength).toFloat())
        path.lineTo(framingRect.left.toFloat(), framingRect.bottom.toFloat())
        path.lineTo((framingRect.left + mBorderLineLength).toFloat(), framingRect.bottom.toFloat())
        canvas.drawPath(path, mBorderPaint)
    }

    fun drawLaser(canvas: Canvas) {
        val framingRect = framingRect

        // Draw a red "laser scanner" line through the middle to show decoding is active
        mLaserPaint.alpha = SCANNER_ALPHA[scannerAlpha]
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size
        val middle = framingRect!!.height() / 2 + framingRect.top
        canvas.drawRect(
            (framingRect.left + 2).toFloat(),
            (middle - 1).toFloat(),
            (framingRect.right - 1).toFloat(),
            (middle + 2).toFloat(),
            mLaserPaint
        )

        postInvalidateDelayed(
            ANIMATION_DELAY,
            framingRect.left - POINT_SIZE,
            framingRect.top - POINT_SIZE,
            framingRect.right + POINT_SIZE,
            framingRect.bottom + POINT_SIZE
        )
    }

    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        updateFramingRect()
    }

    @Synchronized
    fun updateFramingRect() {
        val viewResolution = Point(width, height)
        var width: Int
        var height: Int
        val orientation = DisplayUtils.getScreenOrientation(context)

        if (mSquareViewFinder) {
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (getHeight() * DEFAULT_SQUARE_DIMENSION_RATIO).toInt()
                width = height
            } else {
                width = (getWidth() * DEFAULT_SQUARE_DIMENSION_RATIO).toInt()
                height = width
            }
        } else {
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (getHeight() * LANDSCAPE_HEIGHT_RATIO).toInt()
                width = (LANDSCAPE_WIDTH_HEIGHT_RATIO * height).toInt()
            } else {
                width = (getWidth() * PORTRAIT_WIDTH_RATIO).toInt()
                height = (PORTRAIT_WIDTH_HEIGHT_RATIO * width).toInt()
            }
        }

        if (width > getWidth()) {
            width = getWidth() - MIN_DIMENSION_DIFF
        }

        if (height > getHeight()) {
            height = getHeight() - MIN_DIMENSION_DIFF
        }

        val leftOffset = (viewResolution.x - width) / 2
        val topOffset = (viewResolution.y - height) / 2
        mFramingRect = Rect(
            leftOffset + mViewFinderOffset,
            topOffset + mViewFinderOffset,
            leftOffset + width - mViewFinderOffset,
            topOffset + height - mViewFinderOffset
        )
    }

    companion object {
        private val TAG = "SquareFinderView"

        private val PORTRAIT_WIDTH_RATIO = 6f / 8
        private val PORTRAIT_WIDTH_HEIGHT_RATIO = 0.75f

        private val LANDSCAPE_HEIGHT_RATIO = 3f / 8
        private val LANDSCAPE_WIDTH_HEIGHT_RATIO = 5f
        private val MIN_DIMENSION_DIFF = 0

        private val DEFAULT_SQUARE_DIMENSION_RATIO = 5f / 8

        private val SCANNER_ALPHA = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
        private val POINT_SIZE = 10
        private val ANIMATION_DELAY = 80L
    }
}
