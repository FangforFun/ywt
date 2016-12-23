package com.gkzxhn.gkprison.userport.ui.splash;

import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.component.AppComponent;
import com.gkzxhn.gkprison.app.module.ActivityModule;

import dagger.Component;

/**
 * Author: Huang ZN
 * Date: 2016/12/21
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
@Component(dependencies = AppComponent.class, modules = {
        ActivityModule.class, SplashModule.class})
public interface SplashComponent {

    void inject(SplashActivity mSplashActivity);
}
