package com.pc.ui.layout;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class ZoomableCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private final String TAG = "MyFacingView";

	private int mCameraId;
	private Camera mCameraDevice;
	private SurfaceHolder mHolder;

	public ZoomableCameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ZoomableCameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ZoomableCameraPreview(Context context) {
		super(context);
		init();
	}

	private void init() {
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setKeepScreenOn(true);
	}

	private boolean mIsRemoveCallback;

	public void removeCallback() {

		if (null != mHolder && !mIsRemoveCallback) {
			mHolder.removeCallback(this);
			mIsRemoveCallback = true;
		}
		releaseCameraDevice();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		this.holder = holder;
		// Unlock the camera object before passing it to media recorder.
		try {
			releaseCameraDevice();

			initCamera();

			// 通过SurfaceView显示取景画面
			if (mCameraDevice != null) {
				mCameraDevice.setPreviewDisplay(holder);
			}
			// mCameraDevice.unlock();
		} catch (Exception e) {
			releaseCameraDevice();

			Log.e(TAG, "init CamerDevice " + e.toString());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i(TAG, "surfaceChanged " + width + "  " + height);
		this.holder = holder;
		if (mCameraDevice != null) {
			// 设置参数并开始预览
			mCameraDevice.stopPreview();
			Camera.Parameters mParameters = mCameraDevice.getParameters();
			boolean surpport = mParameters.isZoomSupported();
			Log.d("zoomSupported", "supported?:" + surpport);

			boolean smoothZoomSurpport = mParameters.isSmoothZoomSupported();
			Log.d("smoontZoomSurpport", "smoontZoomSurpport?:" + smoothZoomSurpport);

			int max = mParameters.getMaxZoom();
			Log.d("zoomMax", "max?:" + max);

			Size lSize = findBestResolution(width, height);
			invalidate();

			PixelFormat pixelFormat = new PixelFormat();
			PixelFormat.getPixelFormatInfo(mCameraDevice.getParameters().getPreviewFormat(), pixelFormat);
			mParameters.setPreviewSize(lSize.width, lSize.height);
			mParameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
			mCameraDevice.setParameters(mParameters);
			mCameraDevice.startPreview();
		} else {
			Log.e(TAG, "Camera is null!");
		}
	}

	private SurfaceHolder holder;

	/**
	 * @see SurfaceHolder.Callback#surfaceDestroyed(SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");

		releaseCameraDevice();
	}

	public void releaseCameraDevice() {
		if (mCameraDevice == null) {
			return;
		}

		// close display
		mCameraDevice.setPreviewCallback(null);
		mCameraDevice.stopPreview();
		mCameraDevice.release();
		mCameraDevice = null;
	}

	private Size findBestResolution(int pWidth, int pHeight) {
		if (mCameraDevice == null) {
			return null;
		}
		List<Size> lSizes = mCameraDevice.getParameters().getSupportedPreviewSizes();
		Size lSelectedSize = mCameraDevice.new Size(0, 0);
		for (Size lSize : lSizes) {
			if ((lSize.width <= pWidth) && (lSize.height <= pHeight) && (lSize.width >= lSelectedSize.width)
					&& (lSize.height >= lSelectedSize.height)) {
				lSelectedSize = lSize;
			}
		}
		if ((lSelectedSize.width == 0) || (lSelectedSize.height == 0)) {
			lSelectedSize = lSizes.get(0);
		}

		return lSelectedSize;
	}

	/**
	 * initialize Camera Device
	 */
	public void initCamera() throws Exception {
		int cameraId = 0;
		int numberOfCameras = Camera.getNumberOfCameras();
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (; cameraId < numberOfCameras; cameraId++) {
			Camera.getCameraInfo(cameraId, cameraInfo);
			// 前置摄像头
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				break;
			}
		}

		if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			mCameraDevice = Camera.open(cameraId);
		} else {
			mCameraDevice = Camera.open();
		}

		mCameraId = cameraId;

		adjustCameraDisplayOrientation();
	}

	/**
	 * 校准摄像头方向
	 */
	public void adjustCameraDisplayOrientation() {
		if (null == mCameraDevice) {
			return;
		}

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraId, info);

		int degrees = 0;
		int angle;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			angle = (info.orientation + degrees) % 360;
			angle = (360 - angle) % 360; // compensate the mirror
		}
		// back-facing
		else {
			angle = (info.orientation - degrees + 360) % 360;
		}

		mCameraDevice.setDisplayOrientation(angle);
	}

	public void zoom(int progress) {
		Camera.Parameters params = mCameraDevice.getParameters();
		boolean zoomSurpport = params.isZoomSupported();
		if (!zoomSurpport) return;

		int maxZoom = params.getMaxZoom();
		int zoom = (int) (progress / 100.0 * maxZoom);
		int currZoom = params.getZoom();
		if (currZoom == zoom) return;

		params.setZoom(zoom);

		mCameraDevice.setParameters(params);
	}

}
