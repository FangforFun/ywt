package com.gkzxhn.gkprison.userport.ui.register;

import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.component.AppComponent;
import com.gkzxhn.gkprison.app.module.ActivityModule;
import com.gkzxhn.gkprison.app.module.ApiModule;

import dagger.Component;

/**
 * Author: Huang ZN
 * Date: 2016/12/26
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
@Component(dependencies = AppComponent.class, modules = {ActivityModule.class, ApiModule.class})
public interface RegisterComponent {

    void inject(RegisterActivity activity);

}
