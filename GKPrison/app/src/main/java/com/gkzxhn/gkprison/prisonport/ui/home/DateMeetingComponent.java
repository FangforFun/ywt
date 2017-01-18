package com.gkzxhn.gkprison.prisonport.ui.home;

import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.component.AppComponent;
import com.gkzxhn.gkprison.app.module.ActivityModule;
import com.gkzxhn.gkprison.app.module.ApiModule;

import dagger.Component;

/**
 * Author: Huang ZN
 * Date: 2017/1/17
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
@Component(dependencies = AppComponent.class, modules = {ActivityModule.class, ApiModule.class})
public interface DateMeetingComponent {

    void inject(DateMeetingListActivity activity);

}
