package com.gkzxhn.gkprison.login;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Utils;

/**
 * 登录页面
 */
public class LoadingActivity extends BaseActivity {
    private String token;
    private PersonLoadingFragment personLoadingFragment;
    private PrisonLoadingFragment prisonLoadingFragment;
    private PopupWindow popupWindow;

    @Override
    protected View initView() {
        View view = View.inflate(this,R.layout.activity_loading,null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("登录");
        setRegistVisiblity(View.VISIBLE);
        Bundle date = new Bundle();
        date.putString("token",token);
        personLoadingFragment = new PersonLoadingFragment();
        personLoadingFragment.setArguments(date);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoadingFragment).commit();
        rl_regist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.rl_regist:
                if(popupWindow != null && popupWindow.isShowing()){
                    dismissPopupWindow();
                    return;
                }
                View view = View.inflate(this, R.layout.work_popup_layout, null);
                TextView tv_person_login = (TextView) view.findViewById(R.id.tv_person_login);
                tv_person_login.setOnClickListener(this);
                TextView tv_prison_login = (TextView) view.findViewById(R.id.tv_prison_login);
                tv_prison_login.setOnClickListener(this);
                popupWindow = new PopupWindow(view, DensityUtil.dip2px(this, getResources().getDimension(R.dimen.login_choose_width)), DensityUtil.dip2px(this, getResources().getDimension(R.dimen.title_bar_height)*2/3));
                // 要想popup window播放动画，要添加背景
                popupWindow.setBackgroundDrawable(new ColorDrawable(
                        Color.TRANSPARENT));
                popupWindow.setOutsideTouchable(true);
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                popupWindow.showAtLocation(ly_title_bar, Gravity.NO_GRAVITY,
                        DensityUtil.getScreenWidthHeight(this)[0] - DensityUtil.dip2px(this, getResources().getDimension(R.dimen.title_bar_height)/2), DensityUtil.dip2px(this, getResources().getDimension(R.dimen.title_bar_height)/2) + Utils.getStatusHeight(this));

                AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
                aa.setDuration(500);

                ScaleAnimation sa = new ScaleAnimation(0.5f, 1.0f, 0.5f,
                        1.0f, Animation.RELATIVE_TO_SELF, 1,
                        Animation.RELATIVE_TO_SELF, 0);
                sa.setDuration(500);

                AnimationSet set = new AnimationSet(false);
                set.addAnimation(aa);
                set.addAnimation(sa);
                view.startAnimation(set);
                break;
            case R.id.tv_person_login:
                LoadingActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoadingFragment).commit();
                dismissPopupWindow();
                tv_user_type.setText("个人用户");
                break;
            case R.id.tv_prison_login:
                prisonLoadingFragment = new PrisonLoadingFragment();
                LoadingActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, prisonLoadingFragment).commit();
                dismissPopupWindow();
                tv_user_type.setText("监狱用户");
                break;
        }
    }

    /**
     * 消掉popupWindow
     */
    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }
}
