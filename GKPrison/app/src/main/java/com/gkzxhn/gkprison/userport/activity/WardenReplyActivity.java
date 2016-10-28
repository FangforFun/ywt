package com.gkzxhn.gkprison.userport.activity;

import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.keda.sky.app.PcAppStackManager;

public class WardenReplyActivity extends BaseActivity {

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(this, R.layout.activity_warden_reply, null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("监狱长信箱");
        setBackVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }

}
