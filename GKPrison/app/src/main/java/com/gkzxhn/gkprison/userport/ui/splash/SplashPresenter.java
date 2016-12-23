package com.gkzxhn.gkprison.userport.ui.splash;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.userport.db.SQLiteHelper;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;

import javax.inject.Inject;

/**
 * Author: Huang ZN
 * Date: 2016/12/21
 * Email:943852572@qq.com
 * Description:SplashPresenter
 */
@PerActivity
public class SplashPresenter implements SplashContract.Presenter{

    private static final String TAG = SplashPresenter.class.getName();
    private SplashContract.View mSplashView;
    private Context mContext;

    @Inject
    public SplashPresenter(Context context){
        this.mContext = context;
    }

    @Override
    public void attachView(@NonNull SplashContract.View view) {
        mSplashView = view;
    }

    @Override
    public void detachView() {
        mSplashView = null;
    }

    @Override
    public void initDB() {
        SQLiteHelper.init(mContext);
    }

    @Override
    public void next() {
        mSplashView.showMainUi();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isFirst = (boolean) getSharedPres("is_first", true);
                if (!isFirst){
                    boolean isLock = (boolean) getSharedPres("isLock", false);
                    if (isLock){// 已加锁进入输入密码页面
                        mSplashView.toInputPassWord();
                        Log.i(TAG, "user will go to input password!");
                    }else {
                        String account = (String) getClearableSharedPres("username", "");
                        String password = (String) getClearableSharedPres("password", "");
                        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)){
                            mSplashView.toLogin();
                            Log.i(TAG, "user will go to login!");
                        }else {
                            boolean isCommonUser = (boolean) getClearableSharedPres("isCommonUser", true);
                            if (isCommonUser){
                                mSplashView.toMain();
                                Log.i(TAG, "isCommonUser, user will go to main!");
                            }else {
                                mSplashView.toDateMeetingList();
                                Log.i(TAG, "isNotCommonUser, user will go to DateMeetingList!");
                            }
                        }
                    }
                }else {// 第一次  进入欢迎页面
                    mSplashView.toWelCome();
                    SPUtil.putCanNotClear(mContext, "is_first", false);
                    Log.i(TAG, "new user!!!!!!!!!");
                }
            }
        }, 1000);
    }

    /**
     * 获取sp的值
     * @param key
     * @param defaultValue
     * @return
     */
    private Object getSharedPres(String key, Object defaultValue){
        return SPUtil.getCanNotClear(mContext, key, defaultValue);
    }

    /**
     * 获取sp的值
     * @param key
     * @param defaultValue
     * @return
     */
    private Object getClearableSharedPres(String key, Object defaultValue){
        return SPUtil.get(mContext, key, defaultValue);
    }
}
