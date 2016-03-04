package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.zcw.togglebutton.ToggleButton;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity {

    private ToggleButton tb_clock_remind;
    private ToggleButton tb_pwd_set;
    private RelativeLayout rl_version_update;
    private TextView tv_agreement;
    private TextView tv_contact_us;// 联系我们
    private AlertDialog agreement_dialog;
    private RelativeLayout rl_opinion_feedback;// 意见反馈
    private SharedPreferences sp;
    private String token;
    private boolean isLock;
    private boolean isMsgRemind;
    private TextView tv_version;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_setting, null);
        tb_clock_remind = (ToggleButton) view.findViewById(R.id.tb_clock_remind);
        tb_pwd_set = (ToggleButton) view.findViewById(R.id.tb_pwd_set);
        rl_version_update = (RelativeLayout) view.findViewById(R.id.rl_version_update);
        tv_agreement = (TextView) view.findViewById(R.id.tv_agreement);
        rl_opinion_feedback = (RelativeLayout) view.findViewById(R.id.rl_opinion_feedback);
        tv_contact_us = (TextView) view.findViewById(R.id.tv_contact_us);
        tv_version = (TextView) view.findViewById(R.id.tv_version);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        token = sp.getString("token", "");
        isLock = sp.getBoolean("isLock", false);
        if(isLock){
            tb_pwd_set.setToggleOn();
        }else {
            tb_pwd_set.setToggleOff();
        }
        isMsgRemind = sp.getBoolean("isMsgRemind", false);
        if(isMsgRemind){
            tb_clock_remind.setToggleOn();
        }else {
            tb_clock_remind.setToggleOff();
        }
        setTitle("设置");
        setBackVisibility(View.VISIBLE);
        tv_version.setText("V " + SystemUtil.getVersionName(getApplicationContext()));
        tb_clock_remind.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                SharedPreferences.Editor editor = sp.edit();
                if (on) {
                    showToastMsgShort("闹钟提醒已开启");
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                    builder.setMessage("闹钟提醒已开启，如您有即将会见的档期，系统将会在会见开始前半小时以闹钟形式提醒您，请注意手机状态。");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    editor.putBoolean("isMsgRemind", true);
                } else {
                    showToastMsgShort("闹钟提醒已关闭");
                    editor.putBoolean("isMsgRemind", false);
                }
                editor.commit();
            }
        });
        tb_pwd_set.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                Intent intent = new Intent(SettingActivity.this, SettingPasswordActivity.class);
                if(on){
                    intent.putExtra("type", "open");
                }else {
                    intent.putExtra("type", "close");
                }
                startActivity(intent);
            }
        });
        rl_version_update.setOnClickListener(this);
        tv_agreement.setOnClickListener(this);
        rl_opinion_feedback.setOnClickListener(this);
        tv_contact_us.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLock = sp.getBoolean("isLock", false);
        if(isLock){
            tb_pwd_set.setToggleOn();
        }else {
            tb_pwd_set.setToggleOff();
        }
        isMsgRemind = sp.getBoolean("isMsgRemind", false);
        if(isMsgRemind){
            tb_clock_remind.setToggleOn();
        }else {
            tb_clock_remind.setToggleOff();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()){
            case R.id.rl_version_update:
//                intent = new Intent(this, VersionUpdateActivity.class);
//                startActivity(intent);
                showToastMsgShort("当前版本：" + SystemUtil.getVersionName(this));
                break;
            case R.id.tv_agreement:
                AlertDialog.Builder agreement_builder = new AlertDialog.Builder(this);
                View agreement_view = View.inflate(this, R.layout.software_agreement_dialog, null);
                agreement_dialog = agreement_builder.create();
                agreement_builder.setView(agreement_view);
                agreement_builder.show();
                agreement_dialog.setCancelable(true);
                break;
            case R.id.rl_opinion_feedback:
                if(!TextUtils.isEmpty(token)) {
                    intent = new Intent(SettingActivity.this, OpinionFeedbackActivity.class);
                    startActivity(intent);
                }else {
                    showToastMsgShort("登录后可用");
                }
                break;
            case R.id.tv_contact_us:
                intent = new Intent(this, ContactUsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(agreement_dialog != null && agreement_dialog.isShowing()){
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
