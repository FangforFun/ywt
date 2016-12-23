package com.gkzxhn.gkprison.app.module;

import android.app.Activity;

import com.gkzxhn.gkprison.app.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Author: Huang ZN
 * Date: 2016/12/21
 * Email:943852572@qq.com
 * Description:
 */

@Module
public class ActivityModule {

    private final Activity mActivity;

    public ActivityModule(Activity activity){
        this.mActivity = activity;
    }

    @Provides
    @PerActivity
    public Activity provideActivity(){
        return mActivity;
    }
}
