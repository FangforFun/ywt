package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.jungly.gridpasswordview.GridPasswordView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 设置密码
 */
public class SettingPasswordActivity extends BaseActivity {

    @BindView(R.id.tv_please_input_pwd)
    TextView tv_please_input_pwd;
    @BindView(R.id.gpv_pwd)
    GridPasswordView gpv_pwd;
    @BindView(R.id.gpv_confirm_pwd)
    GridPasswordView gpv_confirm_pwd;
    @BindView(R.id.gpv_cancel_pwd)
    GridPasswordView gpv_cancel_pwd;
    @BindView(R.id.rl_pwd)
    RelativeLayout rl_pwd;
    @BindView(R.id.tv_not_match_pwd)
    TextView tv_not_match_pwd;
    private String pwd;
    private String confirm_pwd;
    private AlertDialog dialog;
    private Handler handler = new Handler();

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_setting_password, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("设置密码");
        setBackVisibility(View.VISIBLE);
        String type = getIntent().getStringExtra("type");
        if (type.equals("close")) {
            gpv_cancel_pwd.setVisibility(View.VISIBLE);
            gpv_pwd.setVisibility(View.GONE);
            gpv_confirm_pwd.setVisibility(View.GONE);
        }
        gpv_cancel_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
            }

            @Override
            public void onInputFinish(String psw) {
                if (psw.equals(SPUtil.get(SettingPasswordActivity.this, "app_password", "") + "")) {
                    SPUtil.put(SettingPasswordActivity.this, "app_password", "canceled");
                    SPUtil.put(SettingPasswordActivity.this, "isLock", false);
                    handler.postDelayed(show_dialog_task, 500);
                } else {
                    tv_not_match_pwd.setVisibility(View.VISIBLE);
                    tv_not_match_pwd.setText("密码错误，请重新输入");
                    gpv_cancel_pwd.clearPassword();
                }
            }
        });
        gpv_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
                tv_not_match_pwd.setVisibility(View.GONE);
            }

            @Override
            public void onInputFinish(String psw) {
                pwd = psw;
                handler.postDelayed(delay_dismiss_pwd, 500);
            }
        });
        gpv_confirm_pwd.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                confirm_pwd = psw;
                if (pwd.equals(confirm_pwd)) {
                    SPUtil.put(SettingPasswordActivity.this, "isLock", true);
                    SPUtil.put(SettingPasswordActivity.this, "app_password", pwd);
                    handler.postDelayed(show_dialog_task, 500);
                } else {
                    handler.postDelayed(delay_dismiss_confirm_pwd, 1000);
                }
            }
        });
    }

    /**
     * 密码不匹配时延时隐藏确认输入密码
     */
    private Runnable delay_dismiss_confirm_pwd = new Runnable() {
        @Override
        public void run() {
            gpv_confirm_pwd.clearPassword();
            gpv_confirm_pwd.setVisibility(View.GONE);
            gpv_pwd.clearPassword();
            gpv_pwd.setVisibility(View.VISIBLE);
            tv_please_input_pwd.setText("输入密码");
            tv_not_match_pwd.setVisibility(View.VISIBLE);
        }
    };

    /**
     * 延时隐藏输入密码
     */
    private Runnable delay_dismiss_pwd = new Runnable() {
        @Override
        public void run() {
            gpv_pwd.setVisibility(View.GONE);
            gpv_confirm_pwd.setVisibility(View.VISIBLE);
            tv_please_input_pwd.setText("确认密码");
        }
    };

    /**
     * 提示对话框
     */
    private Runnable show_dialog_task = new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingPasswordActivity.this);
            builder.setMessage("设置成功");
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SettingPasswordActivity.this.finish();
                }
            });
            dialog = builder.create();
            dialog.show();
        }
    };

    @Override
    public void onBackPressed() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            super.onBackPressed();
        }
    }
}
