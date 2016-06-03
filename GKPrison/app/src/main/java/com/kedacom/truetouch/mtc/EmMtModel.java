/**
 * @(#)EmMtModel.java 2013-10-18 Copyright 2013 it.kedacom.com, Inc. All rights
 *                    reserved.
 */

package com.kedacom.truetouch.mtc;

import com.pc.utils.StringUtils;

/**
 * 终端型号
 * @author chenjian
 * @date 2013-10-18
 */
public enum EmMtModel {
	// @formatter:off
	
			emUnknownMtModel,
			emPCMT,//桌面终端
			em8010,
			em8010A,
			em8010Aplus,//8010A+
			em8010C,
			em8010C1,//8010C1
			emIMT,
			emTS6610,//em8220A, //6610
			emTS5210,//em8220B, //5210
			emTSV5,//em8220C, //v5
			emTS3210,//em8620A, //3210 
			emTS6610E,
			emTS6210,
			em8010A_2,
			em8010A_4,
			em8010A_8,
			em7210,
			em7610,
			emTS5610,
			emTS3610,
			emTS6210E,
			emTS5810,
			emTS6810,
			emHD100L,
			emHD100,
			emHD100S,
			emHD1000,
			emWD1000,
			emH6000,	

			emIPAD,
			emIPHONE,
			emIPHONE4S ,
			emIPHONE5,
			emAndroid_Pad,
			emAndroid_Phone;
					
			// @formatter:on

	@Override
	public String toString() {
		String emMtModel = super.toString();
		try {
			String startStr = "em";
			if (!StringUtils.isNull(emMtModel) && emMtModel.startsWith(startStr)) {
				emMtModel = emMtModel.substring(emMtModel.indexOf(startStr) + startStr.length());
			}
		} catch (Exception e) {
		}

		return emMtModel;
	}

	public static EmMtModel toEmMtModel(int ordinal) {
		if (ordinal == emUnknownMtModel.ordinal()) {
			return emUnknownMtModel;
		} else if (ordinal == emPCMT.ordinal()) {
			return emPCMT;
		} else if (ordinal == em8010.ordinal()) {
			return em8010;
		} else if (ordinal == em8010A.ordinal()) {
			return em8010A;
		} else if (ordinal == em8010Aplus.ordinal()) {
			return em8010Aplus;
		} else if (ordinal == em8010C.ordinal()) {
			return em8010C;
		} else if (ordinal == em8010C1.ordinal()) {
			return em8010C1;
		} else if (ordinal == emIMT.ordinal()) {
			return emIMT;
		} else if (ordinal == emTS6610.ordinal()) {
			return emTS6610;
		} else if (ordinal == emTS5210.ordinal()) {
			return emTS5210;
		} else if (ordinal == emTSV5.ordinal()) {
			return emTSV5;
		} else if (ordinal == emTS3210.ordinal()) {
			return emTS3210;
		} else if (ordinal == emTS6610E.ordinal()) {
			return emTS6610E;
		} else if (ordinal == emTS6210.ordinal()) {
			return emTS6210;
		} else if (ordinal == em8010A_2.ordinal()) {
			return em8010A_2;
		} else if (ordinal == em8010A_4.ordinal()) {
			return em8010A_4;
		} else if (ordinal == em8010A_8.ordinal()) {
			return em8010A_8;
		} else if (ordinal == em7210.ordinal()) {
			return em7210;
		} else if (ordinal == em7610.ordinal()) {
			return em7610;
		} else if (ordinal == emTS5610.ordinal()) {
			return emTS5610;
		} else if (ordinal == emTS3610.ordinal()) {
			return emTS3610;
		} else if (ordinal == emTS6210E.ordinal()) {
			return emTS6210E;
		} else if (ordinal == emTS5810.ordinal()) {
			return emTS5810;
		} else if (ordinal == emTS6810.ordinal()) {
			return emTS6810;
		} else if (ordinal == emHD100L.ordinal()) {
			return emHD100L;
		} else if (ordinal == emHD100.ordinal()) {
			return emHD100;
		} else if (ordinal == emHD100S.ordinal()) {
			return emHD100S;
		} else if (ordinal == emHD1000.ordinal()) {
			return emHD1000;
		} else if (ordinal == emWD1000.ordinal()) {
			return emWD1000;
		} else if (ordinal == emH6000.ordinal()) {
			return emH6000;
		} else if (ordinal == emIPAD.ordinal()) {
			return emIPAD;
		} else if (ordinal == emIPHONE.ordinal()) {
			return emIPHONE;
		} else if (ordinal == emIPHONE4S.ordinal()) {
			return emIPHONE4S;
		} else if (ordinal == emIPHONE5.ordinal()) {
			return emIPHONE5;
		} else if (ordinal == emAndroid_Pad.ordinal()) {
			return emAndroid_Pad;
		} else if (ordinal == emAndroid_Phone.ordinal()) {
			return emAndroid_Phone;
		}

		return emUnknownMtModel;
	}
}
