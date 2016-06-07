/**
 * @(#)ConfMeetingService.java 2013-6-13 Copyright 2013 it.kedacom.com, Inc. All
 *                             rights reserved.
 */

package com.kedacom.mvc_demo.vconf.modle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.mvc_demo.vconf.bean.CallingAddr;
import com.kedacom.mvc_demo.vconf.bean.ConfInfo;
import com.kedacom.mvc_demo.vconf.bean.EmCallDisconnectReason;
import com.kedacom.mvc_demo.vconf.bean.LabelAssign;
import com.kedacom.mvc_demo.vconf.bean.LinkState;
import com.kedacom.mvc_demo.vconf.bean.MtId;
import com.kedacom.mvc_demo.vconf.bean.SaveVconf;
import com.kedacom.mvc_demo.vconf.bean.TMtInfoEx;
import com.kedacom.mvc_demo.vconf.bean.VConfType;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.modle.service.VideoCapServiceManager;
import com.kedacom.truetouch.mtc.EmMtAddrType;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.StringUtils;
import com.pc.utils.TerminalUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频会议
 * @author chenjian
 * @date 2013-6-13
 */

public class VideoConferenceService {

	/** 会议最多的人数 */
	public final static int MAX_MEMBER = 192;

	public static final String JOINCONF_ACTION = "com.kedacom.truetouch_JoinConf";
	public static final String LINKSTATE_ACTION = "com.kedacom.truetouch_LinkState";
	public static final String ONCALLINCOMING_ACTION = "com.kedacom.truetouch_onCallIncoming";
	public static final String CALLPEERE164NUM_ACTION = "com.kedacom.truetouch_CallPeerE164Num";
	public static final String REVEIVECONFINFO_ACTION = "com.kedacom.truetouch_ReceiveConfInfo";
	public static final String VCONF_RECORD_ACTION = "com.kedacom.truetouch_vconfrecord";
	public static final String RECVDUALSTATE_ACTION = "com.kedacom.truetouch_RecvDualState";

	// 音视频码率分割值，大于分割值为视频
	public static final int CALLRATE_SPLITLINE = 64;

	// 会议状态
	public static LinkState mLinkState;

	// 音视频对端E164号
	public static String mCallPeerE164Num;
	// 呼叫方地址信息
	public static CallingAddr mCallingAddr;
	// 多点会议时，本终端编号
	public static LabelAssign mLabelAssign;
	// 会议详情
	public static ConfInfo mConfInfo;

	// 双流
	public static boolean isDualStrea;

	public static boolean isJoinConf = false;// true 主叫，false被叫

	// 会议包含终端信息
	private static List<TMtInfoEx> mTMtInfoList;

	// 主动执行退出会议的动作
	public static boolean mIsQuitAction;

	public static SaveVconf mSaveVconf;// 缓存自己主动加入，呼叫，创建会议

	/**
	 * 会议结束时，清除会议相关数据
	 */
	public static void cleanConf() {
		mLinkState = null;
		isJoinConf = false;
		mCallPeerE164Num = null;
		mLabelAssign = null;
		mConfInfo = null;
		if (null != mTMtInfoList) {
			mTMtInfoList.clear();
		}
		mTMtInfoList = null;
		mCallingAddr = null;
		mSaveVconf = null;
		mIsQuitAction = false;

		OtherApplyDialogManager.releaseOtherApplyChairDialog();
		OtherApplyDialogManager.releaseOtherApplySpeakDialog();
	}

	/**
	 * 是否是有主席权限
	 * @return
	 */
	public static boolean isChairMan() {
		return mConfInfo != null && mConfInfo.mChairman != null && mLabelAssign != null && StringUtils.equals(mConfInfo.mChairman.mTerNo, mLabelAssign.mTerNo);
	}

	/**
	 * 主席
	 * @return
	 */
	public static MtId getChairMan() {
		if (mConfInfo == null) {
			return null;
		}

		return mConfInfo.mChairman;
	}

	/**
	 * 
	 是否是双流
	 * @return
	 */
	public static boolean isDualStrea() {

		return isDualStrea;
	}

	/**
	 * 主席信息
	 * @return
	 */
	public static TMtInfoEx getChairManTMtInfoEx() {
		// 主席
		MtId chairman = getChairMan();
		if (chairman == null) {
			return null;
		}

		if (StringUtils.isNull(chairman.mTerNo)) {
			return null;
		}

		if (mTMtInfoList == null || mTMtInfoList.isEmpty()) {
			return null;
		}

		for (int i = 0; i < mTMtInfoList.size(); i++) {
			TMtInfoEx tmtInfoEx = mTMtInfoList.get(i);
			if (null == tmtInfoEx || null == tmtInfoEx.mLabel) {
				continue;
			}
			if (StringUtils.equals(tmtInfoEx.mLabel.mTerNo, chairman.mTerNo)) {
				return tmtInfoEx;
			}
		}

		return null;
	}

	/**
	 * 是否是是发言人
	 * @return
	 */
	public static boolean isSpeaker() {
		return mConfInfo != null && mConfInfo.mChairman != null && mLabelAssign != null && StringUtils.equals(mConfInfo.mSpeaker.mTerNo, mLabelAssign.mTerNo);
	}

	/**
	 * 通过terNo获取alias
	 * @param terNo
	 * @return
	 */
	public static String getAliasByTerNo(String terNo) {
		if (null == mTMtInfoList) {
			return null;
		}

		for (TMtInfoEx tMtInfoEx : mTMtInfoList) {
			if (StringUtils.equals(tMtInfoEx.mLabel.mTerNo, terNo)) {
				return tMtInfoEx.mAlias;
			}
		}

		return null;
	}

	/**
	 * 移交权限后，并不会收到VCONFINFO通知，需要手动置为-1
	 */
	public static void resetChairMan() {
		if (null == mConfInfo) {
			return;
		}

		if (mConfInfo.mChairman != null) {
			mConfInfo.mChairman.mTerNo = "-1";
		}
	}

	/**
	 * 参会码率
	 * @param context
	 * @return
	 */
	public static int confCallRete(Context context) {
		if (context == null) {
			context = MyApplication.mOurApplication;
		}

		if (context == null) {
			return CALLRATE_SPLITLINE;
		}

		return 768;
	}

	/**
	 * 是否正在会议中
	 * @return
	 */
	public static boolean isCSVConf() {
		if (null == mLinkState) {
			return false;
		}

		// 正处于会议中
		if (mLinkState.isCSP2P() || mLinkState.isCSMCC()) {

			return true;
		}

		return false;
	}

	/**
	 * 当前会议是否是点对点会议
	 * @return
	 */
	public static boolean isP2PVConf() {
		if (null == mLinkState) {
			return false;
		}

		return mLinkState.isCSP2P();
	}

	/**
	 * 是否正在当前处于当前会议室的会议中
	 * @param e164
	 * @return
	 */
	public static boolean isCSVConf(String e164) {
		if (StringUtils.isNull(e164)) {
			return false;
		}

		if (!isCSVConf()) {
			return false;
		}

		return StringUtils.equals(mCallPeerE164Num, e164);
	}

	/**
	 * 进入音视频入会/音视频界面
	 * @param activity
	 * @param alias
	 * @param ipAddr
	 * @param e164
	 * @param isJoinVConf
	 */
	public static void openVConfActivity(Activity activity, String alias, String ipAddr, String e164, boolean isJoinVConf, int vconfType) {
		int callRate = confCallRete(MyApplication.mOurApplication);

		openVConfActivity(activity, alias, ipAddr, e164, (short) callRate, isJoinVConf, vconfType, false);
	}

	/**
	 * * 进入音视频入会/音视频界面
	 * @param activity
	 * @param alias
	 * @param ipAddr
	 * @param e164
	 * @param callRete
	 * @param isJoinVConf true: 入会
	 * @param vconfType 会议的创建形式：呼叫；召集；加入
	 */
	public static void openVConfActivity(Activity activity, String alias, String ipAddr, String e164, short callRete, boolean isJoinVConf, int vconfType, boolean isCommon) {
		if (activity == null || (StringUtils.isNull(ipAddr) && StringUtils.isNull(e164))) {
			return;
		}

		boolean isAudioConf = false;
		if (callRete <= 0) {
			callRete = CALLRATE_SPLITLINE;
		}
		if (callRete <= CALLRATE_SPLITLINE) {
			isAudioConf = true;
		}

		VideoConferenceService.isJoinConf = isJoinVConf;

		// 注册视频会议Service
		if (!isAudioConf) {
			VideoCapServiceManager.bindService();
		}

		Bundle b = new Bundle();
		b.putBoolean("JoinVConf", isJoinVConf);
		b.putString(MyApplication.ALIAS, alias);
		b.putString(MyApplication.E164NUM, e164);
		b.putString(MyApplication.IPAddr, ipAddr);
		b.putInt("vconf_type", vconfType);

		if (!isJoinVConf) {
			b.putBoolean("PullDown", true);
		}
		if(isCommon){
			b.putBoolean("isCommon", true);
		}

		if (VideoConferenceService.mSaveVconf == null || vconfType != VConfType.exist.ordinal()) {
			SaveVconf saveVconf = new SaveVconf();
			saveVconf.e164 = e164;
			saveVconf.vConfType = vconfType;
			saveVconf.isMackCall = true;
			saveVconf.vConfName = "";
			saveVconf.isAudio = isAudioConf;
			VideoConferenceService.mSaveVconf = saveVconf;
		}

		Intent intent = new Intent();
		if (isAudioConf) {
			// intent.setClass(activity, VConfAudioActivity.class);
		} else {
			// 视频入会
			if (isJoinVConf) {
				// intent.setClass(activity, JoinVConfVideoActivity.class);
			} else {
				intent.setClass(activity, VConfVideoActivity.class);
			}
		}
		intent.putExtras(b);
		activity.startActivity(intent);
	}

	/**
	 * 进入音视频/音视频界面
	 * <p>
	 * 注意：如果是非P2P呼叫，e164为会议E164，否则为个人e164
	 * </p>
	 * @param activity
	 * @param e164
	 * @param isAudioConf 音频
	 * @param isMackCall p2p呼叫会议
	 */
	public static void openVConfActivity(Activity activity, String e164, boolean isAudioConf, boolean isMackCall) {
		if (activity == null || e164 == null || e164.length() == 0) {
			return;
		}

		// 注册视频会议Service
		if (!isAudioConf) {
			VideoCapServiceManager.bindService();
		}

		Bundle b = new Bundle();
		b.putBoolean("PullDown", true);
		b.putBoolean("JoinVConf", false);
		b.putBoolean("MackCall", isMackCall);
		b.putString(MyApplication.E164NUM, e164);
		b.putInt("vconf_type", VConfType.exist.ordinal());

		Intent intent = new Intent();
		if (isAudioConf) {
			// intent.setClass(activity, VConfAudioActivity.class);
		} else {
			intent.setClass(activity, VConfVideoActivity.class);
		}
		intent.putExtras(b);
		activity.startActivity(intent);
	}

	/**
	 * 检测会议是否可用
	 * @param toastInfo 会议不可用时Toast提示
	 * @param reRegisterGK GK失败时重新连接
	 * @param isCheckVCVonf 检测是否正在会议中
	 * @return
	 */
	public static boolean isAvailableVCconf(boolean toastInfo, boolean reRegisterGK, boolean isCheckVCVonf) {
		Context context = MyApplication.mOurApplication;

		// 无网络提示
		if (!NetWorkUtils.isAvailable(context)) {
			if (toastInfo) {
				Toast.makeText(MyApplication.mOurApplication, "网络连接失败", Toast.LENGTH_SHORT).show();
			}
			return false;
		}

		// 2G网络不能入会
		if (NetWorkUtils.is2G(context)) {
			if (toastInfo) {
				Toast.makeText(MyApplication.mOurApplication, "2G网络不能入会", Toast.LENGTH_SHORT).show();
			}
			return false;
		}

		// GK注册失败
		if (!LoginFlowService.mRegisterGK) {
			if (reRegisterGK) {
				Toast.makeText(MyApplication.mOurApplication, "重连GK", Toast.LENGTH_SHORT).show();
				LoginFlowService.reRegGk();
			} else {
				if (toastInfo) {
					Toast.makeText(MyApplication.mOurApplication, "GK不可用", Toast.LENGTH_SHORT).show();
				}
			}
			return false;
		}

		// 正处于会议中
		if (isCheckVCVonf && isCSVConf()) {
			if (toastInfo) {
				Toast.makeText(MyApplication.mOurApplication, "当前正在会议中", Toast.LENGTH_SHORT).show();
			}
			return false;
		}

		return true;
	}

	

	/**
	 * 发起p2p视频呼叫
	 * 
	 * <pre>
	 * 音频呼叫返回数据
	 * H323UpLoadBitrateLessThan64
	 * CallPeerE164Num
	 * LinkState
	 * LinkState
	 * 
	 * 视频呼叫返回数据
	 * CallPeerE164Num
	 * LinkState
	 * </pre>
	 * @param ipAddr
	 * @param e164Num
	 * @param callRete =64：音频, >64视频
	 */
	public static void makeCallVideo(String ipAddr, String e164Num) {
		if (null == ipAddr) {
			ipAddr = "";
		}

		if (null == e164Num) {
			e164Num = "";
		}

		if (StringUtils.isNull(ipAddr) && StringUtils.isNull(e164Num)) {
			return;
		}

		short callRete = (short) confCallRete(null);
		short addrType = (short) EmMtAddrType.emIPAddr.ordinal();
		if (!StringUtils.isNull(e164Num)) {
			addrType = (short) EmMtAddrType.emE164.ordinal();
		}

		KdvMtBaseAPI.makeCall(callRete, addrType, ipAddr, e164Num);
	}

	/**
	 * 音频呼叫
	 * 
	 * <pre>
	 * 音频呼叫返回数据
	 * H323UpLoadBitrateLessThan64
	 * CallPeerE164Num
	 * LinkState
	 * LinkState
	 * </pre>
	 * @param ipAddr
	 * @param e164Num
	 */
	public static void makeCallAudio(String ipAddr, String e164Num) {
		if (null == ipAddr) {
			ipAddr = "";
		}

		if (null == e164Num) {
			e164Num = "";
		}

		if (StringUtils.isNull(ipAddr) && StringUtils.isNull(e164Num)) {
			return;
		}

		short addrType = (short) EmMtAddrType.emIPAddr.ordinal();
		if (!StringUtils.isNull(e164Num)) {
			addrType = (short) EmMtAddrType.emE164.ordinal();
		}

		// short callRete = (short) confCallRete(null);

		KdvMtBaseAPI.makeCall((short) CALLRATE_SPLITLINE, addrType, ipAddr, e164Num);
	}

	public static int sVconfStatus;

	/**
	 * * 保存VConf记录
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param isConnected 是否已接通
	 * @param info 如果是p2p，传入呼叫人（或被呼叫人）信息，直接通过JID存储记录
	 * @param linkState 连接状态
	 * @param e164 对e164号码
	 */
	public static void saveVConfRecord(long startTime, boolean isConnected) {
		if (null == mLinkState) {
			return;
		}

		boolean isRead = true;
		String talker = StringUtils.isNull(mCallPeerE164Num) ? mLinkState.getIpAddr() : mCallPeerE164Num;
		String name = mLinkState.getAlias();
		if (StringUtils.isNull(name)) {
			name = StringUtils.isNull(mCallPeerE164Num) ? mLinkState.getIpAddr() : mCallPeerE164Num;
		}

		if (!isConnected && !mLinkState.isbCalling()) {// 未连接，非主叫
			if (mLinkState.getReason() != EmCallDisconnectReason.local.ordinal()) {
				isRead = false;
			}
		} else {
			isRead = true;
		}

		// 被省略

		// 清除vconfservcice相关信息
		cleanConf();
	}

	/**
	 * 添加 会议包含终端信息
	 * @param tMtInfoList
	 * @param reset
	 */
	public static void addTMtInfoEx(List<TMtInfoEx> tMtInfoList, boolean reset) {
		if (null == mTMtInfoList) {
			mTMtInfoList = new ArrayList<TMtInfoEx>();
		}

		if (reset) {
			mTMtInfoList.clear();
		}

		if (null == tMtInfoList || tMtInfoList.isEmpty()) {
			return;
		}

		if (reset) {
			mTMtInfoList.addAll(tMtInfoList);
		}

		for (TMtInfoEx tMtInfoEx : tMtInfoList) {
			if (tMtInfoEx == null) continue;
			if (mTMtInfoList.contains(tMtInfoEx)) {
				mTMtInfoList.remove(tMtInfoEx);
			}
			mTMtInfoList.add(tMtInfoEx);
		}
	}

	/**
	 * 删除会议包含终端信息
	 * @param tMtInfoEx
	 */
	public static void delTmtInfoEx(TMtInfoEx tMtInfoEx) {
		if (tMtInfoEx == null || mTMtInfoList == null || mTMtInfoList.isEmpty()) {
			return;
		}

		mTMtInfoList.remove(tMtInfoEx);
	}

	/**
	 * 会议包含终端信息
	 * @return
	 */
	public static List<TMtInfoEx> getTMtInfoList() {
		return mTMtInfoList;
	}

	/**
	 * 重新设置音频模式切换
	 */
	public static void setStreamVolumeModel(final boolean force, final boolean isReceiverModel) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 连接到耳机，强制设置为听筒模式
				if (TerminalUtils.isWiredHeadsetOn(MyApplication.mOurApplication)) {
					TerminalUtils.setReceiverModel(MyApplication.mOurApplication, true, true);
					return;
				}

				// 强制设置模式
				if (force) {
					if (!isReceiverModel) {
						TerminalUtils.setSpeakerphoneOn(MyApplication.mOurApplication, true, true);
						return;
					}

					TerminalUtils.setReceiverModel(MyApplication.mOurApplication, true, true);

					return;
				}

				TerminalUtils.setSpeakerphoneOn(MyApplication.mOurApplication, true, true);
			}
		}).start();
	}

	/**
	 * 结束音视频通话时，恢复到扬声器模式
	 * 
	 * <pre>
	 * 每次入会会通过MtVConfInfo检测通话模式
	 * 所以，在恢复扬声器模式时，如果发现当前有耳机连接
	 * 则，先存储模式的字段即可
	 * </pre>
	 */
	public static void recoverSpeakerphoneOn() {
		final Context context = MyApplication.mOurApplication;
		if (context == null) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// 连接到耳机，设置模式无效
				if (TerminalUtils.isWiredHeadsetOn(context)) {
					return;
				}

				TerminalUtils.setSpeakerphoneOn(context, true, true);
			}
		}).start();
	}

	/**
	 * 关闭音视频相关界面
	 */
	public static void forceCloseVConfActivity() {
		Activity activity = AppStackManager.Instance().getActivity(VConfVideoActivity.class);
		if (activity != null) {
			AppStackManager.Instance().popActivity(activity);
		}

		// activity = AppStackManager.Instance().getActivity(VConfAudioActivity.class);
		// if (activity != null) {
		// AppStackManager.Instance().popActivity(activity);
		// }

		// activity = AppStackManager.Instance().getActivity(VConfInfoActivity.class);
		// if (activity != null) {
		// AppStackManager.Instance().popActivity(activity);
		// }
	}

	/**
	 * 退出会议
	 */
	public static void quitConf() {
		if (null == VideoConferenceService.mLinkState) return;

		// 结束点对点会议
		if (VideoConferenceService.mLinkState.isCSP2P()) {
			KdvMtBaseAPI.endP2PConf();
			return;
		}

		KdvMtBaseAPI.quitConf();
	}

	/**
	 * 退出音视频会议
	 * @param exceptionQuit 异常退出
	 */
	public static void quitConfAction(boolean exceptionQuit) {
		VideoCapServiceManager.unBindService();
		Activity currentActivity = AppStackManager.Instance().currentActivity();

		mSaveVconf = null;

		// 异常退会
		if (exceptionQuit) {
			cleanConf();

			KdvMtBaseAPI.codecStop();

			forceCloseVConfActivity();
		}
		mIsQuitAction = false;

		// 试着解除视频会议Service
		if (mLinkState == null || mLinkState.isCSHanup() || mLinkState.isCSIDLE()) {
			forceCloseVConfActivity();
		}

		recoverSpeakerphoneOn();
	}

}
