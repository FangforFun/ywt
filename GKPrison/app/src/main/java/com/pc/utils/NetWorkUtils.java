package com.pc.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author chenjian
 * @date 2013-6-6
 */
public class NetWorkUtils {

	/**
	 * IPV4 Address
	 * 
	 * @param context
	 * @param judeWifi
	 * @return
	 */
	public static String getIpAddr(Context context, boolean judeWifi) {
		String ipaddr = NetWorkUtils.getLocalIpAddress();

		if (!judeWifi) {
			return ipaddr;
		}

		// 小米2S(MI 2S)通过NetWorkUtils.getLocalIpAddress()获取的IP始终为：10.0.2.15
		// 对应2S这种情况,可对WiFi单点进行判断
		if (context != null && (TextUtils.isEmpty(ipaddr) || judeWifi)
				&& isWiFi(context)) {
			ipaddr = NetWorkUtils.getNormalWiFiIpAddres(context);
		}

		return ipaddr;
	}

	/**
	 * Normal IPV4 Address
	 * 
	 * @return 192.168.1.118
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}

		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}

		return "";
	}

	/**
	 * WIFI MAC Address
	 * 
	 * @param context
	 * @return 88:32:9B:90:BB:32
	 */
	public static String getLocalMacAddres(Context context) {
		if (null == context) {
			return "";
		}

		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * WIFI First IP Address
	 * 
	 * <pre>
	 * 第一个IP地址，即本机IP
	 * </pre>
	 * 
	 * @param context
	 * @return 1979820224
	 */
	public static int getFirstWiFiIpAddres(Context context) {
		if (null == context) {
			return 0;
		}

		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();

		return info.getIpAddress();
	}

	/**
	 * WIFI Normal IP Address *
	 * 
	 * <pre>
	 * 第一个IP地址，即本机IP
	 * </pre>
	 * 
	 * @param context
	 * @return 192.168.1.118
	 */
	public static String getNormalWiFiIpAddres(Context context) {
		if (null == context) {
			return "";
		}

		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		int inIP = info.getIpAddress();

		return (inIP & 0xFF) + "." + ((inIP >> 8) & 0xFF) + "."
				+ ((inIP >> 16) & 0xFF) + "." + (inIP >> 24 & 0xFF);
	}

	public static String intToIp(int intIp) {

		return (intIp & 0xFF) + "." +

		((intIp >> 8) & 0xFF) + "." +

		((intIp >> 16) & 0xFF) + "." +

		(intIp >> 24 & 0xFF);
	}

	/**
	 * 获取DNS
	 * 
	 * @param context
	 * @return
	 */
	public static String getDns(Context context) {
		if (context == null) {
			return "";
		}

		WifiManager my_wifiManager = ((WifiManager) context
				.getSystemService(Context.WIFI_SERVICE));
		if (my_wifiManager == null) {
			return null;
		}
		DhcpInfo dhcpInfo = my_wifiManager.getDhcpInfo();

		if (dhcpInfo == null) {
			return null;
		}
		String dns = intToIp(dhcpInfo.dns1);

		return dns;
	}

	/**
	 * 网络是否可用
	 * 
	 * @return
	 */
	public static boolean isAvailable(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connMgr == null) {
			return false;
		}
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		if (netInfo.isAvailable() && netInfo.isConnected()) {
			return true;
		}

		return false;
	}

	/**
	 * 网络是否可用
	 * 
	 * @param netInfo
	 * @return
	 */
	public static boolean isAvailable(NetworkInfo netInfo) {
		if (null == netInfo) {
			return false;
		}

		if (netInfo.isAvailable() && netInfo.isConnected()) {
			return true;
		}

		return false;
	}

	/**
	 * 是否连接Wifi
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWiFi(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connMgr == null) {
			return false;
		}
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		// 网络可用
		if (isAvailable(netInfo)) {
			int type = netInfo.getType(); // 网络类型
			if (ConnectivityManager.TYPE_WIFI == type) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否连接手机网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobile(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connMgr == null) {
			return false;
		}
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		// 网络可用
		if (isAvailable(netInfo)) {
			int type = netInfo.getType(); // 网络类型
			if (ConnectivityManager.TYPE_MOBILE == type) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 网络类型
	 * 
	 * @param context
	 * @return
	 */
	public static int getNetworkType(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		return netInfo.getType();
	}

	/**
	 * 网络子类型
	 * 
	 * @param context
	 * @return
	 */
	public static int getNetworkSubType(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		return netInfo.getSubtype();
	}

	/**
	 * 是否连接3G网络
	 * <p>
	 * 注意：NETWORK_TYPE_HSPA 和 NETWORK_TYPE_HSUPA 还没有定位是否为联通3G
	 * </p>
	 * 
	 * @param context
	 * @return
	 */
	public static boolean is3G(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		// 网络不可用
		if (!isAvailable(netInfo)) {
			return false;
		}

		// 非手机网络
		if (ConnectivityManager.TYPE_MOBILE != netInfo.getType()) {
			return false;
		}

		// NetworkInfo mMoble =
		// connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		int subType = netInfo.getSubtype();
		if (subType == TelephonyManager.NETWORK_TYPE_HSDPA // 联通3G
				|| subType == TelephonyManager.NETWORK_TYPE_UMTS // 联通3G
				|| subType == TelephonyManager.NETWORK_TYPE_EVDO_0 // 电信3G
				|| subType == TelephonyManager.NETWORK_TYPE_EVDO_A)// 电信3G
		{
			return true;
		}

		return false;
	}

	/**
	 * 是否为联通3G网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isUnicom3G(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		// 网络不可用
		if (!isAvailable(netInfo)) {
			return false;
		}

		// 非手机网络
		if (ConnectivityManager.TYPE_MOBILE != netInfo.getType()) {
			return false;
		}

		int subType = netInfo.getSubtype();
		if (subType == TelephonyManager.NETWORK_TYPE_HSDPA
				|| subType == TelephonyManager.NETWORK_TYPE_UMTS) { // 联通3G
			return true;
		}

		return false;
	}

	/**
	 * 是否为电信3G网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isTelecom3G(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		// 网络不可用
		if (!isAvailable(netInfo)) {
			return false;
		}

		// 非手机网络
		if (ConnectivityManager.TYPE_MOBILE != netInfo.getType()) {
			return false;
		}

		int subType = netInfo.getSubtype();
		if (subType == TelephonyManager.NETWORK_TYPE_EVDO_0
				|| subType == TelephonyManager.NETWORK_TYPE_EVDO_A) { // 电信3G
			return true;
		}

		return false;
	}

	/**
	 * 是否连接2G网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean is2G(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (null == netInfo) {
			return false;
		}

		// 网络不可用
		if (!isAvailable(netInfo)) {
			return false;
		}

		// 非手机网络
		if (ConnectivityManager.TYPE_MOBILE != netInfo.getType()) {
			return false;
		}

		int subType = netInfo.getSubtype();
		if (subType == TelephonyManager.NETWORK_TYPE_GPRS // 移动和联通2G
				|| subType == TelephonyManager.NETWORK_TYPE_CDMA // 电信2G
				|| subType == TelephonyManager.NETWORK_TYPE_EDGE) // 移动和联通2G
		{
			return true;
		}

		return false;
	}
}
