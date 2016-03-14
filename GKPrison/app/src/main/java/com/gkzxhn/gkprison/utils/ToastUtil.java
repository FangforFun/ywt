package com.gkzxhn.gkprison.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by zhengneng on 2016/3/13.
 * Toast统一管理类
 */
public class ToastUtil {

    public static boolean isShowToast = true;// 是否弹土司

    private ToastUtil(){

        /**
         * cannot be instantiated
         */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 短时间显示Toast  (3s)
     * @param context
     * @param msg
     */
    public static void showShortToast(Context context, CharSequence msg){
        if(isShowToast){
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 短时间显示Toast
     * @param context
     * @param msg
     */
    public static void showShortToast(Context context, int msg){
        if(isShowToast){
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 长时间显示Toast (5s)
     * @param context
     * @param msg
     */
    public static void showLongToast(Context context, CharSequence msg){
        if(isShowToast){
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 长时间显示Toast (5s)
     * @param context
     * @param msg
     */
    public static void showLongToast(Context context, int msg){
        if(isShowToast){
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 自定义Toast显示时常
     * @param context
     * @param msg
     * @param duration
     */
    public static void showToastDivTime(Context context, CharSequence msg, int duration){
        if(isShowToast){
            Toast.makeText(context, msg, duration).show();
        }
    }

    /**
     * 自定义Toast显示时常
     * @param context
     * @param msg
     * @param duration
     */
    public static void showToastDivTime(Context context, int msg, int duration){
        if(isShowToast){
            Toast.makeText(context, msg, duration).show();
        }
    }
}
