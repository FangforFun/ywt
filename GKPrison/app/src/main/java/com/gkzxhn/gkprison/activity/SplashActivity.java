package com.gkzxhn.gkprison.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gkzxhn.gkprison.R;

public class SplashActivity extends BaseActivity {


    @Override
    protected View initView() {
        View view = View.inflate(mContext,R.layout.activity_splash,null);
        return view;
    }

    @Override
    protected void initData() {

    }
}
