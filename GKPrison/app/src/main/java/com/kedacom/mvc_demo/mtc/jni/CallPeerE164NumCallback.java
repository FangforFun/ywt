/**
 * @(#)CallPeerE164NumCallback.java 2013-7-30 Copyright 2013 it.kedacom.com,
 *                                  Inc. All rights reserved.
 * 
 *                                  <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * 		<EventID>
 * 		29874
 * 		</EventID>
 * 		<Message>
 * 			<CallPeerE164Num>
 * 				<Result>1</Result>
 * 				<PeerE164Num>0512112991005</PeerE164Num>
 * 			</CallPeerE164Num>
 * 		</Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * 
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * @author chenjian
 * @date 2013-7-30
 */

public class CallPeerE164NumCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		android.util.Log.i("vconf", "CallPeerE164Num");

		loadParseSingleXML(strXML);

		boolean r = result4SingleXML();

		// Intent intent = new Intent();
		// intent.setAction(VideoConferenceService.CALLPEERE164NUM_ACTION);
		// Bundle b = new Bundle();
		// b.putBoolean(TruetouchGlobal.RESULT, r);
		// intent.putExtras(b);
		// TruetouchGlobal.sendBroadcast(intent, null);

		if (!r) {
			VideoConferenceService.mCallPeerE164Num = "";

			return;
		}

		VideoConferenceService.mCallPeerE164Num = getSingleObject("PeerE164Num");

		// MtcLib.confDetailInfoReq(VideoConferenceService.mCallPeerE164Num);
	}

}
