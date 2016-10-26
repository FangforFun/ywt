package com.keda.sky.app;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.keda.callback.MyMtcCallback;
import com.kedacom.kdv.mt.api.Base;
import com.kedacom.kdv.mt.api.Configure;
import com.kedacom.kdv.mt.bean.TMtH323PxyCfg;
import com.kedacom.kdv.mt.bean.TagTNetUsedInfoApi;
import com.kedacom.kdv.mt.constant.EmMtModel;
import com.kedacom.kdv.mt.constant.EmNetAdapterWorkType;
import com.kedacom.truetouch.audio.AudioDeviceAndroid;
import com.pc.utils.FormatTransfer;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.StringUtils;
import com.pc.utils.VConfStaticPic;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * TrueTouch Application 
 * 
 * @author chenj
 * @date 2014年10月24日
 */
public class SkyDemoApplication extends Application {

	public final static String ID = "id";
	public final static String JID = "jid";
	public final static String NAME = "name";
	public final static String IPAddr = "ipAddr";
	public final static String ALIAS = "alias";
	public final static String E164NUM = "e164Num";
	public final static String USER_NAME = "username";
	public final static String RESULT = "result";

	public boolean isH323;

	public static SkyDemoApplication mOurApplication;

	public static Context getContext() {
		return mOurApplication.getApplicationContext();
	}

	public static SkyDemoApplication getApplication() {
		return (SkyDemoApplication) mOurApplication;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mOurApplication = this;
		Log.i(SkyDemoApplication.class.getSimpleName(), "onCreate..." + android.os.Build.MODEL + "  package:" + getPackageName());
		Base.mtStart(EmMtModel.emSkyAndroidPhone, TruetouchGlobal.MTINFO_SKYWALKER, "5.0", getMediaLibDir()
				+ File.separator, MyMtcCallback.getInstance(), "kedacom"); // 启动业务终端，开始接受回调
		new Thread(new Runnable() {

			@Override
			public void run() {
				parseH323();
				// 设音视频上下文置
				AudioDeviceAndroid.initialize(getContext());
				setUserdNetInfo();
				// 启动Service
				Base.initService();
				Log.w("Test", "开始终端服务 SYSStartService: agent/misc/mtmp/rest/upgrade/im/mtpa");
				VConfStaticPic.checkStaticPic(SkyDemoApplication.getContext(), getTempDir() + File.separator);
			}
		}).start();
	}

	public void parseH323() {
		// 从数据库获取当前 是否注册了代理
		StringBuffer H323PxyStringBuf = new StringBuffer();
		Configure.getH323PxyCfg(H323PxyStringBuf);
		String h323Pxy = H323PxyStringBuf.toString();
		TMtH323PxyCfg tmtH323Pxy = new Gson().fromJson(h323Pxy, TMtH323PxyCfg.class);
		// { "achNumber" : "", "achPassword" : "", "bEnable" : true, "dwSrvIp" : 1917977712, "dwSrvPort" : 2776 }
		if (null != tmtH323Pxy) {
			isH323 = tmtH323Pxy.bEnable;
		}
	}

	public String getMediaLibDir() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/mediaLib" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.getAbsolutePath();
	}

	public String getTempDir() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/mediaLib/temp" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.getAbsolutePath();
	}

	public static String getTmpDir() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/.tmp" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		return dir.getAbsolutePath();
	}

	// 保存截图的路径(绝对路径)
	public static String getPictureDir() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/.picture" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		return dir.getAbsolutePath();
	}

	// 图片保存文件夹绝对路径
	public static String getSaveDir() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/save" + File.separator);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		return dir.getAbsolutePath();
	}

	/**
	 * 设置正在使用的网络信息
	 */
	public void setUserdNetInfo() {
		String ip = NetWorkUtils.getIpAddr(SkyDemoApplication.getContext(), true);

		TagTNetUsedInfoApi netInfo = new TagTNetUsedInfoApi();
		netInfo.emUsedType = EmNetAdapterWorkType.emNetAdapterWorkType_Wifi_Api;
		// netInfo.dwIp = NetWorkUtils.getFirstWiFiIpAddres(TruetouchApplication.getContext());
		try {
			netInfo.dwIp = FormatTransfer.lBytesToLong(InetAddress.getByName(ip).getAddress());
		} catch (Exception e) {
			netInfo.dwIp = FormatTransfer.reverseInt((int) NetWorkUtils.ip2int(ip));
		}
		if (NetWorkUtils.isMobile(SkyDemoApplication.getContext())) {
			netInfo.emUsedType = EmNetAdapterWorkType.emNetAdapterWorkType_MobileData_Api;
		}
		String dns = NetWorkUtils.getDns(SkyDemoApplication.getContext());
		try {
			if (!StringUtils.isNull(dns)) {
				netInfo.dwDns = FormatTransfer.lBytesToLong(InetAddress.getByName(dns).getAddress());
			} else {
				netInfo.dwDns = 0;
			}
		} catch (UnknownHostException e) {
			Log.e("Test", "dwDns: " + dns + "--" + netInfo.dwDns);
		}

		Log.e("Test", "ip: " + ip + "--" + netInfo.dwIp);

		Configure.sendUsedNetInfoNtf(netInfo);
	}
}
