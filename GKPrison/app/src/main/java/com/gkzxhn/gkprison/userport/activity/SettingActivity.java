package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.zcw.togglebutton.ToggleButton;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity {

    private ToggleButton tb_msg_remind;
    private ToggleButton tb_clock_remind;
    private ToggleButton tb_pwd_set;
    private RelativeLayout rl_version_update;
    private TextView tv_agreement;
    private AlertDialog agreement_dialog;
    private RelativeLayout rl_opinion_feedback;// 意见反馈

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_setting, null);
        tb_msg_remind = (ToggleButton) view.findViewById(R.id.tb_msg_remind);
        tb_clock_remind = (ToggleButton) view.findViewById(R.id.tb_clock_remind);
        tb_pwd_set = (ToggleButton) view.findViewById(R.id.tb_pwd_set);
        rl_version_update = (RelativeLayout) view.findViewById(R.id.rl_version_update);
        tv_agreement = (TextView) view.findViewById(R.id.tv_agreement);
        rl_opinion_feedback = (RelativeLayout) view.findViewById(R.id.rl_opinion_feedback);
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
                AlertDialog.Builder agreement_builder = new AlertDialog.Builder(this);
                View agreement_view = View.inflate(this, R.layout.software_agreement_dialog, null);
                LinearLayout ll_explain_content = (LinearLayout) agreement_view.findViewById(R.id.ll_explain_content);
                agreement_dialog = agreement_builder.create();
                agreement_builder.setView(agreement_view);
                agreement_builder.show();
                agreement_dialog.setCancelable(true);
                ll_explain_content.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        long downTime = 0;
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downTime = System.currentTimeMillis();
                                Log.i("按下了...", downTime + "");
                                break;
                            case MotionEvent.ACTION_UP:
                                long upTime = System.currentTimeMillis();
                                if (upTime - downTime < 500) {
                                    agreement_dialog.dismiss();
                                }
                                Log.i("离开了...", upTime + "..." + (upTime - downTime));
                                break;
                        }
                        return false;
                    }
                });
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
