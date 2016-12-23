package com.gkzxhn.gkprison.userport.ui.splash;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.InputPasswordActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.ui.login.LoginActivity;
import com.gkzxhn.gkprison.userport.ui.splash.welcome.WelComeActivity;
import com.gkzxhn.gkprison.utils.SystemUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author: Huang ZN
 * Date: 2016/12/21
 * Email:943852572@qq.com
 * Description:启动页面
 */
public class SplashActivity extends BaseActivityNew implements SplashContract.View{

    @BindView(R.id.tv_version)
    TextView tv_version_name;
    @BindView(R.id.splash)
    LinearLayout splash;

    @Inject SplashPresenter mPresenter;

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        mPresenter.attachView(this);
        mPresenter.initDB();
        mPresenter.next();
    }

    @Override
    public void showMainUi() {
        String app_version_info = getString(R.string.app_v)
                + SystemUtil.getVersionName(this);
        tv_version_name.setText(app_version_info);
        setUpBackGroundResources();
    }

    @Override
    public void toWelCome() {
        WelComeActivity.startActivity(this);
        finish();
    }

    @Override
    public void toInputPassWord() {
        InputPasswordActivity.startActivity(this);
        finish();
    }

    @Override
    public void toLogin() {
        LoginActivity.startActivity(this);
        finish();
    }

    @Override
    public void toMain() {
        MainActivity.startActivity(this);
        finish();
    }

    @Override
    public void toDateMeetingList() {
        DateMeetingListActivity.startActivity(this);
        finish();
    }

    private void setUpBackGroundResources() {
        if (SystemUtil.isTablet(this)){
            splash.setBackgroundResource(R.drawable.splash_tablet);
        }
    }

    @Override
    protected void initInjector() {
        DaggerSplashComponent.builder()
                .appComponent(getAppComponent())
                .activityModule(getActivityModule())
                .splashModule(new SplashModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return false;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @Override
    public int setLayoutResId() {
        return R.layout.activity_splash_new;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
