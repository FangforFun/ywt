package com.gkzxhn.gkprison.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gkzxhn.gkprison.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_splash,null);
        return view;
    }

    @Override
    protected void initData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, AgreementActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 2000);
        setActionBarGone(View.GONE);
    }
}
