/**
 * @(#)OtherApplyChair.java   2014-6-6
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 */

package com.kedacom.mvc_demo.vconf.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.vconf.bean.MtInfo;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.modle.ApplyAdapter;
import com.kedacom.mvc_demo.vconf.modle.OtherApplyDialogManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenj
 * @date 2014-6-6
 */

public class OtherApplyDialog {

	private final int what = 112;

	private ApplyAdapter mAdapter;
	private Dialog mApplyDialog;
	private Handler mHandler;
	// list
	private List<MtInfo> mApplyPersonInfos;

	private boolean isApplyChairMan;

	public OtherApplyDialog() {
		mApplyPersonInfos = new ArrayList<MtInfo>();
	}

	public OtherApplyDialog(boolean isApplyChairMan) {
		this.isApplyChairMan = isApplyChairMan;
		mApplyPersonInfos = new ArrayList<MtInfo>();
	}

	/**
	 * add MtInfo
	 * @param mtInfo
	 */
	public void addMtInfo(MtInfo mtInfo) {
		if (null == mtInfo) return;

		// 已经存在
		if (mApplyPersonInfos.contains(mtInfo)) {
			return;
		}

		mApplyPersonInfos.add(mtInfo);
	}

	public void set() {

	}

	/**
	 * 弹出框为空
	 * @return
	 */
	public boolean isNullApplyChairDialog() {
		return null == mApplyDialog;
	}

	private Activity mCurrActivity;

	/**
	 * show Dialog
	 * @param activity
	 * @param mtInfo
	 */
	public void showApplyDialog(final Activity activity, final MtInfo mtInfo) {
		if (null == activity || null == mtInfo) return;

		addMtInfo(mtInfo);

		if (isApplyChairMan) {
			VConfVideoActivity vActivity = (VConfVideoActivity) AppStackManager.Instance().getActivity(VConfVideoActivity.class);
			if (null != vActivity) {
				VConfFunctionView functionView = vActivity.getVConfFunctionView();
				if (null != functionView) {
					functionView.removeReqChairmanHandler();
				}
			}
			// 音频
//			VConfAudioActivity aActivity = (VConfAudioActivity)  AppStackManager.Instance().getActivity(VConfAudioActivity.class);
//			if (null != aActivity) {
//				VConfFunctionView functionView = aActivity.getVConfFunctionView();
//				if (null != functionView) {
//					functionView.removeReqChairmanHandler();
//				}
//			}
		}

		if (null != mApplyDialog) {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (null != mAdapter) {
						mHandler.removeMessages(what);

						mHandler.sendEmptyMessageDelayed(what, 10 * 1000);
						mAdapter.setMtInfo(mApplyPersonInfos);
						mAdapter.notifyDataSetChanged();
					}

					if (!mApplyDialog.isShowing() && mCurrActivity != null && !mCurrActivity.isFinishing() && !mCurrActivity.isDestroyed()) {
						mApplyDialog.show();
					}
				}
			});

			return;
		}

		mCurrActivity = activity;
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				final Activity currActivity = AppStackManager.Instance().currentActivity();
				// 如果正在视音频播放界面，关闭所有弹出框
				if (null != currActivity) {
					VConfFunctionView functionView = null;
					if (currActivity instanceof VConfVideoActivity) {
						functionView = ((VConfVideoActivity) currActivity).getVConfFunctionView();
					}
					// 音频
//					else if (currActivity instanceof VConfAudioActivity) {
//						functionView = ((VConfAudioActivity) currActivity).getVConfFunctionView();
//					}

					if (functionView != null) {
						functionView.dissPopWin();
					}
				}

				View view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vconf_applay_chair_speaker_dialog, null);
				TextView title = (TextView) view.findViewById(R.id.title);
				if (!isApplyChairMan) {
					title.setText(R.string.vconf_applyspeaker);
				} else {
					title.setText(R.string.vconf_applychair);
				}

				final ListView listView = (ListView) view.findViewById(R.id.listView);
				listView.setSelector(android.R.color.transparent);
				mAdapter = new ApplyAdapter(activity, mApplyPersonInfos);
				listView.setAdapter(mAdapter);
				listView.setOnItemClickListener(mAdapter);

				mHandler = new Handler() {

					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						try {
							if (isApplyChairMan) {
								OtherApplyDialogManager.releaseOtherApplyChairDialog();
							} else {
								OtherApplyDialogManager.releaseOtherApplySpeakDialog();
							}
						} catch (Exception e) {
						}
					}
				};

				// 10s自动消失
				mHandler.sendEmptyMessageDelayed(what, 10 * 1000);

				mApplyDialog = new Dialog(activity, R.style.Loading_Dialog_Theme);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lp.gravity = Gravity.CENTER;
				mApplyDialog.addContentView(view, lp);
				mApplyDialog.setCanceledOnTouchOutside(false);
				mApplyDialog.setCancelable(false);
				mApplyDialog.show();

				view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mApplyDialog != null) {
							mApplyDialog.cancel();
							mApplyDialog = null;
						}

						if (null != mAdapter) {
							MtInfo info = mAdapter.getSelectItem();
							if (info != null) {
								if (isApplyChairMan) {
									KdvMtBaseAPI.specifyChairman(info.mMtId.getMcuNo(), info.mMtId.getTerNo());
								} else {
									KdvMtBaseAPI.specifySpeaker(info.mMtId.getMcuNo(), info.mMtId.getTerNo());
								}
							}
						}

						try {
							if (isApplyChairMan) {
								OtherApplyDialogManager.releaseOtherApplyChairDialog();
							} else {
								OtherApplyDialogManager.releaseOtherApplySpeakDialog();
							}
						} catch (Exception e) {
						}
					}
				});
				view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mApplyDialog != null) {
							mApplyDialog.cancel();
							mApplyDialog = null;
						}

						try {
							OtherApplyDialogManager.releaseOtherApplyChairDialog();
							OtherApplyDialogManager.releaseOtherApplySpeakDialog();
						} catch (Exception e) {
						}
					}
				});
			}
		});
	}

	/**
	 * release data
	 */
	public void release() {
		if (null != mHandler) {
			mHandler.removeMessages(what);
		}
		if (null != mCurrActivity) {
			mCurrActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mApplyDialog != null && mApplyDialog.isShowing()) {
						mApplyDialog.cancel();
					}
					mApplyDialog = null;
				}
			});
		}

		if (null != mApplyPersonInfos) {
			mApplyPersonInfos.clear();
		}
	}

	public void cancelDialog() {
		if (null == mCurrActivity) {
			return;
		}

		mCurrActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mApplyDialog != null && mApplyDialog.isShowing()) {
					mApplyDialog.cancel();
				}
			}
		});
	}
}
