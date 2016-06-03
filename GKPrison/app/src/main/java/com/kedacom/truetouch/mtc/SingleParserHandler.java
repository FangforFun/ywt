package com.kedacom.truetouch.mtc;

import android.text.TextUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

/**
 * 针对的XML String（没有重复标签）的解
 * 
 * @author cj
 */
public class SingleParserHandler extends DefaultHandler {

	private Properties mProps;
	// private String currentName;
	private StringBuffer mCurrentValue;

	public SingleParserHandler() {
		mProps = new Properties();
		mCurrentValue = new StringBuffer();
	}

	public Properties getProperties() {
		return mProps;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	/**
	 * 当遇到开始标签时被调用，比如 <tag attribute="att"> 可以得到标签属
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		mCurrentValue.delete(0, mCurrentValue.length());
		// currentName = localName;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		mCurrentValue.append(ch, start, length);
		// if (!TextUtils.isEmpty(currentName)
		// && !StringUtils.isNullNotTrim(currentValue.toString())) {
		// //String v = currentValue.toString().trim();
		// String v = currentValue.toString();
		// props.put(currentName, v);
		// currentName = null;
		// }
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (!TextUtils.isEmpty(localName)
				&& !TextUtils.isEmpty(mCurrentValue.toString())) {
			String v = mCurrentValue.toString();
			mProps.put(localName, v);
		}
		super.endElement(uri, localName, qName);
	}

}
