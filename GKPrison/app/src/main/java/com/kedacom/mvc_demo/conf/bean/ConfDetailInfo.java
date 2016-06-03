/**
 * @(#)ConfDetailInfo.java 2013-11-26 Copyright 2013 it.kedacom.com, Inc. All
 *                         rights reserved.
 */

package com.kedacom.mvc_demo.conf.bean;

/**
 * @author chenjian
 * @date 2013-11-26
 */

public class ConfDetailInfo {

	private int _id;
	private String confName;
	private String confE164;
	private String domainGUID;
	private String domainName;
	private String domainMOID;

	// 1-召开中会议，2-空闲会议, 3-预约会议, 4-模板
	private int confStyle;

	// 短号
	private String shortNum;

	// 会议加密模式: 0-不加密, 1-des加密,2-aes加密
	private int encryptMode;
	private int bitRate;

	// 会议模式：0-高清、1-标清、2-流畅、3-自定义
	private int confMode;

	// 分辨率 VIDEO_FORMAT_AUTO, ...
	private String resolution;

	// 开放模式：1-根据密码加入 2-完全开放
	private int openMode;

	// 是否讨论会议: 0-不是讨论会议(演讲会议) 1-讨论会议
	private int discussConf;
	private String startTime;

	// 持续时间(分钟)
	private int duration;

	// 创会人别名字符串
	private String alias;

	// 会议的双流发起方式: 0-发言人 1-任意终端
	private int dualMode;

	// 是否卫星会议
	private int confSpcl;

	public int getId() {
		return _id;
	}

	public void setId(int _id) {
		this._id = _id;
	}

	/** @return the confName */
	public String getConfName() {
		return confName;
	}

	/** @param confName the confName to set */
	public void setConfName(String confName) {
		this.confName = confName;
	}

	/** @return the confE164 */
	public String getConfE164() {
		return confE164;
	}

	/** @param confE164 the confE164 to set */
	public void setConfE164(String confE164) {
		this.confE164 = confE164;
	}

	/** @return the domainGUID */
	public String getDomainGUID() {
		return domainGUID;
	}

	/** @param domainGUID the domainGUID to set */
	public void setDomainGUID(String domainGUID) {
		this.domainGUID = domainGUID;
	}

	/** @return the domainName */
	public String getDomainName() {
		return domainName;
	}

	/** @param domainName the domainName to set */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/** @return the domainMOID */
	public String getDomainMOID() {
		return domainMOID;
	}

	/** @param domainMOID the domainMOID to set */
	public void setDomainMOID(String domainMOID) {
		this.domainMOID = domainMOID;
	}

	/** @return the confStyle */
	public int getConfStyle() {
		return confStyle;
	}

	/** @param confStyle the confStyle to set */
	public void setConfStyle(int confStyle) {
		this.confStyle = confStyle;
	}

	/** @return the shortNum */
	public String getShortNum() {
		return shortNum;
	}

	/** @param shortNum the shortNum to set */
	public void setShortNum(String shortNum) {
		this.shortNum = shortNum;
	}

	/** @return the encryptMode */
	public int getEncryptMode() {
		return encryptMode;
	}

	/** @param encryptMode the encryptMode to set */
	public void setEncryptMode(int encryptMode) {
		this.encryptMode = encryptMode;
	}

	/** @return the bitRate */
	public int getBitRate() {
		return bitRate;
	}

	/** @param bitRate the bitRate to set */
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	/** @return the confMode */
	public int getConfMode() {
		return confMode;
	}

	/** @param confMode the confMode to set */
	public void setConfMode(int confMode) {
		this.confMode = confMode;
	}

	/** @return the resolution */
	public String getResolution() {
		return resolution;
	}

	/** @param resolution the resolution to set */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	/** @return the openMode */
	public int getOpenMode() {
		return openMode;
	}

	/** @param openMode the openMode to set */
	public void setOpenMode(int openMode) {
		this.openMode = openMode;
	}

	/** @return the discussConf */
	public int getDiscussConf() {
		return discussConf;
	}

	/** @param discussConf the discussConf to set */
	public void setDiscussConf(int discussConf) {
		this.discussConf = discussConf;
	}

	/** @return the startTime */
	public String getStartTime() {
		return startTime;
	}

	/** @param startTime the startTime to set */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/** @return the duration */
	public int getDuration() {
		return duration;
	}

	/** @param duration the duration to set */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/** @return the alias */
	public String getAlias() {
		return alias;
	}

	/** @param alias the alias to set */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/** @return the dualMode */
	public int getDualMode() {
		return dualMode;
	}

	/** @param dualMode the dualMode to set */
	public void setDualMode(int dualMode) {
		this.dualMode = dualMode;
	}

	/** @return the confSpcl */
	public int getConfSpcl() {
		return confSpcl;
	}

	/** @param confSpcl the confSpcl to set */
	public void setConfSpcl(int confSpcl) {
		this.confSpcl = confSpcl;
	}

}
