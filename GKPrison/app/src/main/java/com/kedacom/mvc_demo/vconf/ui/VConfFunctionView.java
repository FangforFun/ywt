/**
 * @(#)VConfFunctionView.java 2013-8-2 Copyright 2013 it.kedacom.com, Inc. All
 *                            rights reserved.
 */

package com.kedacom.mvc_demo.vconf.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.pc.utils.NetWorkUtils;

/**
 * @author chenjian
 * @date 2013-8-2
 */

public class VConfFunctionView extends LinearLayout implements OnClickListener {

	private final int WHAT_REQCHAIRMAN = 0x369;
	private final long DELAY_MILLIS = 10 * 1000;

	private ImageView mDumbImg;
	private Button mExitImg;
	private TextView mChairText;
	private ImageView mQuietImg;
	private Button mMoreInfoImg;
	private View mVconfFunctionFrame;

	// 弹出框
	private PopupWindow mExitVConfWin;
	private PopupWindow mVConfInfoMoreWin;

	private Activity mCurrActivity;

	public VConfFunctionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VConfFunctionView(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mCurrActivity = AppStackManager.Instance().currentActivity();

		mDumbImg = (ImageView) findViewById(R.id.mute_Img);
		mQuietImg = (ImageView) findViewById(R.id.quiet_Img);
		mMoreInfoImg = (Button) findViewById(R.id.moreInfo_Img);
		mExitImg = (Button) findViewById(R.id.exit_Img);
		mVconfFunctionFrame = findViewById(R.id.vconfFunction_Frame);
		mChairText = (TextView) findViewById(R.id.vconf_chairtext);

		initContentComponentValue();

		mExitImg.setOnClickListener(this);
		mDumbImg.setOnClickListener(this);
		mQuietImg.setOnClickListener(this);
		mMoreInfoImg.setOnClickListener(this);

		if (VideoConferenceService.mLinkState != null && VideoConferenceService.mLinkState.isCSP2P()) {
			mMoreInfoImg.setVisibility(View.GONE);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mExitVConfWin != null) {
			mExitVConfWin.dismiss();
		}
		mExitVConfWin = null;

		if (mVConfInfoMoreWin != null) {
			mVConfInfoMoreWin.dismiss();
		}
		mVConfInfoMoreWin = null;

	}

	public void onDestroyView() {
		dissPopWin();

		mExitVConfWin = null;
		mVConfInfoMoreWin = null;
	}

	/**
	 * 为各主件设置初始值
	 */
	private void initContentComponentValue() {
		setQuietImageView(false);
		setMuteImageView(false);

		KdvMtBaseAPI.makeSelMtMute(false);
		KdvMtBaseAPI.makeSelMtQuiet(false);
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}
		String e164 = null;
		// if (mCurrActivity instanceof VConfAudioActivity) {
		// e164 = ((VConfAudioActivity) mCurrActivity).getE164();
		// } else

		if (mCurrActivity instanceof VConfVideoActivity) {
			e164 = ((VConfVideoActivity) mCurrActivity).getE164();
		}

		switch (v.getId()) {
		// 哑音
			case R.id.mute_Img:
				toggleMute();
				break;

			// 静音
			case R.id.quiet_Img:
				toggleQuiet();
				break;

			// 更多信息
			case R.id.moreInfo_Img:
				// P2P呼叫，只有统计信息
				if (VideoConferenceService.mLinkState != null && VideoConferenceService.mLinkState.isCSP2P()) {
				} else {
					toggleVConfInfoMoreWindow();
				}
				break;

			// 申请、释放管理员权限
			case R.id.vconf_chairtext:
				mHandler.removeMessages(WHAT_REQCHAIRMAN);

				// 释放主席
				if (VideoConferenceService.isChairMan()) {
					KdvMtBaseAPI.revokeChairman();
				}
				// 申请主席，10s之内申请失败弹出提示
				else {
					KdvMtBaseAPI.reqChairman();
					mHandler.sendEmptyMessageDelayed(WHAT_REQCHAIRMAN, DELAY_MILLIS);
				}

				disssVConfInfoMoreWin();
				break;

			// 挂断
			case R.id.exit_Img:
				VideoConferenceService.mIsQuitAction = true;

				if (checkExceptionQuit()) {
					VideoConferenceService.quitConfAction(true);
					break;
				}

				// 结束点对点会议
				if (VideoConferenceService.mLinkState.isCSP2P()) {
					KdvMtBaseAPI.endP2PConf();
					VideoConferenceService.forceCloseVConfActivity();
					break;
				}

				// 主席权限，有主席权限时，可选择 "退出会议"或"结束会议"
				if (VideoConferenceService.isChairMan()) {
					toggleExitVConfWindow();
				}
				// 没有主席权限，直接主动退出会议
				else {
					KdvMtBaseAPI.quitConf();
					VideoConferenceService.forceCloseVConfActivity();
				}
				break;

			// 退出会议
			case R.id.ExitConf_Text:
				VideoConferenceService.mIsQuitAction = true;

				if (checkExceptionQuit()) {
					VideoConferenceService.quitConfAction(true);
					break;
				}

				KdvMtBaseAPI.quitConf();
				disssExitVConfWin();
				VideoConferenceService.forceCloseVConfActivity();
				break;

			// 结束会议, 需主席权限
			case R.id.EndConf_Text:
				VideoConferenceService.mIsQuitAction = true;

				if (checkExceptionQuit()) {
					VideoConferenceService.quitConfAction(true);
					break;
				}

				KdvMtBaseAPI.endConf();
				disssExitVConfWin();
				VideoConferenceService.forceCloseVConfActivity();
				break;

			default:
				break;
		}

	}

	/**
	 * 检测是否是异常退会
	 * @return
	 */
	private boolean checkExceptionQuit() {
		if (!NetWorkUtils.isAvailable(getContext()) || VideoConferenceService.mLinkState == null || !LoginFlowService.mRegisterGK || !VideoConferenceService.isCSVConf()) {
			Activity currActivity = AppStackManager.Instance().currentActivity();

			// if (currActivity != null && ((currActivity instanceof VConfAudioActivity) || (currActivity instanceof
			// VConfVideoActivity))) {
			// AppStackManager.Instance().popActivity(currActivity);
			// }

			if (currActivity != null && ((currActivity instanceof VConfVideoActivity))) {
				AppStackManager.Instance().popActivity(currActivity);
			}

			VideoConferenceService.cleanConf();

			return true;
		}

		return false;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (null == msg) {
				return;
			}

			switch (msg.what) {
				case WHAT_REQCHAIRMAN:
					//
					if (!VideoConferenceService.isChairMan()) {
						Toast.makeText(getContext(), "申请管理方失败", 0).show();
					}
					break;

				default:
					break;
			}
		};
	};

	/**
	 * 删除申请管理权限的提示
	 */
	public void removeReqChairmanHandler() {
		if (null != mHandler) {
			mHandler.removeMessages(WHAT_REQCHAIRMAN);
		}
	}

	/**
	 * 哑音开关
	 */
	private void toggleMute() {
		if (null == mDumbImg || null == mDumbImg.getTag()) {
			return;
		}

		boolean mute = (Boolean) mDumbImg.getTag();
		KdvMtBaseAPI.makeSelMtMute(!mute);

		setMuteImageView(!mute);
	}

	/**
	 * 静音
	 */
	public void toggleQuiet() {
		if (null == mQuietImg || null == mQuietImg.getTag()) {
			return;
		}

		boolean quiet = (Boolean) mQuietImg.getTag();
		KdvMtBaseAPI.makeSelMtQuiet(!quiet);

		setQuietImageView(!quiet);
	}

	/**
	 * 当前是否为静音状态
	 * @return
	 */
	public boolean isQuiet() {
		if (null == mQuietImg || null == mQuietImg.getTag()) {
			return false;
		}

		return (Boolean) mQuietImg.getTag();
	}

	/**
	 * 设置静音图标
	 * @param quiet
	 */
	public void setQuietImageView(final boolean quiet) {
		if (null == mQuietImg) {
			return;
		}

		post(new Runnable() {

			@Override
			public void run() {
				if (quiet) {
					mQuietImg.setImageResource(R.drawable.vconf_mute);
				} else {
					mQuietImg.setImageResource(R.drawable.vconf_speaker);
				}
				mQuietImg.setTag(quiet);
			}
		});
	}

	/**
	 * 设置哑音图标
	 * @param mute
	 */
	public void setMuteImageView(final boolean mute) {
		if (null == mDumbImg) {
			return;
		}

		post(new Runnable() {

			@Override
			public void run() {
				if (mute) {
					mDumbImg.setImageResource(R.drawable.vconf_microphone_off);
				} else {
					mDumbImg.setImageResource(R.drawable.vconf_microphone_on);
				}
				mDumbImg.setTag(mute);
			}
		});
	}

	/**
	 * 更多会议信息弹出框
	 */
	private void toggleVConfInfoMoreWindow() {
		if (mMoreInfoImg == null) {
			return;
		}

		if (mVConfInfoMoreWin == null) {
			mVConfInfoMoreWin = popVConfInfoMoreWin();
		}

		if (mVConfInfoMoreWin.isShowing()) {
			mVConfInfoMoreWin.dismiss();
			return;
		}

		View view = mVConfInfoMoreWin.getContentView();
		// 判断自己是否有主席权限，如果有，隐藏“申请管理权限”
		if (view != null && VideoConferenceService.isChairMan()) {
			((TextView) view.findViewById(R.id.vconf_chairtext)).setText(R.string.vconf_releaseAdministrativePrivileges);
		} else {
			((TextView) view.findViewById(R.id.vconf_chairtext)).setText(R.string.vconf_Apply4AdministrativePrivileges);
		}

		// 弹出框箭头离右边框的间距
		int wLP = getResources().getDimensionPixelSize(R.dimen.Vconf_more_info_width) / 5;
		// 更多信息 中间里屏幕右边框的间距
		int x = mVconfFunctionFrame.getWidth() * 3 / 8;
		int y = mMoreInfoImg.getHeight() + getResources().getDimensionPixelSize(R.dimen.vconfFunction_padding_top_bottom);
		mVConfInfoMoreWin.showAtLocation(mVconfFunctionFrame, Gravity.BOTTOM | Gravity.RIGHT, x - wLP, y);
	}

	/**
	 * 退出/介绍会议
	 */
	private void toggleExitVConfWindow() {
		if (mExitImg == null) {
			return;
		}

		if (mExitVConfWin == null) {
			mExitVConfWin = popVConfExitWin();
		}

		if (mExitVConfWin.isShowing()) {
			mExitVConfWin.dismiss();
			return;
		}

		int x = mExitImg.getWidth() / 5;
		int y = mExitImg.getHeight() + getResources().getDimensionPixelSize(R.dimen.vconfFunction_padding_top_bottom);
		// int y = mVconfFunctionFrame.getHeight() * 2 / 3;
		mExitVConfWin.showAtLocation(mExitImg, Gravity.BOTTOM | Gravity.RIGHT, x, y);
		mExitVConfWin.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
			}
		});
	}

	/**
	 * 会议信息弹出框
	 * @return
	 */
	private PopupWindow popVConfInfoMoreWin() {
		if (mCurrActivity == null) {
			return null;
		}

		View view = LayoutInflater.from(mCurrActivity).inflate(R.layout.vconf_info_more_layout, null);
		if (view == null) {
			return null;
		}

		final int wLP = getResources().getDimensionPixelSize(R.dimen.Vconf_more_info_width);
		final int hLP = ViewGroup.LayoutParams.WRAP_CONTENT;
		final PopupWindow popWin = new PopupWindow(view, wLP, hLP, true);
		popWin.setBackgroundDrawable(new BitmapDrawable());
		popWin.setOutsideTouchable(true);
		popWin.setAnimationStyle(android.R.style.Animation_Dialog);

		view.findViewById(R.id.vconf_chairtext).setOnClickListener(this);

		return popWin;
	}

	/**
	 * 退出/介绍会议弹出框
	 * @return
	 */
	private PopupWindow popVConfExitWin() {
		if (mCurrActivity == null) {
			return null;
		}

		View view = LayoutInflater.from(mCurrActivity).inflate(R.layout.vconf_exit_selector_layout, null);
		if (view == null) {
			return null;
		}

		final int wLP = getResources().getDimensionPixelSize(R.dimen.VconfExitPopWidth);
		final int hLP = ViewGroup.LayoutParams.WRAP_CONTENT;
		final PopupWindow popWin = new PopupWindow(view, wLP, hLP, true);
		popWin.setBackgroundDrawable(new BitmapDrawable());
		popWin.setOutsideTouchable(true);
		popWin.setAnimationStyle(android.R.style.Animation_Dialog);

		view.findViewById(R.id.ExitConf_Text).setOnClickListener(this);
		view.findViewById(R.id.EndConf_Text).setOnClickListener(this);

		return popWin;
	}

	/**
	 * 是否有弹出框显示
	 * @return
	 */
	public boolean hasPopWindowShowing() {
		if (mExitVConfWin != null && mExitVConfWin.isShowing()) {
			return true;
		}

		if (mVConfInfoMoreWin != null && mVConfInfoMoreWin.isShowing()) {
			return true;
		}

		return false;
	}

	private void disssVConfInfoMoreWin() {
		if (mVConfInfoMoreWin == null) {
			return;
		}

		if (mVConfInfoMoreWin.isShowing()) {
			mVConfInfoMoreWin.dismiss();
		}
	}

	private void disssExitVConfWin() {
		if (mExitVConfWin == null) {
			return;
		}

		if (mExitVConfWin.isShowing()) {
			mExitVConfWin.dismiss();
		}
	}

	private void disssCodecStatusWin() {
	}

	/**
	 * 关闭所有弹出框
	 */
	public void dissPopWin() {
		post(new Runnable() {

			@Override
			public void run() {
				disssExitVConfWin();
				disssCodecStatusWin();
				disssVConfInfoMoreWin();
			}
		});
	}

}
