/**
 * @(#)ReceiveConfInfoCallback.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc.
 *                                  All rights reserved.
 * 
 *                                  <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29391</EventID>
 * <Message>
 * <ReceiveConfInfo>
 * 			<Result>1</Result>
 * 			<ConfId></ConfId>
 * 			<StartTime>2013-8-9 11:7:40</StartTime>
 * 			<Duration>240</Duration>
 * 			<BitRate>384</BitRate>
 * 			<SecBitRate>0</SecBitRate>
 * 			<emMainVideoResolution>14</emMainVideoResolution>
 * 			<emSecondVideoResolution>0</emSecondVideoResolution>
 * 			<emDoubleVideoResolution>0</emDoubleVideoResolution>
 * 			<TalkHoldTime>0</TalkHoldTime>
 * 			<ConfPwd></ConfPwd>
 * 			<ConfName>摩云直播室</ConfName>
 * 			<ConfE164>051211#999966</ConfE164>
 * 			<bIsAudioPowerSel>0</bIsAudioPowerSel>
 * 			<bIsDiscussMode>1</bIsDiscussMode>
 * 			<bIsAutoVMP>1</bIsAutoVMP>
 * 			<bIsCustomVMP>0</bIsCustomVMP>
 * 			<bIsForceBroadcast>0</bIsForceBroadcast>
 * 			<Chairman>
 * 				<McuNo>192</McuNo>
 * 				<TerNo>1</TerNo>
 * 			</Chairman>
 * 			<Speaker>
 * 				<McuNo>192</McuNo>
 * 				<TerNo>1</TerNo>
 * 			</Speaker>
 * 			<PollInfo>
 * 				<emMode>0</emMode>
 * 				<emStat>0</emStat>
 * 				<KeepTime>0</KeepTime>
 * 				<MtNum>0</MtNum>
 * 				<MtInfoList>
 * 				</MtInfoList>
 * 			</PollInfo>
 * 			<VMPParam>
 * 				<bIsCustomVMP>0</bIsCustomVMP>
 * 				<bIsAutoVMP>1</bIsAutoVMP>
 * 				<bIsBroadcast>1</bIsBroadcast>
 * 				<emStyle>0</emStyle>
 * 				<MtList>
 * 				</MtList>
 * 				<MmbTypeList>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 					<MmbType>1</MmbType>
 * 				</MmbTypeList>
 * 		</VMPParam>
 * </ReceiveConfInfo>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.content.Intent;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.mvc_demo.vconf.bean.ConfInfo;
import com.kedacom.mvc_demo.vconf.bean.MtId;
import com.kedacom.mvc_demo.vconf.bean.MtInfo;
import com.kedacom.mvc_demo.vconf.bean.PollInfo;
import com.kedacom.mvc_demo.vconf.bean.VMPParam;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.pc.utils.StringUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author chenjian
 * @date 2013-8-9
 */

public class ReceiveConfInfoCallback extends BaseCallbackHandler {

	@SuppressWarnings("rawtypes")
	@Override
	public void addCallback(String strXML) {
		android.util.Log.i("vconf", "ReceiveConfInfo");

		if (null == strXML || 0 == strXML.length()) {
			return;
		}

		ConfInfo confInfo = null;
		try {
			Document doc = DocumentHelper.parseText(strXML);
			Element root = doc.getRootElement();
			Element messageElt = root.element("Message");
			Element receiveConfInfoElt = messageElt.element("ReceiveConfInfo");
			String result = receiveConfInfoElt.elementTextTrim("Result");
			if (!StringUtils.equals("1", result)) {
				VideoConferenceService.mConfInfo = null;
				return;
			}

			confInfo = new ConfInfo();

			confInfo.mConfId = receiveConfInfoElt.elementTextTrim("ConfId");
			confInfo.mStartTime = receiveConfInfoElt.elementTextTrim("StartTime");
			confInfo.mDuration = receiveConfInfoElt.elementTextTrim("Duration");
			confInfo.mBitRate = receiveConfInfoElt.elementTextTrim("BitRate");
			confInfo.mSecBitRate = receiveConfInfoElt.elementTextTrim("SecBitRate");
			confInfo.mEmMainVideoResolution = receiveConfInfoElt.elementTextTrim("emMainVideoResolution");
			confInfo.mEmSecondVideoResolution = receiveConfInfoElt.elementTextTrim("emSecondVideoResolution");
			confInfo.mEmDoubleVideoResolution = receiveConfInfoElt.elementTextTrim("emDoubleVideoResolution");
			confInfo.mTalkHoldTime = receiveConfInfoElt.elementTextTrim("TalkHoldTime");
			confInfo.mConfPwd = receiveConfInfoElt.elementTextTrim("ConfPwd");
			confInfo.mConfName = receiveConfInfoElt.elementTextTrim("ConfName");
			confInfo.mConfE164 = receiveConfInfoElt.elementTextTrim("ConfE164");

			String bIsAudioPowerSel = receiveConfInfoElt.elementTextTrim("bIsAudioPowerSel");
			confInfo.mIsAudioPowerSel = StringUtils.equals("1", bIsAudioPowerSel);

			String bIsDiscussMode = receiveConfInfoElt.elementTextTrim("bIsDiscussMode");
			confInfo.mIsDiscussMode = StringUtils.equals("1", bIsDiscussMode);

			String bIsAutoVMP = receiveConfInfoElt.elementTextTrim("bIsAutoVMP");
			confInfo.mIsAutoVMP = StringUtils.equals("1", bIsAutoVMP);

			String bIsCustomVMP = receiveConfInfoElt.elementTextTrim("bIsCustomVMP");
			confInfo.mIsCustomVMP = StringUtils.equals("1", bIsCustomVMP);

			String bIsForceBroadcast = receiveConfInfoElt.elementTextTrim("bIsForceBroadcast");
			confInfo.mIsForceBroadcast = StringUtils.equals("1", bIsForceBroadcast);

			// 主席终端
			Element chairmanElt = receiveConfInfoElt.element("Chairman");
			if (null != chairmanElt) {
				MtId chairman = new MtId();

				chairman.mMcuNo = chairmanElt.elementTextTrim("McuNo");
				chairman.mTerNo = chairmanElt.elementTextTrim("TerNo");

				confInfo.mChairman = chairman;
			}

			// 发言终端
			Element speakerElt = receiveConfInfoElt.element("Speaker");
			if (null != speakerElt) {
				MtId speaker = new MtId();

				speaker.mMcuNo = speakerElt.elementTextTrim("McuNo");
				speaker.mTerNo = speakerElt.elementTextTrim("TerNo");

				confInfo.mSpeaker = speaker;
			}

			Element pollInfoElt = receiveConfInfoElt.element("PollInfo");
			if (null != pollInfoElt) {
				PollInfo pollInfo = new PollInfo();

				pollInfo.mEmMode = pollInfoElt.elementTextTrim("emMode");
				pollInfo.mEmStat = pollInfoElt.elementTextTrim("emStat");
				pollInfo.mKeepTime = pollInfoElt.elementTextTrim("KeepTime");
				pollInfo.mMtNum = pollInfoElt.elementTextTrim("MtNum");

				Element mtInfoListElt = pollInfoElt.element("MtInfoList");
				Iterator mtInfoIT = mtInfoListElt.elementIterator("MtInfo");

				List<MtInfo> mtInfoList = new ArrayList<MtInfo>();
				while (mtInfoIT.hasNext()) {
					Element mtInfoElt = (Element) mtInfoIT.next();
					if (mtInfoElt == null) continue;

					MtInfo mtInfo = new MtInfo();
					MtId mtId = new MtId();

					mtId.mMcuNo = mtInfoElt.elementTextTrim("McuNo");
					mtId.mTerNo = mtInfoElt.elementTextTrim("TerNo");

					mtInfo.mAlias = mtInfoElt.elementTextTrim("Alias");
					mtInfo.mMtId = mtId;

					mtInfoList.add(mtInfo);
				}

				pollInfo.mMtInfoList = mtInfoList;

				confInfo.mPollInfo = pollInfo;
			}

			Element vMPParamElt = receiveConfInfoElt.element("VMPParam");
			if (null != vMPParamElt) {
				VMPParam vmpParam = new VMPParam();

				vmpParam.mIsCustomVMP = StringUtils.equals("1", vMPParamElt.elementTextTrim("bIsCustomVMP"));
				vmpParam.mIsAutoVMP = StringUtils.equals("1", vMPParamElt.elementTextTrim("bIsAutoVMP"));
				vmpParam.mIsBroadcast = StringUtils.equals("1", vMPParamElt.elementTextTrim("bIsBroadcast"));
				vmpParam.mEmStyle = vMPParamElt.elementTextTrim("emStyle");

				List<MtId> mtList = new ArrayList<MtId>();

				Element mtListElt = vMPParamElt.element("MtList");
				Iterator mtIT = mtListElt.elementIterator("Mt");
				while (mtIT.hasNext()) {
					Element mtElt = (Element) mtIT.next();
					if (mtElt == null) continue;

					MtId mtId = new MtId();

					mtId.mMcuNo = mtElt.elementTextTrim("McuNo");
					mtId.mTerNo = mtElt.elementTextTrim("TerNo");

					mtList.add(mtId);
				}
				vmpParam.mMtList = mtList;

				List<String> mmbTypeList = new ArrayList<String>();

				Element mmbTypeListELt = vMPParamElt.element("MmbTypeList");
				Iterator mmbTypeIT = mmbTypeListELt.elementIterator("MmbType");
				while (mmbTypeIT.hasNext()) {
					Element mmbTypeElt = (Element) mmbTypeIT.next();
					if (null == mmbTypeElt) continue;

					mmbTypeList.add(mmbTypeElt.getTextTrim());
				}
				vmpParam.mMmbTypeList = mmbTypeList;

				confInfo.mVMPParam = vmpParam;
			}

			VideoConferenceService.mConfInfo = confInfo;
			Intent intent = new Intent();
			intent.putExtra("McuNo", confInfo.mChairman.mMcuNo);
			intent.putExtra("TerNo", confInfo.mChairman.mTerNo);
			intent.setAction(VideoConferenceService.REVEIVECONFINFO_ACTION);
			MyApplication.getApplication().sendBroadcast(intent);
			Activity currentActivity = AppStackManager.Instance().currentActivity();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
	}

}
