package com.kedacom.mvc_demo.vconf.bean;

/**
 * 音视频 召集，加入，呼叫时的保存对象，用来从通知栏进入对应页面
 * @author jsl
 * @date 2013-11-12
 */
public class SaveVconf {

	/** 会议的e164号，有可能是个人的e164号 */
	public String e164;

	/** 会议的创建类型 ---呼叫，加入，召集 */
	public int vConfType;

	/** 点对点或者多人 */
	public boolean isMackCall;

	/** 会议室名称 */
	public String vConfName;

	/** 音视频 */
	public boolean isAudio;
}
