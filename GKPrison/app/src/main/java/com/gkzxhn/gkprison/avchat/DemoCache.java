package com.gkzxhn.gkprison.avchat;

import android.content.Context;

import com.netease.nim.uikit.NimUIKit;

/**
 * Created by hzn on 2015/12/23
 */
public class DemoCache {

    private static Context context;

    private static String account;

    public static void clear() {
        account = null;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        DemoCache.account = account;
        NimUIKit.setAccount(account);
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        DemoCache.context = context.getApplicationContext();
    }
}
