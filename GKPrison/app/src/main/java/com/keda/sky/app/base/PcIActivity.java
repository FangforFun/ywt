/**
 * @(#)IBaseActivity.java 2013-9-3 Copyright 2013 it.kedacom.com, Inc. All
 *                        rights reserved.
 */

package com.keda.sky.app.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * pc Activity interface
 * 
 * @author chenjian
 * @date 2013-9-3
 */

public interface PcIActivity extends PcBaseActivity {

	/**
	 * 检测Intent是否有额外的数据
	 * 
	 * @param pExtraKey
	 * @return
	 */
	public boolean hasExtra(String pExtraKey);

	/**
	 * 返回Action
	 * 
	 * @return action
	 */
	public String getAction();

	/**
	 * 返回Bundle
	 * 
	 * @return bundle
	 */
	public Bundle getExtra();

	/**
	 * 初始化Extras
	 * 
	 * @see com.pc.app.base.PcBaseActivity#initExtras()
	 */
	@Override
	public void initExtras();

	/**
	 * 默认打开一个Activity的方式
	 * 
	 * @param pClass
	 */
	public void openActivity(Class<?> pClass);

	/**
	 * 打开一个Activity
	 * 
	 * @param pClass
	 * @param requestCode
	 */
	public void openActivity(Class<?> pClass, int requestCode);

	/**
	 * 打开一个Activity
	 * 
	 * @param pAction
	 * @param requestCode
	 */
	public void openActivity(String pAction, int requestCode);

	/**
	 * 打开一个Activity
	 * 
	 * @param pClass
	 * @param pBundle
	 */
	public void openActivity(Class<?> pClass, Bundle pBundle);

	/**
	 * 打开一个Activity
	 * 
	 * @param pAction
	 * @param pBundle
	 * @param requestCode
	 */
	public void openActivity(String pAction, Bundle pBundle, int requestCode);

	/**
	 * 打开一个Activity
	 * 
	 * @param pClass
	 * @param requestCode
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void openActivity(Class<?> pClass, int requestCode, int enterAnim, int exitAnim);

	/**
	 * 打开一个Activity
	 * 
	 * @param intent
	 * @param requestCode
	 * @param enterAnim
	 * @param exitAnim
	 */
	public void openActivity(Intent intent, int requestCode, int enterAnim, int exitAnim);

	/**
	 * 打开一个Activity
	 * 
	 * @param pClass
	 * @param pBundle
	 * @param requestCode
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void openActivity(Class<?> pClass, Bundle pBundle, int requestCode, int enterAnim, int exitAnim);

	/**
	 * 打开一个Activity
	 * 
	 * @param pClass
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void openActivity(Class<?> pClass, int enterAnim, int exitAnim);

	/**
	 * 关闭Activity
	 *
	 * @param checkSoftInput 检测软键盘
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void finish(boolean checkSoftInput, int enterAnim, int exitAnim);

	/**
	 * 关闭Activity
	 *
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void finish(int enterAnim, int exitAnim);

	/**
	 * 关闭Activity
	 *
	 * @param checkSoftInput
	 */
	public void finish(boolean checkSoftInput);

	/**
	 * 显示自定义时间的toast
	 * 
	 * @param msg
	 * @param duration
	 */
	public void show(final String pMsg, final int duration);

	/**
	 * 显示短时间的toast
	 * 
	 * @param pResId
	 */
	public void showShortToast(final int pResId);

	/**
	 * 显示长时间的toast
	 * 
	 * @param pResId
	 */
	public void showLongToast(int pResId);

	/**
	 * 显示长时间的toast
	 * @param pMsg
	 */
	public void showLongToast(final String pMsg);

	/**
	 * 显示短时间的toast
	 * 
	 * @param pMsg
	 */
	public void showShortToast(final String pMsg);

	/**
	 * 估算View
	 * 
	 * @param view
	 */
	public void measureView(View view);

}
