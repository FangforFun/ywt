/**
 * @(#)VideoCapServiceConnect.java 2013-8-14 Copyright 2013 it.kedacom.com, Inc.
 *                                 All rights reserved.
 */

package com.kedacom.mvc_demo.vconf.modle.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * @author chenjian
 * @date 2013-8-14
 */

public class VideoCapServiceConnect implements ServiceConnection {

	private final String TAG = VideoCapServiceConnect.class.getSimpleName();

	private VideoCapService mVideoCapService;

	// Service is started
	private boolean mIsStarted;

	/**
	 * @see ServiceConnection#onServiceConnected(ComponentName,
	 *      IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mVideoCapService = ((VideoCapService.VideoCapServiceBinder) service).getService();

		mIsStarted = true;
	}

	/**
	 * @see ServiceConnection#onServiceDisconnected(ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		mVideoCapService = null;

		mIsStarted = false;
	}

	/** @return the mIsStarted */
	public boolean isStarted() {
		return mIsStarted;
	}

	/**
	 * 初始化采集图像
	 */
	public void initVideoCapture(Context context) {
		if (null == mVideoCapService) {
			return;
		}

		try {
			mVideoCapService.initVideoCapture(context);
		} catch (Exception e) {
			Log.i(TAG, "initVideoCapture", e);
		}
	}

	/**
	 * 重新开始采集图像
	 */
	public void reStartVideoCapture(SurfaceHolder surfaceHolder, short resolution, boolean portrait) {
		if (null == mVideoCapService) {
			return;
		}

		try {
			mVideoCapService.reStartVideoCapture(surfaceHolder, resolution, portrait);
		} catch (Exception e) {
			Log.i(TAG, "reStartVideoCapture", e);
		}
	}

	/**
	 * 开始采集图像
	 */
	public void startVideoCapture(SurfaceHolder surfaceHolder, short resolution, boolean portrait) {
		if (null == mVideoCapService) {
			return;
		}

		try {
			mVideoCapService.startVideoCapture(surfaceHolder, resolution, portrait);
		} catch (Exception e) {
			Log.i(TAG, "startVideoCapture", e);
		}
	}

	public SurfaceHolder getSurfaceHolder() {
		if (null == mVideoCapService) {
			return null;
		}

		try {
			return mVideoCapService.getSurfaceHolder();
		} catch (Exception e) {
			Log.i(TAG, "get SurfaceHolder", e);
		}

		return null;
	}

	/**
	 * 停止采集图像
	 */
	public void stopVideoCapture() {
		if (null == mVideoCapService) {
			return;
		}

		try {
			mVideoCapService.stopVideoCapture();
		} catch (Exception e) {
			Log.i(TAG, "stopVideoCapture", e);
		}
	}

	public void destroyVideoCapture() {
		if (null == mVideoCapService) {
			return;
		}

		try {
			mVideoCapService.destroyVideoCapture();
		} catch (Exception e) {
			Log.i(TAG, "destroyVideoCapture", e);
		}
	}

}
