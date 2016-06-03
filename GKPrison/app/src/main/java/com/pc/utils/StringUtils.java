/**
 * $RCSfile: StringUtils.java,v $ $Revision: 1.11.2.2 $ $Date: 2001/01/16
 * 06:06:07 $ Copyright (C) 2000 CoolServlets.com. All rights reserved.
 * =================================================================== The
 * Apache Software License, Version 1.1 Redistribution and use in source and
 * binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following
 * disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. 3. The
 * end-user documentation included with the redistribution, if any, must include
 * the following acknowledgment: "This product includes software developed by
 * CoolServlets.com (http://www.coolservlets.com)." Alternately, this
 * acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear. 4. The names "Jive" and
 * "CoolServlets.com" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written permission,
 * please contact webmaster@coolservlets.com. 5. Products derived from this
 * software may not be called "Jive", nor may "Jive" appear in their name,
 * without prior written permission of CoolServlets.com. THIS SOFTWARE IS
 * PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COOLSERVLETS.COM OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ==================================================================== This
 * software consists of voluntary contributions made by many individuals on
 * behalf of CoolServlets.com. For more information on CoolServlets.com, please
 * see <http://www.coolservlets.com>.
 */

package com.pc.utils;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * String基本处理方式 Utility class to peform common String manipulation algorithms.
 */
public class StringUtils {

	public final static String NULL = "null";

	public static final String lineSeparator = System.getProperty("line.separator");


	public static boolean equals(CharSequence a, CharSequence b) {
		if (a == b) return true;

		int length;
		if (a != null && b != null && (length = a.length()) == b.length()) {
			if (a instanceof String && b instanceof String) {
				return a.equals(b);
			} else {
				for (int i = 0; i < length; i++) {
					if (a.charAt(i) != b.charAt(i)) return false;
				}
				return true;
			}
		}

		return false;
	}

	public static boolean equalsStr(String aStr, String bStr, boolean ignoreCase) {
		if (aStr == bStr) return true;

		try {
			if (ignoreCase) {
				return aStr.equalsIgnoreCase(bStr);
			} else {
				return aStr.equals(bStr);
			}
		} catch (Exception e) {
		}

		return false;
	}

	public static boolean containsStr(String aStr, String bStr, boolean ignoreCase) {
		if (aStr == bStr) return true;

		try {
			String tmpAstr = aStr;
			String tmpBstr = bStr;
			if (ignoreCase) {
				if (!StringUtils.isNull(aStr)) {
					tmpAstr = aStr.toLowerCase();
				}
				if (!StringUtils.isNull(bStr)) {
					tmpBstr = aStr.toLowerCase();
				}
			}

			return tmpAstr.contains(tmpBstr);
		} catch (Exception e) {
		}

		return false;
	}

	public static String trim(String s) {
		return s == null ? null : s.trim();
	}

	/**
	 * 删除左边第一个空格
	 */
	public static String ltrim(String source) {
		try {
			return source.replaceFirst("^\\s", "");
		} catch (Exception e) {
		}
		return source;
	}

	/**
	 * 删除右边最后一个空格
	 */
	public static String rtrim(String source) {
		try {
			return source.replaceFirst("\\s+$", "");
		} catch (Exception e) {
		}
		return source;
	}

	public static String lrtrim(String source) {
		try {
			return source.replaceAll("^\\s+.*(\\s+)$", "");
		} catch (Exception e) {
		}
		return source;
	}


	/**
	 * @param str
	 * @return
	 */
	public static boolean isNotNullString(String str) {
		return !"".equals(str) && !str.equalsIgnoreCase("null");
	}

	/**
	 * 第一个参数为以逗号分隔各个元素的字符串，第二个参数为不含逗号的字符串， 返回第一个参数字符串是否包含与第二个参数字符串相等的元素
	 * @param ids example:"x,y,z"
	 * @param id example:"x"
	 * @return
	 */
	public static boolean contains(String ids, String id) {
		if (ids != null) {
			String[] idArray = ids.split(",");
			for (String idStr : idArray) {
				if (id.equals(idStr)) {
					return true;
				}
			}
		}
		return false;
	}

	/** 将Long列表转换成逗号分隔的字符 */
	public static String convertLongListToCommaDelimitedString(List<Long> longs) {
		// 如果List中分别放着1、2、3，则toString后为[1, 2,3]，
		// 去掉[,空格,]就变成了1,2,3。这里\\s表示空格，用了正则表达式，[]表示其中任意一个
		return longs.toString().replaceAll("[\\s\\]\\[]", "");

	}


	/**
	 * Replaces all instances of oldString with newString in line.
	 * @param line the String to search to perform replacements on
	 * @param oldString the String that should be replaced by newString
	 * @param newString the String that will replace all instances of oldString
	 * @return a String will all instances of oldString replaced by newString
	 */
	public static final String replace(String line, String oldString, String newString) {
		if (line == null) {
			return null;
		}
		int i = 0;
		if ((i = line.indexOf(oldString, i)) >= 0) {
			char[] line2 = line.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j = i;
			while ((i = line.indexOf(oldString, i)) > 0) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
				j = i;
			}
			buf.append(line2, j, line2.length - j);
			return buf.toString();
		}
		return line;
	}

	public static String replaceSubstring(String s__Text, String s__Src, String s__Dest) {
		try {
			if (s__Text == null || s__Src == null || s__Dest == null) return null;
			int i = 0;
			int i_SrcLength = s__Src.length();
			int i_DestLength = s__Dest.length();
			do {
				int j = s__Text.indexOf(s__Src, i);
				if (-1 == j) break;
				s__Text = s__Text.substring(0, j).concat(s__Dest).concat(s__Text.substring(j + i_SrcLength));
				i = j + i_DestLength;
			} while (true);
		} catch (Exception e) {

		}
		return s__Text;
	}

	/**
	 * Replaces all instances of oldString with newString in line with the added
	 * feature that matches of newString in oldString ignore case.
	 * @param line the String to search to perform replacements on
	 * @param oldString the String that should be replaced by newString
	 * @param newString the String that will replace all instances of oldString
	 * @return a String will all instances of oldString replaced by newString
	 */
	public static final String replaceIgnoreCase(String line, String oldString, String newString) {
		if (line == null) {
			return null;
		}
		String lcLine = line.toLowerCase();
		String lcOldString = oldString.toLowerCase();
		int i = 0;
		if ((i = lcLine.indexOf(lcOldString, i)) >= 0) {
			char[] line2 = line.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j = i;
			while ((i = lcLine.indexOf(lcOldString, i)) > 0) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
				j = i;
			}
			buf.append(line2, j, line2.length - j);
			return buf.toString();
		}
		return line;
	}

	/**
	 * Replaces all instances of oldString with newString in line. The count
	 * Integer is updated with number of replaces.
	 * @param line the String to search to perform replacements on
	 * @param oldString the String that should be replaced by newString
	 * @param newString the String that will replace all instances of oldString
	 * @return a String will all instances of oldString replaced by newString
	 */
	public static final String replace(String line, String oldString, String newString, int[] count) {
		if (line == null) {
			return null;
		}
		int i = 0;
		if ((i = line.indexOf(oldString, i)) >= 0) {
			int counter = 0;
			counter++;
			char[] line2 = line.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j = i;
			while ((i = line.indexOf(oldString, i)) > 0) {
				counter++;
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
				j = i;
			}
			buf.append(line2, j, line2.length - j);
			count[0] = counter;
			return buf.toString();
		}
		return line;
	}

	/**
	 * This method takes a string which may contain HTML tags (ie, &lt;b&gt;,
	 * &lt;table&gt;, etc) and converts the '&lt'' and '&gt;' characters to
	 * their HTML escape sequences.
	 * @param input the text to be converted.
	 * @return the input string with the characters '&lt;' and '&gt;' replaced
	 *         with their HTML escape sequences.
	 */
	public static final String escapeHTMLTags(String input) {
		// Check if the string is null or zero length -- if so, return
		// what was sent in.
		if (input == null || input.length() == 0) {
			return input;
		}
		// Use a StringBuffer in lieu of String concatenation -- it is
		// much more efficient this way.
		StringBuffer buf = new StringBuffer(input.length());
		char ch = ' ';
		for (int i = 0; i < input.length(); i++) {
			ch = input.charAt(i);
			if (ch == '<') {
				buf.append("&lt;");
			} else if (ch == '>') {
				buf.append("&gt;");
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	/**
	 * Used by the hash method.
	 */
	private static MessageDigest digest = null;

	/**
	 * Turns an array of bytes into a String representing each byte as an
	 * unsigned hex number.
	 * <p>
	 * Method by Santeri Paavolainen, Helsinki Finland 1996<br>
	 * (c) Santeri Paavolainen, Helsinki Finland 1996<br>
	 * Distributed under LGPL.
	 * @param hash an rray of bytes to convert to a hex-string
	 * @return generated hex string
	 */
	public static final String toHex(byte hash[]) {
		StringBuffer buf = new StringBuffer(hash.length * 2);
		int i;

		for (i = 0; i < hash.length; i++) {
			if (((int) hash[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) hash[i] & 0xff, 16));
		}
		return buf.toString();
	}

	/**
	 * Converts a line of text into an array of lower case words. Words are
	 * delimited by the following characters: , .\r\n:/\+
	 * <p>
	 * In the future, this method should be changed to use a
	 * BreakIterator.wordInstance(). That class offers much more fexibility.
	 * @param text a String of text to convert into an array of words
	 * @return text broken up into an array of words.
	 */
	public static final String[] toLowerCaseWordArray(String text) {
		if (text == null || text.length() == 0) {
			return new String[0];
		}
		StringTokenizer tokens = new StringTokenizer(text, " ,\r\n.:/\\+");
		String[] words = new String[tokens.countTokens()];
		for (int i = 0; i < words.length; i++) {
			words[i] = tokens.nextToken().toLowerCase();
		}
		return words;
	}

	/**
	 * A list of some of the most common words. For searching and indexing, we
	 * often want to filter out these words since they just confuse searches.
	 * The list was not created scientifically so may be incomplete :)
	 */
	private static final String[] commonWords = new String[] {
			"a", "and", "as", "at", "be", "do", "i", "if", "in", "is", "it", "so", "the", "to"
	};
	private static Map<String, String> commonWordsMap = null;


	public static String convertNull(String input) {
		String NULL_TAG = " ";
		String result = StringUtils.replace(input, "\r\n", NULL_TAG);
		result = StringUtils.replace(result, "	", NULL_TAG);
		return StringUtils.replace(result, "\n", NULL_TAG);
	}

	public static String convertLine(String input) {
		String BR_TAG = "<br>";
		String result = StringUtils.replace(input, "\r\n", BR_TAG);
		return StringUtils.replace(result, "\n", BR_TAG);
	}

	public static String dumpNew(String input, int lineNum) {
		String input1 = convertLine(input);
		// int[] poses = new int[input1.length()];
		int pos1 = 0;
		int pos2 = 0;
		int j = 0;
		StringBuffer strTemp = new StringBuffer();
		for (int i = 0; i < input1.length(); i++) {
			pos1 = input1.indexOf("<br>", i);
			System.out.print("pos1=" + pos1);
			if (pos1 - i > lineNum) {
				strTemp.append(input1.substring(i, i + lineNum) + "<br>");
				i = i + lineNum - 1;
			} else if (pos1 >= 0 && pos1 - i <= lineNum) {
				strTemp.append(input1.substring(i, pos1 + 4));
				i = pos1 + 3;
			} else if (pos1 == -1 && input1.length() - i > lineNum) {
				strTemp.append(input1.substring(i, i + lineNum) + "<br>");
				i = i + lineNum - 1;
			} else if (pos1 == -1 && input1.length() - i <= lineNum) {
				strTemp.append(input1.substring(i));
				break;
			}
		}
		return strTemp.toString();

	}

	public int[] parseIP(String IP) {
		int invalida[] = {
				-1, -1, -1, -1
		};
		int ia[] = {
				-1, -1, -1, -1
		};
		if (IP == null) return invalida;
		int len = IP.length();
		int i = 0;
		int b = 0;
		int dot = 0;
		while (i < len) {
			char c = IP.charAt(i++);
			if (c >= '0' && c <= '9')
				b = (b * 10 + c) - 48;
			else if (c == '.') {
				ia[dot] = b;
				if (++dot >= 4) return invalida;
				b = 0;
			} else {
				return invalida;
			}
		}
		if (dot == 3) {
			ia[dot] = b;
			return ia;
		} else {
			return invalida;
		}
	}

	public static String packageOf(Class aClass) {
		if (aClass == null) {
			throw new IllegalArgumentException("StringUtils: Argument \"aClass\" cannot be null.");
		}
		String result = "";
		int index = aClass.getName().lastIndexOf(".");
		if (index >= 0) {
			result = aClass.getName().substring(0, index);
		}
		return result;
	}

	public static String nameOf(Class aClass) {
		if (aClass == null) {
			throw new IllegalArgumentException("StringUtils: Argument \"aClass\" cannot be null.");
		}
		String className = aClass.getName();
		int index = className.lastIndexOf(".");
		if (index >= 0) {
			className = className.substring(index + 1);
		}
		return className;
	}

	public static final byte[] decodeHex(String hex) {
		char[] chars = hex.toCharArray();
		// byte[] bytes = new byte[chars.length/2];
		byte[] bytes = new byte[hex.length() / 2];
		System.out.println("hex.length()/2=" + (hex.length() / 2));
		int byteCount = 0;
		for (int i = 0; i < chars.length; i += 2) {
			byte newByte = 0x00;
			newByte |= hexCharToByte(chars[i]);
			newByte <<= 4;
			newByte |= hexCharToByte(chars[i + 1]);
			bytes[byteCount] = newByte;
			byteCount++;
		}
		return bytes;
	}

	private static final byte hexCharToByte(char ch) {
		switch (ch) {
			case '0':
				return 0x00;
			case '1':
				return 0x01;
			case '2':
				return 0x02;
			case '3':
				return 0x03;
			case '4':
				return 0x04;
			case '5':
				return 0x05;
			case '6':
				return 0x06;
			case '7':
				return 0x07;
			case '8':
				return 0x08;
			case '9':
				return 0x09;
			case 'a':
				return 0x0A;
			case 'b':
				return 0x0B;
			case 'c':
				return 0x0C;
			case 'd':
				return 0x0D;
			case 'e':
				return 0x0E;
			case 'f':
				return 0x0F;
			case 'A':
				return 0x0A;
			case 'B':
				return 0x0B;
			case 'C':
				return 0x0C;
			case 'D':
				return 0x0D;
			case 'E':
				return 0x0E;
			case 'F':
				return 0x0F;

		}
		return 0x00;
	}

	public static String toDB(String in) {
		if (in == null) return null;
		String out = "";
		int i = 0;
		int len = in.length();
		try {
			while (i < len) {
				char c;
				switch (c = in.charAt(i)) {
					case 34: // '"'
						out = out.concat("&quot;");
						break;

					case 39: // '\''
						out = out.concat("&apos;");
						break;

					case 63: // '?'
						out = out.concat("&qst;");
						break;

					case 38: // '&'
						out = out.concat("&amp;");
						break;

					case 60: // '<'
						out = out.concat("&lt;");
						break;

					case 62: // '>'
						out = out.concat("&gt;");
						break;

					case 36: // '$'
						out = out.concat("$$");
						break;

					default:
						if (c >= '~') {
							out = out.concat("&#" + (int) c);
							out = out.concat(";");
							break;
						}
						if (c < ' ') {
							out = out.concat("&#" + (int) c);
							out = out.concat(";");
						} else {
							out = out.concat(Character.toString(c));
						}
						break;
				}
				i++;
			}
		} catch (Exception e) {

		}
		return out;
	}

	public static String fromDB(String in) {
		if (in == null) return null;
		String out = in;
		out = replaceSubstring(out, "&amp;", "&");
		out = replaceSubstring(out, "&apos;", "'");
		out = replaceSubstring(out, "&quot;", "\"");
		out = replaceSubstring(out, "&qst;", "?");
		out = replaceSubstring(out, "&lt;", "<");
		out = replaceSubstring(out, "&gt;", ">");
		out = replaceSubstring(out, "$$", "$");
		int i = 0;
		do {
			i = 0;
			int j = out.indexOf("&#", i);
			int k = out.indexOf(";", j);
			if (-1 != j && -1 != k) {
				String number = out.substring(j + 2, k);
				int i_number = (Integer.valueOf(number)).intValue();
				char c = (char) i_number;
				String dest = Character.toString(c);
				out = out.substring(0, j).concat(dest).concat(out.substring(k + 1));
			} else {
				return out;
			}
		} while (true);
	}


	public static void newLine(StringBuffer buffer, int indent) {
		buffer.append(StringUtils.lineSeparator);
		indent(buffer, indent);
	}

	public static void indent(StringBuffer buffer, int indent) {
		for (int i = 0; i < indent; i++)
			buffer.append(' ');
	}

	public static boolean isNull(String str) {
		boolean b = false;
		if (str == null || str.trim().length() == 0) b = true;

		return b;
	}

	public static boolean isNullNotTrim(String str) {
		boolean b = false;
		if (str == null) b = true;

		return b;
	}

	public static boolean isNULL(String str) {
		boolean b = false;
		if (str == null) b = true;

		return b;
	}

	public static boolean isNull(String str, boolean bValidNullString) {
		boolean b = false;
		if (str == null || str.trim().length() == 0) b = true;
		if (!b && bValidNullString) {
			if (str != null && str.equalsIgnoreCase("null")) b = true;
		}
		return b;
	}

	public static boolean isEquals(String str1, String str2) {
		if (isNull(str1) && isNull(str2)) {
			return true;
		}

		if (isNull(str1) && !isNull(str2)) {
			return false;
		}

		if (!isNull(str1) && isNull(str2)) {
			return false;
		}

		if (str1.equals(str2)) {
			return true;
		}

		return false;
	}


	public static boolean str2Boolean(String s, boolean defaultV) {
		if (StringUtils.isNull(s)) return defaultV;
		if (s != null && s.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	public static int str2Int(String s, int defaultV) {
		if (s != null && !s.equals("")) {
			int num = defaultV;
			try {
				num = Integer.parseInt(s);
			} catch (Exception ignored) {
			}
			return num;
		} else {
			return defaultV;
		}
	}

	public static short str2Short(String s, short defaultV) {
		if (s != null && !s.equals("")) {
			short num = defaultV;
			try {
				num = Short.parseShort(s);
			} catch (Exception ignored) {
			}
			return num;
		} else {
			return defaultV;
		}
	}

	public static long str2Long(String s, long defaultV) {
		if (s != null && !s.equals("")) {
			long num = defaultV;
			try {
				num = Long.parseLong(s);
			} catch (Exception ignored) {
			}
			return num;
		} else {
			return defaultV;
		}
	}

	public static double str2Double(String s, double defaultV) {
		if (s != null && !s.equals("")) {
			double num = defaultV;
			try {
				num = Double.parseDouble(s);
			} catch (Exception ignored) {
			}
			return num;
		} else {
			return defaultV;
		}
	}

	public static float str2Float(String s, float defaultV) {
		if (s != null && !s.equals("")) {
			float num = defaultV;
			try {
				num = Float.parseFloat(s);
			} catch (Exception ignored) {
			}
			return num;
		} else {
			return defaultV;
		}
	}

	public static String valueOf(Long lon, String defValue) {
		if (lon == null) {
			return defValue;
		}

		String result = defValue;
		try {
			result = String.valueOf(lon);
		} catch (Exception e) {
			result = defValue;
		}

		return result;
	}

	public static String valueOf(Integer in, String defValue) {
		if (in == null) {
			return defValue;
		}

		String result = defValue;
		try {
			result = String.valueOf(in);
		} catch (Exception e) {
			result = defValue;
		}

		return result;
	}


}
