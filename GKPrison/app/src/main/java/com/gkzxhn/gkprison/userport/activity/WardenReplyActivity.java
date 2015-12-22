package com.gkzxhn.gkprison.userport.activity;

import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

public class WardenReplyActivity extends BaseActivity {

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_warden_reply, null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("监狱长信箱");
        setBackVisibility(View.VISIBLE);
    }
}
