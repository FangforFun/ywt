/**
 * @(#)CodecStatusCallback.java 2013-8-15 Copyright 2013 it.kedacom.com, Inc.
 *                              All rights reserved.
 * 
 *                              <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29276</EventID>
 * <Message>
 * <CodecStatus>
 * 	<Result>1</Result>
 * 	<CallInfoStatistics>
 * 		<CallBitrate>1024</CallBitrate>
 * 		<RecvEncryptArithmetic>2</RecvEncryptArithmetic>
 * 		<SendEncryptArithmetic>2</SendEncryptArithmetic>
 * 		<RecvAudioStatistics>
 * 			<AudioFormat>8</AudioFormat>
 * 			<CodecPackStat>
 * 				<Bitrate>24</Bitrate>
 * 				<AvrBitrate>24</AvrBitrate>
 * 				<LostPacks>45</LostPacks>
 * 				<TotalPacks>0</TotalPacks>
 * 				<Frames>13792</Frames>
 * 				<LostFrameRate>0</LostFrameRate>
 * 			</CodecPackStat>
 * 		</RecvAudioStatistics>
 * 		<SendAudioStatistics>
 * 			<AudioFormat>8</AudioFormat>
 * 			<CodecPackStat>
 * 				<Bitrate>24</Bitrate>
 * 				<AvrBitrate>0</AvrBitrate>
 * 				<LostPacks>0</LostPacks>
 * 				<TotalPacks>0</TotalPacks>
 * 				<Frames>0</Frames>
 * 				<LostFrameRate>0</LostFrameRate>
 * 			</CodecPackStat>
 * 		</SendAudioStatistics>
 * 		<PrimoRecvVideoStatistics>
 * 			<bH239>0</bH239>
 * 			<VideoFormat >6</VideoFormat >
 * 			<VideoResolution >29</VideoResolution >
 * 			<CodecPackStat>
 * 				<Bitrate>0</Bitrate>
 * 				<AvrBitrate>0</AvrBitrate>
 * 				<LostPacks>0</LostPacks>
 * 				<TotalPacks>0</TotalPacks>
 * 				<Frames>0</Frames>
 * 				<LostFrameRate>0</LostFrameRate>
 * 			</CodecPackStat>
 * 		</PrimoRecvVideoStatistics>
 * 		<PrimoSendVideoStatistics>
 * 			<bH239>0</bH239>
 * 			<VideoFormat >6</VideoFormat >
 * 			<VideoResolution >29</VideoResolution >
 * 			<CodecPackStat>
 * 				<Bitrate>0</Bitrate>
 * 				<AvrBitrate>0</AvrBitrate>
 * 				<LostPacks>0</LostPacks>
 * 				<TotalPacks>0</TotalPacks>
 * 				<Frames>0</Frames>
 * 				<LostFrameRate>0</LostFrameRate>
 * 			</CodecPackStat>
 * 		</PrimoSendVideoStatistics>
 * 		<SecondRecvVideoStatistics>
 * 			<bH239>0</bH239>
 * 			<VideoFormat >6</VideoFormat >
 * 			<VideoResolution >29</VideoResolution >
 * 			<CodecPackStat>
 * 				<Bitrate>0</Bitrate>
 * 				<AvrBitrate>0</AvrBitrate>
 * 				<LostPacks>0</LostPacks>
 * 				<TotalPacks>0</TotalPacks>
 * 				<Frames>0</Frames>
 * 				<LostFrameRate>0</LostFrameRate>
 * 			</CodecPackStat>
 * 		</SecondRecvVideoStatistics>
 * 		<SecondSendVideoStatistics>
 * 			<bH239>0</bH239>
 * 			<VideoFormat >6</VideoFormat >
 * 			<VideoResolution >29</VideoResolution >
 * 			<CodecPackStat>
 * 				<Bitrate>0</Bitrate>
 * 				<AvrBitrate>0</AvrBitrate>
 * 				<LostPacks>0</LostPacks>
 * 				<TotalPacks>0</TotalPacks>
 * 				<Frames>0</Frames>
 * 				<LostFrameRate>0</LostFrameRate>
 * 			</CodecPackStat>
 * 		</SecondSendVideoStatistics>
 * 	</CallInfoStatistics>
 * </CodecStatus>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.util.Log;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * @author chenjian
 * @date 2013-8-15
 */

public class CodecStatusCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		Log.i("vconf", "CodecStatus");

		if (null == strXML || 0 == strXML.length()) {
			return;
		}

		// 保证只在音视频播放界面时才接收数据
		Activity currActivity = AppStackManager.Instance().currentActivity();
		if (null == currActivity) {
			return;
		}

		// if (!(currActivity instanceof VConfAudioActivity) && !(currActivity instanceof VConfVideoActivity)) {
		// return;
		// }

		if (!(currActivity instanceof VConfVideoActivity)) {
			return;
		}

		// parseXML(strXML);

		// // 当前正处于音频播放界面
		// if (currActivity instanceof VConfAudioActivity) {
		// VConfAudioActivity vconfAudioActivity = (VConfAudioActivity) currActivity;
		// if (vconfAudioActivity.getVConfFunctionView() == null) {
		// return;
		// }
		//
		// vconfAudioActivity.getVConfFunctionView().showCodeStatusDetails(mCallInfoStatistics, null);
		// }
		// // 当前正处于视频播放界面
		// else

		if (currActivity instanceof VConfVideoActivity) {
			VConfVideoActivity vconfVideoActivity = (VConfVideoActivity) currActivity;
			if (vconfVideoActivity.getVConfFunctionView() == null) {
				return;
			}

			// vconfVideoActivity.getVConfFunctionView().showCodeStatusDetails(mCallInfoStatistics, null);
		}
	}

}
