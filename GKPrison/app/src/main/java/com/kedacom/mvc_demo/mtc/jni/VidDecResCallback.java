/**
 * @(#)VidDecResCallback.java   2014-7-7
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 <pre>
 ------------------------------
<?xml version="1.0" encoding="utf-8"?>
<TrueTouchAndroid>
<EventID>29851</EventID>
<Message>
<VidDecRes>
	<Result>1</Result>
	<VidDecResInfo>
		<PrimeWidth>0</PrimeWidth>
		<PrimeHeight>0</PrimeHeight>
		<SecondWidth>0</SecondWidth>
		<SeoncdHeight>0</SeoncdHeight>
	</VidDecResInfo>
</VidDecRes>
</Message>
</TrueTouchAndroid>
------------------------------
 *
 *
 */

package com.kedacom.mvc_demo.mtc.jni;

import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
  * 
  * @author chenj
  * @date 2014-7-7
  */

public class VidDecResCallback extends BaseCallbackHandler {

	/**
	 * @see com.kedacom.truetouch.mtc.BaseCallbackHandler#addCallback(String)
	 */
	@Override
	public void addCallback(String strXML) {
		loadParseSingleXML(strXML);
		if (!result4SingleXML()) {
			return;
		}
	}

}
