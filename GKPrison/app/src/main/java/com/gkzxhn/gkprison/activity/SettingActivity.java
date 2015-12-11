package com.gkzxhn.gkprison.activity;

import android.view.View;

import com.gkzxhn.gkprison.R;
import com.zcw.togglebutton.ToggleButton;

public class SettingActivity extends BaseActivity {

    private ToggleButton tb_msg_remind;
    private ToggleButton tb_clock_remind;
    private ToggleButton tb_pwd_set;

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_setting, null);
        tb_msg_remind = (ToggleButton) view.findViewById(R.id.tb_msg_remind);
        tb_clock_remind = (ToggleButton) view.findViewById(R.id.tb_clock_remind);
        tb_pwd_set = (ToggleButton) view.findViewById(R.id.tb_pwd_set);
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
    }
}
