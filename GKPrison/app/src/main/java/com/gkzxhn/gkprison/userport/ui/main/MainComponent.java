package com.gkzxhn.gkprison.userport.ui.main;

import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.component.AppComponent;
import com.gkzxhn.gkprison.app.module.ActivityModule;
import com.gkzxhn.gkprison.app.module.ApiModule;

import dagger.Component;

/**
 * Author: Huang ZN
 * Date: 2016/12/28
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
@Component(dependencies = AppComponent.class, modules = {ActivityModule.class, ApiModule.class})
public interface MainComponent {

    void inject(MainActivity activity);

}
