/**
 * @(#)ConfMeeting.java 2013-6-13 Copyright 2013 it.kedacom.com, Inc. All rights
 *                      reserved.
 */

package com.kedacom.mvc_demo.conf.bean;

import com.pc.utils.StringUtils;
import com.kedacom.mvc_demo.vconf.bean.EmConfListType;

/**
 * 视频会议(Video Conference)
 * @author chenjian
 * @date 2013-6-13
 */

public class VConf   {

	private int _id;
	private String confId; // 会议ID
	private String confName; // 会议名
	private String confE164; // E164号码
	private String mtNum; // 入会终端数
	private String remainTime; // 会议剩余时间
	private boolean needPsw; // 是否需要密码：0 - 不需要，1 - 需要
	private int confModeEx; // 会议模式：0-高清、1-标清、2-流畅、3-自定义
	/**
	 * <pre>
	 * 	 enum EmConfListType
	 * 	 {
	 * 	 emConfListType_Hold = 0, //正在召开
	 * 	 emConfListType_Subscribe, //预约
	 * 	 emConfListType_Idle, //空闲
	 * 	 emConfListType_All, //全部
	 * 	 emConfListTypeEnd
	 * 	 };
	 * </pre>
	 */
	private int confListType; // 会议列表类型
	private String startTime; // 会议开始时间
	private int callRate; // 会议码率
	private String confSpcl;

	public int getId() {
		return _id;
	}

	public void setId(int _id) {
		this._id = _id;
	}

	public String getConfId() {
		return confId;
	}

	public void setConfId(String confId) {
		this.confId = confId;
	}

	public String getConfName() {
		return confName;
	}

	public void setConfName(String confName) {
		this.confName = confName;
	}

	public String getConfE164() {
		return confE164;
	}

	/**
	 * 截取E164号后六位
	 * @return
	 */
	public String getSingleConfE164() {
		String split = "#";

		if (StringUtils.isNull(confE164) || !confE164.contains(split)) {
			return confE164;
		}

		int index = confE164.indexOf(split);
		String sigleConfE164 = confE164;
		try {
			sigleConfE164 = confE164.substring(index + 1);
		} catch (Exception e) {
		}

		return sigleConfE164;
	}

	public void setConfE164(String confE164) {
		this.confE164 = confE164;
	}

	public int getVConfMtNum() {
		return StringUtils.str2Int(mtNum, 0);
	}

	public String getMtNum() {
		return mtNum;
	}

	public void setMtNum(String mtNum) {
		this.mtNum = mtNum;
	}

	public String getRemainTime() {
		return remainTime;
	}

	public void setRemainTime(String remainTime) {
		this.remainTime = remainTime;
	}

	public boolean isNeedPsw() {
		return needPsw;
	}

	public void setNeedPsw(boolean needPsw) {
		this.needPsw = needPsw;
	}

	public int getConfModeEx() {
		return confModeEx;
	}

	public void setConfModeEx(int confModeEx) {
		this.confModeEx = confModeEx;
	}

	public int getConfListType() {
		return confListType;
	}

	public void setConfListType(int confListType) {
		this.confListType = confListType;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public int getCallRate() {
		return callRate;
	}

	public void setCallRate(int callRate) {
		this.callRate = callRate;
	}

	/** @return the confSpcl */
	public String getConfSpcl() {
		return confSpcl;
	}

	/**
	 * @param confSpcl the confSpcl to set
	 */
	public void setConfSpcl(String confSpcl) {
		this.confSpcl = confSpcl;
	}

	public void setTemplate() {
		setConfListType(EmConfListType.emConfListType_Idle.ordinal());
	}

	public void setBook() {
		setConfListType(EmConfListType.emConfListType_Subscribe.ordinal());
	}

	public void setConvene() {
		setConfListType(EmConfListType.emConfListType_Hold.ordinal());
	}

	/**
	 * 空闲会议
	 * @return
	 */
	public boolean isTemplate() {
		return confListType == EmConfListType.emConfListType_Idle.ordinal();
	}

	/**
	 * 预定会议
	 * @return
	 */
	public boolean isBook() {
		return confListType == EmConfListType.emConfListType_Subscribe.ordinal();
	}

	/**
	 * 会议中
	 * @return
	 */
	public boolean isConvene() {
		return confListType == EmConfListType.emConfListType_Hold.ordinal();
	}

//	/**
//	 * 会议类型对应的资源
//	 * @return
//	 */
//	public int getTypeResouceId() {
//		if (isConvene()) {
//			return R.drawable.vconf_video_ing;
//		} else if (isBook()) {
//			return R.drawable.video_book;
//		} else if (isTemplate()) {
//			return R.drawable.video_free;
//		}
//
//		return R.drawable.video_free;
//	}


	/**
	 * 比较是否是同一会议
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			VConf vconf = (VConf) obj;
			if (vconf == null || StringUtils.isNull(vconf.confId)) {
				return false;
			}
			if (vconf.confId.equals(this.confId)) {
				return true;
			}

		} catch (Exception e) {
		}

		return false;
	}

}
