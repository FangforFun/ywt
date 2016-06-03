/**
 * @(#)CallPeerE164Num.java 2013-7-30 Copyright 2013 it.kedacom.com, Inc. All
 *                          rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

import com.pc.utils.StringUtils;

/**
 * @author chenjian
 * @date 2013-7-30
 */

public class CallPeerE164Num
{

	private String result;
	private String peerE164Num;

	/** @return the result */
	public boolean isResult() {
		return StringUtils.equals(result, "1");
	}

	/**
	 * @param result
	 *        the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/** @return the peerE164Num */
	public String getPeerE164Num() {
		return peerE164Num;
	}

	/**
	 * @param peerE164Num
	 *        the peerE164Num to set
	 */
	public void setPeerE164Num(String peerE164Num) {
		this.peerE164Num = peerE164Num;
	}

//	public static CallPeerE164Num xml2Object(BaseCallbackHandler parseHandler) {
//		if (parseHandler == null) {
//			return null;
//		}
//
//		CallPeerE164Num callPeerE164Num = new CallPeerE164Num();
//
//		// 通过实例获得类名
//		Class<? extends Object> callPeerE164NumClass = callPeerE164Num.getClass();
//
//		Method[] methods = callPeerE164NumClass.getMethods();
//		if (methods == null || methods.length == 0) {
//			return null;
//		}
//
//		for (int i = 0; i < methods.length; i++) {
//			Method method = methods[i];
//			if (method == null)
//				continue;
//			String name = method.getName();
//			if (null == name || name.length() == 0)
//				continue;
//			if (!name.startsWith("set"))
//				continue;
//			try {
//				// 属性
//				String declare = name.replace("set", "");
//				Object[] args = new Object[] { parseHandler.getSingleObject(declare) };
//
//				method.invoke(callPeerE164Num, args);
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return callPeerE164Num;
//	}
}
