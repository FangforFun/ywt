package com.kedacom.truetouch.mtc;

import android.text.TextUtils;
import android.util.Log;

import java.util.Properties;

public abstract class BaseCallbackHandler {

	public final String RESULT_SUCCED = "1";
	public final String RESULT_FAILED = "0";

	public ParseXML mParseXML;

	public BaseCallbackHandler() {
		mParseXML = new ParseXML();
	}

	private boolean mIsSyn = false;

	public void setSynData(boolean syn) {
		mIsSyn = syn;
	}

	public boolean isSynData() {
		return mIsSyn;
	}

	public abstract void addCallback(String strXML);

	public void loadParseSingleXML(String strXML) {
		if (TextUtils.isEmpty(strXML) || mParseXML == null)
			return;

		try {
			mParseXML.parseXML(strXML);
		} catch (Exception e) {
			Log.e("BaseCallbackHandler", "loadParseStr", e);
		}
	}

	public String getSingleObject(String keyname) {
		if (TextUtils.isEmpty(keyname) || mParseXML == null) {
			return null;
		}

		Properties tProperties = mParseXML.getProps();
		if (tProperties == null) {
			return null;
		}

		if (!tProperties.containsKey(keyname)) {
			return null;
		}

		return tProperties.getProperty(keyname);
	}

	public boolean result4SingleXML() {
		String strResult = getSingleObject("Result");
		if (TextUtils.isEmpty(strResult))
			return false;

		if ("0".equals(strResult))
			return false;

		if ("1".equals(strResult))
			return true;

		return false;
	}

}
