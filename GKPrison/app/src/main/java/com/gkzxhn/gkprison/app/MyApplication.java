package com.gkzxhn.gkprison.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.gkzxhn.gkprison.app.component.AppComponent;
import com.gkzxhn.gkprison.app.component.DaggerAppComponent;
import com.gkzxhn.gkprison.app.module.AppModule;
import com.gkzxhn.gkprison.app.utils.KDInitUtil;
import com.gkzxhn.gkprison.app.utils.NimInitUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.squareup.leakcanary.LeakCanary;

/**
 * Author: Huang ZN
 * Date: 2016/12/20
 * Email:943852572@qq.com
 * Description:application
 */
public class MyApplication extends MultiDexApplication {

    private static final String TAG = MyApplication.class.getName();
    public static MyApplication mOurApplication;// application实例
    private AppComponent mAppComponent;

    public static Context getContext() {
        return mOurApplication.getApplicationContext();
    }

    public static MyApplication getApplication() {
        return mOurApplication;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOurApplication = this;
        initComponent();// 初始化组件
        NimInitUtil.initNim();// 云信SDK相关初始化及后续操作
        KDInitUtil.init();// 科达SDK相关初始化及后续操作
        ToastUtil.registerContext(this);
        LeakCanary.install(this);
//        CrashHandler.getInstance().init(mOurApplication);
    }

    /**
     * 初始化组件
     */
    private void initComponent() {
        mAppComponent = DaggerAppComponent.builder().appModule(
                new AppModule(this)).build();
        mAppComponent.inject(this);
    }

    /**
     * 获取appComponent
     * @return
     */
    public AppComponent getAppComponent(){
        return mAppComponent == null ? null : mAppComponent;
    }
}
