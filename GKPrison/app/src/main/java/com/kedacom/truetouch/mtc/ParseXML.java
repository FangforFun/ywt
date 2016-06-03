package com.kedacom.truetouch.mtc;

import android.text.TextUtils;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ParseXML {

	// 定Properties 用来存放标签
	private Properties props;

	public Properties getProps() {
		return props;
	}

	public void parseXML(String xml) {
		if (TextUtils.isEmpty(xml))
			return;

		// 对字符串解析
		StringReader reader = new StringReader(xml);
		InputSource is = new InputSource(reader);
		is.setCharacterStream(reader);
		try {
			// 获取SAX解析
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			SAXParser parser = saxFactory.newSAXParser();

			// 从SAXParser获取XMLReader
			XMLReader xmlReader = parser.getXMLReader();

			SingleParserHandler parserHandler = new SingleParserHandler();
			xmlReader.setContentHandler(parserHandler);
			xmlReader.parse(is);

			props = parserHandler.getProperties();
		} catch (Exception e) {
			Log.e("ParseXML", "parseXML", e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

}
