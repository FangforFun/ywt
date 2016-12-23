package com.gkzxhn.gkprison.app.component;

import android.content.Context;

import com.gkzxhn.gkprison.app.MyApplication;
import com.gkzxhn.gkprison.app.module.AppModule;
import com.gkzxhn.gkprison.app.module.DBModule;
import com.gkzxhn.gkprison.base.BaseActivityNew;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Author: Huang ZN
 * Date: 2016/12/20
 * Email:943852572@qq.com
 * Description:AppComponent
 */

@Singleton
@Component(modules = {AppModule.class, DBModule.class})
public interface AppComponent {

    Context getContext();

    void inject(MyApplication mApplication);

    void inject(BaseActivityNew mBaseActivity);
}
