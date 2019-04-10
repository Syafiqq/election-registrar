package me.dm7.barcodescanner.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback {

    protected CameraWrapper mCameraWrapper;
    private CameraPreview mPreview;
    private IViewFinder mViewFinderView;
    private Rect mFramingRectInPreview;
    private Rect mScaledFramingRectInPreview;
    private CameraHandlerThread mCameraHandlerThread;
    private Boolean mFlashState;
    private boolean mAutofocusState = true;
    private boolean mShouldScaleToFill = true;

    private float mBorderAlpha = 1.0f;
    private float mAspectTolerance = 0.1f;

    public BarcodeScannerView(Context context) {
        super(context);
        init();
    }

    public BarcodeScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        mViewFinderView = createViewFinderView(getContext());
    }

    public void setupLayout(CameraWrapper cameraWrapper) {
        removeAllViews();

        mPreview = getCameraPreview(cameraWrapper);
        mPreview.setAspectTolerance(mAspectTolerance);
        mPreview.setShouldScaleToFill(mShouldScaleToFill);
        if (!mShouldScaleToFill) {
            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            relativeLayout.setGravity(Gravity.CENTER);
            relativeLayout.setBackgroundColor(Color.BLACK);
            relativeLayout.addView(mPreview);
            addView(relativeLayout);
        } else {
            addView(mPreview);
        }

        if (mViewFinderView instanceof View) {
            addView((View) mViewFinderView);
        } else {
            throw new IllegalArgumentException("IViewFinder object returned by " +
                    "'createViewFinderView()' should be instance of android.view.View");
        }
    }

    private CameraPreview getCameraPreview(CameraWrapper cameraWrapper) {
        return new CameraPreview(getContext(), cameraWrapper, this);
    }

    /**
     * <p>Method that creates view that represents visual appearance of a barcode scanner</p>
     * <p>Override it to provide your own view for visual appearance of a barcode scanner</p>
     *
     * @param context {@link Context}
     * @return {@link android.view.View} that implements {@link IViewFinder}
     */
    protected abstract IViewFinder createViewFinderView(Context context);

    public void startCamera(int cameraId) {
        if (mCameraHandlerThread == null) {
            mCameraHandlerThread = new CameraHandlerThread(this);
        }
        mCameraHandlerThread.startCamera(cameraId);
    }

    public void setupCameraPreview(CameraWrapper cameraWrapper) {
        mCameraWrapper = cameraWrapper;
        if (mCameraWrapper != null) {
            setupLayout(mCameraWrapper);
            mViewFinderView.setupViewFinder();
            if (mFlashState != null) {
                setFlash(mFlashState);
            }
            setAutoFocus(mAutofocusState);
        }
    }

    public void startCamera() {
        startCamera(CameraUtils.getDefaultCameraId());
    }

    public void stopCamera() {
        if (mCameraWrapper != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);
            mCameraWrapper.mCamera.release();
            mCameraWrapper = null;
        }
        if (mCameraHandlerThread != null) {
            mCameraHandlerThread.quit();
            mCameraHandlerThread = null;
        }
    }

    public void stopCameraPreview() {
        if (mPreview != null) {
            mPreview.stopCameraPreview();
        }
    }

    protected void resumeCameraPreview() {
        if (mPreview != null) {
            mPreview.showCameraPreview();
        }
    }

    public synchronized Rect getFramingRectInPreview(int previewWidth, int previewHeight) {
        if (mFramingRectInPreview == null) {
            int viewFinderViewWidth = mViewFinderView.getWidth();
            int viewFinderViewHeight = mViewFinderView.getHeight();
            if (viewFinderViewWidth == 0 || viewFinderViewHeight == 0) {
                return null;
            }

            double ratio = viewFinderViewWidth * 1.0 / viewFinderViewHeight;
            if (previewWidth * 1.0 / previewHeight >= ratio) {
                previewWidth = (int) (ratio * previewHeight);
            } else {
                previewHeight = (int) (previewWidth / ratio);
            }
            Rect rect2 = new Rect(0, 0, previewWidth, previewHeight);

            mFramingRectInPreview = rect2;
        }
        return mFramingRectInPreview;
    }

    public synchronized Rect getScaledFramingRect(Rect preview) {
        if (mScaledFramingRectInPreview == null) {
            Rect framingRect = mViewFinderView.getFramingRect();
            int viewFinderViewWidth = mViewFinderView.getWidth();
            int viewFinderViewHeight = mViewFinderView.getHeight();
            Rect rect = new Rect(framingRect);

            if (preview.width() < viewFinderViewWidth) {
                rect.left = rect.left * preview.width() / viewFinderViewWidth;
                rect.right = rect.right * preview.width() / viewFinderViewWidth;
            }

            if (preview.height() < viewFinderViewHeight) {
                rect.top = rect.top * preview.height() / viewFinderViewHeight;
                rect.bottom = rect.bottom * preview.height() / viewFinderViewHeight;
            }

            mScaledFramingRectInPreview = rect;
        }
        return mScaledFramingRectInPreview;
    }

    public boolean getFlash() {
        if (mCameraWrapper != null && CameraUtils.isFlashSupported(mCameraWrapper.mCamera)) {
            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            return parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
        }
        return false;
    }

    public void setFlash(boolean flag) {
        mFlashState = flag;
        if (mCameraWrapper != null && CameraUtils.isFlashSupported(mCameraWrapper.mCamera)) {

            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            if (flag) {
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCameraWrapper.mCamera.setParameters(parameters);
        }
    }

    public void toggleFlash() {
        if (mCameraWrapper != null && CameraUtils.isFlashSupported(mCameraWrapper.mCamera)) {
            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCameraWrapper.mCamera.setParameters(parameters);
        }
    }

    public void setMaxZoom() {
        if (mCameraWrapper != null) {
            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            if (parameters.getZoom() != (parameters.getMaxZoom())) {
                parameters.setZoom(parameters.getMaxZoom());
            }
            mCameraWrapper.mCamera.setParameters(parameters);
        }
    }

    public void setOneShotPreviewCallback(Camera.PreviewCallback callback) {
        if (mCameraWrapper != null) {
            mCameraWrapper.mCamera.setOneShotPreviewCallback(callback);
        }
    }

    public void setAutoFocus(boolean state) {
        mAutofocusState = state;
        if (mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }

    public void setShouldScaleToFill(boolean shouldScaleToFill) {
        mShouldScaleToFill = shouldScaleToFill;
    }

    public void setAspectTolerance(float aspectTolerance) {
        mAspectTolerance = aspectTolerance;
    }

    public byte[] getRotatedData(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        int rotationCount = getRotationCount();

        if (rotationCount == 1 || rotationCount == 3) {
            for (int i = 0; i < rotationCount; i++) {
                byte[] rotatedData = new byte[data.length];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++)
                        rotatedData[x * height + height - y - 1] = data[x + y * width];
                }
                data = rotatedData;
                int tmp = width;
                width = height;
                height = tmp;
            }
        }

        return data;
    }

    public int getRotationCount() {
        int displayOrientation = mPreview.getDisplayOrientation();
        return displayOrientation / 90;
    }
}

