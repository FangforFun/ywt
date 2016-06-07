package com.kedacom.mvc_demo.login;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.pc.utils.DNSParseUtil;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.ValidateUtils;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.truetouch.mtc.EmMtAddrType;

/**
 * @author chenjian
 */
public class LoginFlowService {

	// 登录帐号(E164号)
	private static String mE164;
	private static String mAlias;
	// 登录密码
	private static String mPszPassword;

	// 服务器地址
	private static String mServerAddr;

	// 第一次注册GK
	public static boolean mIsFirstLogin = true;

	// 正在注册GK
	public static boolean mRegisteringGK;
	// gk注册是否已经成功
	public volatile static boolean mRegisterGK;

	static {
		mRegisteringGK = false;
		mRegisterGK = false;
	}

	private LoginFlowService() {
	}

	/**
	 * 准备GK注册，存储一些登录信息(如：帐号、密码等)
	 * 
	 * @param gksvrIp
	 * @param e164
	 * @param alias
	 * @param pwd
	 *            明文密码
	 */
	public static void prepareRegGk(String gksvrIp, String e164, String alias, String pwd) {
		if (TextUtils.isEmpty(gksvrIp)) {
			return;
		}

		Log.i("Login flow", "prepare Reg Gk, gksvrIp: " + gksvrIp);

		if (pwd == null) {
			pwd = "";
		}

		if (e164 == null) {
			e164 = "";
		}

		if (alias == null) {
			alias = "";
		}

		mE164 = e164;
		mAlias = alias;
		mPszPassword = pwd;
		mServerAddr = gksvrIp;
		if (TextUtils.isEmpty(mAlias)) {
			mAlias = e164 + System.currentTimeMillis();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				String serverIP = ValidateUtils.isIP(mServerAddr) ? mServerAddr : DNSParseUtil.dnsParse(mServerAddr);

				// 域名解析失败
				if (TextUtils.isEmpty(serverIP)) {
					parseServeraddrWrong();

					return;
				}

				mRegisteringGK = true;
				mRegisterGK = false;

				regGk(serverIP);
			}
		}).start();
	}

	/**
	 * gk注册
	 * 
	 * @param serverIP
	 */
	public static void regGk(String serverIP) {
		if (TextUtils.isEmpty(serverIP)) {
			return;
		}

		short addrType = (short) EmMtAddrType.emIPAddr.ordinal();
		if (!TextUtils.isEmpty(mE164)) {
			addrType = (short) EmMtAddrType.emE164.ordinal();
		}

		Log.i("Login flow", "Reg Gk, Ip: " + serverIP + " Alias:" + mAlias);

		String localIp = NetWorkUtils.getIpAddr(MyApplication.getApplication(), true);
		if (null == localIp) {
			localIp = "";
		}

		KdvMtBaseAPI.regGk(serverIP, addrType, mE164, mAlias, mPszPassword, false, true, localIp);
	}

	/**
	 * GK断链之后重新注册GK
	 * 
	 * @param gksvrIp
	 * @param e164
	 * @param alias
	 * @param pwd
	 *            明文密码
	 */
	public static void reRegGk() {
		if (mRegisterGK || mRegisteringGK) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.i("Login flow", "重新注册GK, Ip:" + mServerAddr + " Alias:" + mAlias);

				String serverIP = ValidateUtils.isIP(mServerAddr) ? mServerAddr : DNSParseUtil.dnsParse(mServerAddr);
				// 域名解析失败
				if (TextUtils.isEmpty(serverIP)) {
					parseServeraddrWrong();
					return;
				}

				short addrType = (short) EmMtAddrType.emIPAddr.ordinal();
				if (!TextUtils.isEmpty(mE164)) {
					addrType = (short) EmMtAddrType.emE164.ordinal();
				}

				mRegisteringGK = true;

				KdvMtBaseAPI.regGk(serverIP, addrType, mE164, mAlias, mPszPassword, false, true, "");
			}
		}).start();

	}

	/**
	 * 解析服务器地址失败
	 */
	private static void parseServeraddrWrong() {
		// 如果正在登录,需要注销
		if (mRegisteringGK) {
			KdvMtBaseAPI.unRegGk();
		}

		baseSeeting4LoginFaile();

		final Activity currActivity = AppStackManager.Instance().currentActivity();

		// 当前正处于登录界面，直接弹出提示，并回到输入状态
//		if (currActivity != null && currActivity instanceof LoginActivity) {
//			String message = currActivity.getString(R.string.parse_serveraddr_wrong);
//
//			LoginActivity lA = (LoginActivity) currActivity;
//			lA.loginFailed(message, false);
//
//			return;
//		}
	}

	/**
	 * 最后的登录成功或失败
	 * 
	 * @Description
	 * @param result
	 */
	public static void loginSuccessOrFail(final boolean result) {
		mRegisteringGK = false;
		final Activity a = AppStackManager.Instance().currentActivity();
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(result) {
					Toast.makeText(a, "注册成功了", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(a, "注册失败了", Toast.LENGTH_SHORT).show();
				}
			}
		});
//		if (result) {
//			if (a != null && a instanceof LoginActivity) {
//				((LoginActivity) a).loginSuccessed();
//			}
//		} else {
//			baseSeeting4LoginFaile();
//
//			// 当前在登录界面时，显示登录信息输入框
//			if (a != null && a instanceof LoginActivity) {
//				LoginActivity lA = (LoginActivity) a;
//				lA.loginFailed(R.string.login_failed, false);
//			}
//		}
	}

	/**
	 * 还原所有登录状态
	 */
	public static void restoreLoginState() {
		mRegisteringGK = false;
		mRegisterGK = false;
	}

	/**
	 * 登录失败之后的基本设置
	 */
	private static void baseSeeting4LoginFaile() {
		restoreLoginState();
	}

	public static Dialog mSelectorDialog;

	/** @return the mServerAddr */
	public static String getServerAddr() {
		return mServerAddr;
	}

	/** @return the mPszUsername (E164号) */
	public static String getE164() {
		return mE164;
	}

	/** @return the mAlias */
	public static String getAlias() {
		return mAlias;
	}

	/** @return the mPszPassword */
	public static String getPszPassword() {
		return mPszPassword;
	}

}
