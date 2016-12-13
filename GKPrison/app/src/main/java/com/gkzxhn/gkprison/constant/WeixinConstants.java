package com.gkzxhn.gkprison.constant;

import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * Created by zhangjia on 16/3/15.
 */
public class WeixinConstants {

    public static final String TAG = "WeixinConstants";

    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wx4973a8b575999262";

    public static class ShowMsgActivity {
        public static final String STitle = "showmsg_title";
        public static final String SMessage = "showmsg_message";
        public static final String BAThumbData = "showmsg_thumb_data";
    }

    /**
     * 获取app签名信息
     * @param params
     * @return
     */
    public static String genAppSign(List<NameValuePair> params) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        Log.d(TAG, sb.toString());
        sb.append("key=");
        sb.append("d75699d893882dea526ea05e9c7a4090");
        Log.d(TAG, sb.toString());
        String appSign = MD5Utils.ecoder(sb.toString()).toUpperCase();
        Log.d(TAG, appSign);
        return appSign;
    }
}
