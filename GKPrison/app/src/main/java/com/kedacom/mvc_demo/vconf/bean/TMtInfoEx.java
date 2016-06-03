/**
 * @(#)TerListExInform.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc. All
 *                          rights reserved.
 * 
 *                          <pre>
 * typedef struct tagTMtInfoEx
 * {
 * 	TMtId	tLabel;
 * 	s8		achAlias[MT_MAX_NAME_LEN+1];		    //终端别名
 * 	u8		byType;									// 终端类型
 * 	s8		achE164[MT_MAX_E164NUM_LEN+1];			//终端E164号
 * 	u8		byNone;									// 预留
 * public:
 * 	tagTMtInfoEx(){memset( this ,0 ,sizeof( struct tagTMtInfoEx) );}
 * }TMtInfoEx ,*PTMtInfoEx;
 * </pre>
 */

package com.kedacom.mvc_demo.vconf.bean;


/**
 * @author chenjian
 * @date 2013-8-9
 */

public class TMtInfoEx {

	public MtId mLabel;

	// 终端别名
	public String mAlias;

	// 终端类型
	public String mType;

	// 终端E164号
	public String mE164;

	// 预留
	public String mNone;

	@Override
	public boolean equals(Object o) {
		if (null == o) {
			return false;
		}

		TMtInfoEx tMtInfo = (TMtInfoEx) o;
		return mLabel.equals(tMtInfo.mLabel);
	}
}
