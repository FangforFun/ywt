package com.pc.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class TerminalUtils {


	public static int terminalDensityDpi(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		return dm.densityDpi;
	}

	/**
	 * 终端设备的WH
	 * @Description
	 * @return
	 */
	public static int[] terminalWH(Context context) {
		int[] wh = new int[2];

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		wh[0] = dm.widthPixels;
		wh[1] = dm.heightPixels;

		// int orientation =
		// KTruetouchApplication.mOurApplication.getResources().getConfiguration().orientation;
		// if (orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
		// } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
		// }

		return wh;
	}

	/**
	 * 终端设备的H
	 * @Description
	 * @return
	 */
	public static int terminalH(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		return dm.heightPixels;
	}

	/**
	 * 终端设备的W
	 * @Description
	 * @return
	 */
	public static int terminalW(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		return dm.widthPixels;
	}

	public static float computeScale4DensityDpi(Context context, int dpi) {
		return ((float) context.getResources().getDisplayMetrics().densityDpi) / dpi;
	}

	/**
	 * 状态栏的高度
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		int statusBarHeight = 0;
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return statusBarHeight;
	}

	/**
	 * 是否自动旋转
	 * @param context
	 * @return
	 */
	public static boolean isAutoRotate(Context context) {
		if (context == null) return false;

		return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
	}

	/**
	 * 检测屏幕方向
	 */
	public static int getScreenOrientation(Context context) {
		Configuration conf = context.getResources().getConfiguration();
		return conf.orientation;
	}

	/**
	 * 是否是横屏
	 * @param context
	 * @return
	 */
	public static boolean isOrientationLandscape(Context context) {
		Configuration conf = context.getResources().getConfiguration();
		if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 屏幕方向
	 * @param context
	 * @return
	 */
	public static int getRotation(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		int rotate = 0;
		switch (d.getRotation()) {
			case Surface.ROTATION_0: // 手机处于正常状态
				rotate = 0;
				break;

			case Surface.ROTATION_90:// 手机旋转90度
				rotate = 1;
				break;

			case Surface.ROTATION_180:// 手机旋转180度
				rotate = 2;
				break;

			case Surface.ROTATION_270:// 手机旋转270度
				rotate = 3;
				break;

			default:
				break;
		}

		return rotate;
	}

	/**
	 * 屏幕方向角度
	 * @param context
	 * @return
	 */
	public static int getRotationAngle(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		int angle = 0;
		switch (d.getRotation()) {
			case Surface.ROTATION_0: // 手机处于正常状态
				angle = 0;
				break;

			case Surface.ROTATION_90:// 手机旋转90度
				angle = 90;
				break;

			case Surface.ROTATION_180:// 手机旋转180度
				angle = 180;
				break;

			case Surface.ROTATION_270:// 手机旋转270度
				angle = 270;
				break;

			default:
				break;
		}

		return angle;
	}

	public Rect getScreenBoundRect(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		Rect rect = new Rect(0, 0, dm.widthPixels, dm.heightPixels);

		return rect;
	}

	/**
	 * 电话号码
	 * @param context
	 * @return
	 */
	public static String getPhoneNumber(Context context) {
		if (context == null) return "";

		TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		return telephonyMgr.getLine1Number();
	}

	/**
	 * sim是否可用
	 */
	public static boolean isSimEnable(Context context) {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (TelephonyManager.SIM_STATE_ABSENT == mTelephonyMgr.getSimState()) {// sim卡不可用
			return false;
		} else {// sim卡可用
			return true;
		}
	}

	/**
	 * 电话状态
	 * @Description
	 * @param context
	 * @return CALL_STATE_IDLE 无任何状态时; CALL_STATE_OFFHOOK 接起电话时;
	 *         CALL_STATE_RINGING 电话进来时
	 */
	public static int getCallState(Context context) {
		if (context == null) return 0;

		TelephonyManager telephonMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		return telephonMgr.getCallState();
	}

	@Deprecated
	public static String telephonyInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		String str = "";
		str += "DeviceId(IMEI) = " + tm.getDeviceId() + "\n";
		str += "DeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion() + "\n";
		str += "Line1Number = " + tm.getLine1Number() + "\n";
		str += "NetworkCountryIso = " + tm.getNetworkCountryIso() + "\n";
		str += "NetworkOperator = " + tm.getNetworkOperator() + "\n";
		str += "NetworkOperatorName = " + tm.getNetworkOperatorName() + "\n";
		str += "NetworkType = " + tm.getNetworkType() + "\n";
		str += "PhoneType = " + tm.getPhoneType() + "\n";
		str += "SimCountryIso = " + tm.getSimCountryIso() + "\n";
		str += "SimOperator = " + tm.getSimOperator() + "\n";
		str += "SimOperatorName = " + tm.getSimOperatorName() + "\n";
		str += "SimSerialNumber = " + tm.getSimSerialNumber() + "\n";
		str += "SimState = " + tm.getSimState() + "\n";
		str += "SubscriberId(IMSI) = " + tm.getSubscriberId() + "\n";
		str += "VoiceMailNumber = " + tm.getVoiceMailNumber() + "\n";

		return str;
	}

	public static String IMSI(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		return tm.getSubscriberId();
	}

	public static String IMEI(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		return tm.getDeviceId();
	}


	/**
	 * 检查麦克风是否静音
	 * @param context
	 * @return
	 */
	public static boolean isMicrophoneMute(Context context) {
		if (null == context) {
			return false;
		}

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		return audioManager.isMicrophoneMute();
	}

	/**
	 * 检查喇叭扩音器是否开着
	 * @param context
	 * @return
	 */
	public static boolean isSpeakerphoneOn(Context context) {
		if (null == context) {
			return false;
		}

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		return audioManager.isSpeakerphoneOn();
	}

	/**
	 * 检查音频路由到有线耳机是否开着
	 * @param context
	 * @return
	 */
	public static boolean isWiredHeadsetOn(Context context) {
		if (null == context) {
			return false;
		}

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		return audioManager.isWiredHeadsetOn();
	}

	/**
	 * 设置听筒模式
	 * @param context
	 * @param useMaxVolume 使用最大音量
	 * @param forceOpenVolume 强制开启音量，当音量为0时有效
	 */
	public static void setReceiverModel(Context context, boolean useMaxVolume, boolean forceOpenVolume) {
		if (null == context) {
			return;
		}

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		// if (!audioManager.isSpeakerphoneOn()) {
		// return;
		// }

		int volumeIndex = 0;
		if (useMaxVolume) {
			volumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		} else {
			volumeIndex = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
			if (volumeIndex <= 0 && forceOpenVolume) {
				volumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
			}
		}

		// 关闭扬声器
		audioManager.setSpeakerphoneOn(false);
		audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);

		// 设置音量
		audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volumeIndex, AudioManager.STREAM_VOICE_CALL);
		audioManager.setMode(AudioManager.MODE_IN_CALL);
	}

	/**
	 * 扬声器模式
	 * @param context
	 * @param useMaxVolume 使用最大音量
	 * @param forceOpenVolume 强制开启音量，当音量为0时有效
	 */
	public static void setSpeakerphoneOn(Context context, boolean useMaxVolume, boolean forceOpenVolume) {
		if (null == context) {
			return;
		}

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.isSpeakerphoneOn()) {
			return;
		}

		int volumeIndex = 0;
		if (useMaxVolume) {
			volumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		} else {
			volumeIndex = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
			if (volumeIndex <= 0 && forceOpenVolume) {
				volumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
			}
		}

		// 打开扬声器
		audioManager.setSpeakerphoneOn(true);

		// 设置音量
		audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volumeIndex, AudioManager.STREAM_VOICE_CALL);
		audioManager.setMode(AudioManager.MODE_NORMAL);
	}


	/**
	 * 计算系统总内存
	 * @param context
	 * @return
	 */
	public static int getTotalMemory(Context context) {
		if (context == null) {
			return 0;
		}
		String str1 = "/proc/meminfo";

		String str2;

		String[] arrayOfString;

		int initial_memory = 0;

		try {

			FileReader localFileReader = new FileReader(str1);

			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");

			// for (String num : arrayOfString) {
			// // Log.i(str2, num + "\t");
			// }

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() / 1024;// 显示单位为MB

			localBufferedReader.close();

		} catch (IOException e) {

		}

		return initial_memory;

	}

	/**
	 * 返回cpu型号
	 * @return
	 */
	public static String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String cpuType = "";
		String[] cpuInfo = {
				"", ""
		}; // 1-cpu型号 //2-cpu频率
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
		}
		cpuType = cpuInfo[0];
		// Log.i("Test", "cpuinfo:" + cpuInfo[0] + " --- " + cpuInfo[1]);
		return cpuType;
	}

	/**
	 * 获取CPU序列号
	 * @Description
	 * @return CPU序列号(16位) 读取失败为"0000000000000000"
	 */
	public static String getCPUSerial() {
		String str = "";
		String strCPU = "";
		String cpuAddress = "0000000000000000";
		try {
			Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo"); // 读取CPU信息
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (int i = 1; i < 100; i++) {// 查找CPU序列号
				str = input.readLine();
				if (str != null) {
					if (str.indexOf("Serial") > -1) {// 查找到序列号所在行
						strCPU = str.substring(str.indexOf(":") + 1, str.length());// 提取序列号
						cpuAddress = strCPU.trim();// 去空格

						break;
					}
				} else {
					break;// 文件结尾
				}
			}
			input.close();
			ir.close();
		} catch (IOException ex) {
		}

		return cpuAddress;
	}

	/**
	 * 资料地址：http://hi.baidu.com/ch_ff/item/e2d74df357f59c0f85d278f9 <br>
	 * 网上方法，未验证
	 * @return
	 */
	public static short readUsage() {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String load = reader.readLine();
			String[] toks = load.split(" ");
			long idle1 = Long.parseLong(toks[5]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			Thread.sleep(360);

			reader.seek(0);
			load = reader.readLine();
			reader.close();
			toks = load.split(" ");
			long idle2 = Long.parseLong(toks[5]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
			return (short) (100 * (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)));
		} catch (IOException ex) {
		} catch (Exception e) {
		}

		return 0;
	}

	/**
	 * 获取内存使用率
	 * @param context
	 */
	public static short getMemoryUsage(Context context) {
		if (context == null) {
			return 0;
		}
		short usuge = 0;
		final ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityMgr.getMemoryInfo(memoryInfo);

		// 系统剩余内存
		final long availMem = (memoryInfo.availMem >> 10) >> 10;

		// 已使用内存
		final long usedMen = getTotalMemory(context) - availMem;

		usuge = (short) ((float) usedMen / getTotalMemory(context) * 100);
		return usuge;
	}

	/**
	 * 获取cpu的核数 <br>
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * @return The number of cores, or 1 if failed to get result
	 */
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {

			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Default to return 1 core
			return 1;
		}
	}

	/**
	 * 调整Camera焦点
	 * @param progress value range:0-100
	 * @param camera
	 */
	public static void zoomCamera(int progress, Camera camera) {
		if (camera == null || progress < 0 || progress > 100) {
			return;
		}

		Camera.Parameters params = camera.getParameters();
		boolean zoomSurpport = params.isZoomSupported();
		if (!zoomSurpport) return;

		int maxZoom = params.getMaxZoom();
		int zoom = (int) (progress / 100.0 * maxZoom);
		int currZoom = params.getZoom();
		if (currZoom == zoom) return;

		boolean mSmoothZoomSurpport = params.isSmoothZoomSupported();
		if (mSmoothZoomSurpport) {
			try {
				camera.stopSmoothZoom();
			} catch (Exception e) {
			}
			camera.startSmoothZoom(zoom);
		} else {
			params.setZoom(zoom);
			camera.setParameters(params);
		}
	}

}
