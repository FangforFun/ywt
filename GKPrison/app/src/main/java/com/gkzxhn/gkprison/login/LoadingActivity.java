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
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 登录页面
 */
public class LoadingActivity extends BaseActivity {
    private String token;
    private PersonLoadingFragment personLoadingFragment;
    private PrisonLoadingFragment prisonLoadingFragment;

    @Override
    protected View initView() {
        View view = View.inflate(this,R.layout.activity_loading,null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("登录");
        List<String> dataset = new LinkedList<>(Arrays.asList("个人用户", "监狱用户"));
        ns_login_type.attachDataSource(dataset);
        setRegistVisiblity(View.VISIBLE);
        ns_login_type.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        ns_login_type.setText("个人用户");
                        LoadingActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoadingFragment).commit();
                        break;
                    case 1:
                        ns_login_type.setText("监狱用户");
                        prisonLoadingFragment = new PrisonLoadingFragment();
                        LoadingActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, prisonLoadingFragment).commit();
                        break;
                }
            }
        });
        Bundle date = new Bundle();
        date.putString("token",token);
        personLoadingFragment = new PersonLoadingFragment();
        personLoadingFragment.setArguments(date);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoadingFragment).commit();
    }
}
