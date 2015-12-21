package com.gkzxhn.gkprison.activity;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.zcw.togglebutton.ToggleButton;

public class SettingActivity extends BaseActivity {

    private ToggleButton tb_msg_remind;
    private ToggleButton tb_clock_remind;
    private ToggleButton tb_pwd_set;
    private RelativeLayout rl_version_update;
    private TextView tv_agreement;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_setting, null);
        tb_msg_remind = (ToggleButton) view.findViewById(R.id.tb_msg_remind);
        tb_clock_remind = (ToggleButton) view.findViewById(R.id.tb_clock_remind);
        tb_pwd_set = (ToggleButton) view.findViewById(R.id.tb_pwd_set);
        rl_version_update = (RelativeLayout) view.findViewById(R.id.rl_version_update);
        tv_agreement = (TextView) view.findViewById(R.id.tv_agreement);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("设置");
        setBackVisibility(View.VISIBLE);
        tb_msg_remind.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    showToastMsgShort("短信提醒已开启");
                } else {
                    showToastMsgShort("短信提醒已关闭");
                }
            }
        });
        tb_clock_remind.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if(on){
                    showToastMsgShort("闹钟提醒已开启");
                }else {
                    showToastMsgShort("闹钟提醒已关闭");
                }
            }
        });
        tb_pwd_set.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if(on){
                    showToastMsgShort("独立密码设置已开启");
                }else {
                    showToastMsgShort("独立密码设置已关闭");
                }
            }
        });
        rl_version_update.setOnClickListener(this);
        tv_agreement.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()){
            case R.id.rl_version_update:
                intent = new Intent(this, VersionUpdateActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_agreement:
                showToastMsgShort("协议...");
                break;
        }
    }
}
