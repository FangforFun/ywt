package com.kedacom.truetouch.mtc;

import android.text.TextUtils;
import android.util.Log;

public class MyMtcCallback extends MtcCallback {

	public final String JNIHEADER = "com.kedacom.mvc_demo.mtc.jni.";

	@Override
	public void callback(String result) {
//		if (true) {
//			System.out.println("------------------------------");
//			System.out.println(result);
//			System.out.println("------------------------------");
//		}

		String eventType = getMessageChildFirstTag2(result);
		if (null == eventType) {
			return;
		}
		
		if ((!eventType.equals("CodecStatus")) 
				&& (!eventType.equals("VidDecRes"))
				&& (!eventType.equals("CodecFrameRate")))
		{
			System.out.println("------------------------------");
			System.out.println(result);
			System.out.println("------------------------------");
			
		}

		callbackClass(new StringBuffer().append(JNIHEADER).append(eventType).append("Callback").toString(), result);
	}

	private void callbackClass(String className, String xmlStr) {
		if (TextUtils.isEmpty(className) || TextUtils.isEmpty(xmlStr)) {
			return;
		}

		BaseCallbackHandler baseCallback = null;
		try {
			Class<?> c = Class.forName(className);
			if (c != null) {
				baseCallback = (BaseCallbackHandler) c.newInstance();
			}
		} catch (ClassNotFoundException e) {
			Log.e("MtcCallback", "not found Event Callback " + className, e);
		} catch (IllegalAccessException e) {
			Log.e("MtcCallback", "IllegalAccessException ", e);
		} catch (InstantiationException e) {
			Log.e("MtcCallback", "InstantiationException ", e);
		}

		if (baseCallback != null) {
			// baseCallback.setSynData(false);
			baseCallback.addCallback(xmlStr);
		}
	}

	/**
	 * message��ǩ�µĵ�һ���ӱ�ǩ
	 * 
	 * @param xml
	 * @return
	 */
	public String getMessageChildFirstTag2(String xml) {
		String resultChildFirstTag = "";
		try {
			String subXML = xml.substring(xml.indexOf("<Message>") + "<Message>".length());
			if (null == subXML) {
				return null;
			}
			subXML = subXML.substring(subXML.indexOf("<") + 1);
			if (null == subXML) {
				return null;
			}
			resultChildFirstTag = subXML.substring(0, subXML.indexOf(">"));
		} catch (Exception e) {
			Log.e("ParseXML", "getMessageChildFirstTag2", e);
		}

		return resultChildFirstTag;
	}

}
