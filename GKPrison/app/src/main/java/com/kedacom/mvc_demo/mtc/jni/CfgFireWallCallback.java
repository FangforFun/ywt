/**
 * @(#)CfgFireWallCallback.java 2013-11-5 Copyright 2013 it.kedacom.com, Inc.
 *                              All rights reserved.
 * 
 *                              <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29571</EventID>
 * <Message>
 * <CfgFireWall>
 * 		<Result>1</Result>
 * 		<EmbedFwNatProxy>
 * 			<bUsed>1</bUsed>
 * 			<FwNatProxyServIP>172.16.79.8</FwNatProxyServIP>
 * 			<FwNatProxyServListenPort>2776</FwNatProxyServListenPort>
 * 			<StreamBasePort>0</StreamBasePort>
 * 		</EmbedFwNatProxy>
 * 	</CfgFireWall>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * @author chenjian
 * @date 2013-11-5
 */

public class CfgFireWallCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		// loadParseSingleXML(strXML);
		// if (!result4SingleXML()) {
		// return;
		// }
		//
		// String bUsed = getSingleObject("bUsed");
		// String fwNatProxyServIP = getSingleObject("FwNatProxyServIP");
		// String fwNatProxyServListenPort =
		// getSingleObject("FwNatProxyServListenPort");
		// String streamBasePort = getSingleObject("StreamBasePort");
	}

}
