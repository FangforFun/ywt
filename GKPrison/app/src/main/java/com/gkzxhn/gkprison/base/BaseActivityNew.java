package com.gkzxhn.gkprison.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.gkzxhn.gkprison.app.MyApplication;
import com.gkzxhn.gkprison.app.component.AppComponent;
import com.gkzxhn.gkprison.app.module.ActivityModule;
import com.gkzxhn.gkprison.utils.ResourceUtil;
import com.gkzxhn.gkprison.utils.StatusBarUtil;
import com.keda.sky.app.PcAppStackManager;

/**
 * Author: Huang ZN
 * Date: 2016/12/21
 * Email:943852572@qq.com
 * Description:更换架构后的base activity
 */
public abstract class BaseActivityNew extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(setLayoutResId());
        PcAppStackManager.Instance().pushActivity(this);
        setTranslucentStatus(isApplyTranslucentStatus());
        setStatusBarColor(isApplyStatusBarColor());
        initInjector();
        initUiAndListener();
    }

    /**
     * 设置状态栏颜色
     * @param on
     */
    private void setStatusBarColor(boolean on) {
        if (on)
            StatusBarUtil.setColor(this,
                    ResourceUtil.getThemeColor(this), 0);
    }

    /**
     * 设置布局资源id
     * @return
     */
    public abstract int setLayoutResId();

    /**
     * 初始化ui及接口
     */
    protected abstract void initUiAndListener();

    /**
     * 初始化注入器
     */
    protected abstract void initInjector();

    /**
     * 是否开启自定义状态栏颜色
     * @return
     */
    protected abstract boolean isApplyStatusBarColor();

    /**
     * 释放设置透明状态栏
     * @return
     */
    protected abstract boolean isApplyTranslucentStatus();

    /**
     * 获取AppComponent
     * @return
     */
    protected AppComponent getAppComponent(){
        return ((MyApplication)getApplication()).getAppComponent();
    }

    /**
     * 获取activityModule
     * @return
     */
    protected ActivityModule getActivityModule(){
        return new ActivityModule(this);
    }

    /**
     * 设置透明状态栏
     * @param on
     */
    protected void setTranslucentStatus(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * 重新加载当前activity
     */
    public void reload(){
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    /**
     * 获取状态栏高度
     *  供activity子类快捷调用
     * @return
     */
    public int getStatusBarHeight(){
        return ResourceUtil.getStatusBarHeight(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PcAppStackManager.Instance().popActivity(this);
    }
}