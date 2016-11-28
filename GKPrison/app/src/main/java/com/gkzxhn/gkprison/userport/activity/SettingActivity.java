package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.zcw.togglebutton.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity {

    @BindView(R.id.tb_clock_remind)
    ToggleButton tb_clock_remind;
    @BindView(R.id.tb_pwd_set)
    ToggleButton tb_pwd_set;
    @BindView(R.id.rl_opinion_feedback)
    RelativeLayout rl_opinion_feedback;
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.rl_version_update)
    RelativeLayout rl_version_update;
    @BindView(R.id.ll_setting_options)
    LinearLayout ll_setting_options;
    @BindView(R.id.tv_agreement)
    TextView tv_agreement;
    @BindView(R.id.tv_contact_us)
    TextView tv_contact_us;
    private AlertDialog agreement_dialog;
    private String token;
    private boolean isLock;
    private boolean isMsgRemind;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_setting, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
        token = (String) SPUtil.get(this, "token", "");
        checkStatus();// 检查相关设置状态
        setTitle("设置");
        setBackVisibility(View.VISIBLE);
        tv_version.setText("V " + SystemUtil.getVersionName(getApplicationContext()));
        tb_clock_remind.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    showReminderDialog();// 开启闹钟提醒对话框
                    SPUtil.put(SettingActivity.this, "isMsgRemind", true);
                } else {
                    showToastMsgShort("闹钟提醒已关闭");
                    SPUtil.put(SettingActivity.this, "isMsgRemind", false);
                }
            }
        });
        tb_pwd_set.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                startPwsSetting(on);// 设置密码开关
            }
        });
    }

    /**
     * 打开/关闭密码设置
     *
     * @param on
     */
    private void startPwsSetting(boolean on) {
        Intent intent = new Intent(SettingActivity.this, SettingPasswordActivity.class);
        if (on) {
            intent.putExtra("type", "open");
        } else {
            intent.putExtra("type", "close");
        }
        startActivity(intent);
    }

    /**
     * 开启闹钟提醒对话框
     */
    private void showReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setMessage(R.string.alarm_reminder_msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 判断是否枷锁和闹钟提醒
     */
    private void checkStatus() {
        isLock = (boolean) SPUtil.get(this, "isLock", false);
        if (isLock) {
            tb_pwd_set.setToggleOn();
        } else {
            tb_pwd_set.setToggleOff();
        }
        isMsgRemind = (boolean) SPUtil.get(this, "isMsgRemind", false);
        if (isMsgRemind) {
            tb_clock_remind.setToggleOn();
        } else {
            tb_clock_remind.setToggleOff();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus();// 检查状态
    }

    @Override
    public void onBackPressed() {
        if (agreement_dialog != null && agreement_dialog.isShowing()) {
            agreement_dialog.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick({R.id.rl_opinion_feedback, R.id.rl_version_update, R.id.tv_agreement, R.id.tv_contact_us})
    public void onClick(View view) {
        super.onClick(view);
        Intent intent;
        switch (view.getId()) {
            case R.id.rl_opinion_feedback:
                if (!TextUtils.isEmpty(token)) {
                    intent = new Intent(SettingActivity.this, OpinionFeedbackActivity.class);
                    startActivity(intent);
                } else {
                    showToastMsgShort("登录后可用");
                }
                break;
            case R.id.rl_version_update:
                intent = new Intent(this, VersionUpdateActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_agreement:
                showAgreementDialog();// 协议
                break;
            case R.id.tv_contact_us:
                intent = new Intent(this, ContactUsActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 软件协议
     */
    private void showAgreementDialog() {
        AlertDialog.Builder agreement_builder = new AlertDialog.Builder(this);
        View agreement_view = View.inflate(this, R.layout.software_agreement_dialog, null);
        agreement_dialog = agreement_builder.create();
        agreement_builder.setView(agreement_view);
        agreement_builder.show();
        agreement_dialog.setCancelable(true);
    }
}
