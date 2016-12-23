package com.gkzxhn.gkprison.userport.ui.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.login.RegisterActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Author: Huang ZN
 * Date: 2016/12/22
 * Email:943852572@qq.com
 * Description:LoginActivity登录
 */

public class LoginActivity extends BaseActivityNew implements LoginContract.View{

    // 绑定id

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.layout_prison)
    LinearLayout layout_prison;
    @BindView(R.id.et_username)
    EditText et_username;
    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.layout_personal)
    RelativeLayout layout_personal;
    @BindView(R.id.et_personal_username)
    EditText et_personal_username;
    @BindView(R.id.et_personal_id_code)
    EditText et_personal_id_code;
    @BindView(R.id.et_verify_code)
    EditText et_verify_code;
    @BindView(R.id.tv_send_verify_code)
    TextView tv_send_verify_code;

    @Inject LoginPresenter mPresenter;

    private Handler handler;
    private int countdown = 60;

    private ProgressDialog verify_dialog;

    public static void startActivity(Context mContext){
        Intent intent = new Intent(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public int setLayoutResId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        tv_title.setText(getString(R.string.login_text));
        mPresenter.attachView(this);
    }

    @Override
    protected void initInjector() {
        DaggerLoginComponent.builder()
                .appComponent(getAppComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @Override
    public void showMainUi() {

    }

    @Override
    public void toRegister() {

    }

    @Override
    public void toLoginWithoutAccount() {

    }

    @Override
    public void showProgress() {
        if (verify_dialog == null){
            verify_dialog = new ProgressDialog(this);
            verify_dialog.setMessage("请稍候...");
            verify_dialog.show();
        }else {
            if (!verify_dialog.isShowing())
                verify_dialog.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (verify_dialog != null && verify_dialog.isShowing())
            verify_dialog.dismiss();
    }

    @Override
    public void showFailed(String msg) {

    }

    @Override
    public void showSuccess(String msg) {

    }

    @Override
    public void showToast(String msg) {
        ToastUtil.showShortToast(msg);
    }

    @Override
    public void startCountDown() {
        if (!isRunning) {
            handler = new Handler();
            tv_send_verify_code.setEnabled(false);
            tv_send_verify_code.setBackgroundColor(getResources().getColor(R.color.tv_gray));
            tv_send_verify_code.setTextColor(getResources().getColor(R.color.white));
            handler.post(count_down_task);
        }
    }

    @Override
    public void removeCountDown() {
        removeCodeTask();
    }

    private boolean isRunning = false;
    /**
     * 验证码发送倒计时任务
     */
    private Runnable count_down_task = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            String text = countdown + " s";
            tv_send_verify_code.setText(text);
            countdown--;
            if (countdown == 0) {
                removeCodeTask();
            } else {
                handler.postDelayed(count_down_task, 1000);
            }
        }
    };

    /**
     * 移除倒计时任务
     */
    private void removeCodeTask() {
        if (isRunning) {
            handler.removeCallbacks(count_down_task);
            tv_send_verify_code.setEnabled(true);
            tv_send_verify_code.setBackgroundColor(getResources().getColor(R.color.white));
            tv_send_verify_code.setTextColor(getResources().getColor(R.color.theme));
            tv_send_verify_code.setText(getString(R.string.send_verify_code));
            countdown = 60;
            isRunning = false;
        }
    }

    @OnClick({R.id.tv_send_verify_code, R.id.btn_person_login,
            R.id.btn_personal_switch, R.id.bt_register, R.id.bt_fast_login,
            R.id.btn_prison_login, R.id.btn_prison_switch})
    public void OnClick(View view){
        switch (view.getId()){
            case R.id.tv_send_verify_code:
                if (isRunning)
                    return; // 正在倒计时
                if (SystemUtil.isNetWorkUnAvailable())
                    return;// 网络不可用
                mPresenter.sendVerifyCode(et_personal_username);
                break;
            case R.id.btn_person_login:
                if (SystemUtil.isNetWorkUnAvailable())
                    return;

                break;
            case R.id.btn_personal_switch:
                switchLoginUi();
                break;
            case R.id.bt_register:
                RegisterActivity.startActivity(this);
                break;
            case R.id.bt_fast_login:
                if (SystemUtil.isNetWorkUnAvailable())
                    return;

                break;
            case R.id.btn_prison_login:
                if (SystemUtil.isNetWorkUnAvailable())
                    return;
                SPUtil.put(this, "isRegisteredUser", false);
                MainActivity.startActivity(this);
                finish();
                break;
            case R.id.btn_prison_switch:
                switchLoginUi();
                break;
        }
    }

    /**
     * 切换登录方式UI
     */
    private void switchLoginUi() {
        layout_personal.setVisibility(layout_personal.getVisibility()
                == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
