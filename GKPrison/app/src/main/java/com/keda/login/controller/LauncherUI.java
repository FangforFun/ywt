/**
 * @(#)LoginUI.java 2014-9-28
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 */

package com.keda.login.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

import com.gkzxhn.gkprison.R;


/**
 * 启动界面
 *
 * @author chenj
 * @date 2014-9-28
 */

public class LauncherUI extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher_layout_tt);
    }

    /**
     * @see com.pc.app.base.PcActivity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final ImageView logoImg = (ImageView) findViewById(R.id.logo_img);
        // 进入登录界面
        logoImg.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(LauncherUI.this, LoginUI.class);
                startActivity(intent);
                finish();
            }
        }, 1 * 1000);

    }
}
