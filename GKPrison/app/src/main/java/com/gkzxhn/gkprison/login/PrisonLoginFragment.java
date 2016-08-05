package com.gkzxhn.gkprison.login;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.view.sweet_alert_dialog.SweetAlertDialog;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * created by hzn 2015/12/14
 * description:监狱用户登录界面
 */
public class PrisonLoginFragment extends BaseFragment {

    private static final String TAG = "PrisonLoginFragment";
    @BindView(R.id.et_username)
    EditText et_username;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.btn_login)
    Button btn_login;
    private SweetAlertDialog sadDialog;
    private String username;
    private String password;
    private RequestCallback<LoginInfo> callback;// 云信id登录回调
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:// 云信登录成功
                    loginSuccess();
                    break;
                case 1:// 云信id登录失败
                    loginFailed(msg);
                    break;
            }
        }
    };

    /**
     * 登录失败
     * @param msg
     */
    private void loginFailed(Message msg) {
        int error_code = (int) msg.obj;
        String error_reason = "登录失败！";
        switch (error_code) {
            case 302:error_reason = "用户名或密码错误！";break;
            case 503:error_reason = "服务器繁忙！";break;
            case 415:error_reason = "网络出错，请检查网络！";break;
            case 408:error_reason = "请求超时，请稍后再试！";break;
            case 403:error_reason = "非法操作或没有权限！";break;
            case 422:error_reason = "您的账号已被禁用！";break;
            case 500:error_reason = "服务器错误！";break;
            case 416:error_reason = "操作频繁，请稍后再试！";break;
        }
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
        sadDialog.setTitleText(error_reason).setConfirmText("确定").changeAlertType(SweetAlertDialog.ERROR_TYPE);
        sadDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
    }

    /**
     * 登录成功
     */
    private void loginSuccess() {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
        sadDialog.setTitleText("登录成功！").changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        SPUtil.put(getActivity(), "token", username);
        SPUtil.put(getActivity(), "username", username);
        SPUtil.put(getActivity(), "password", password);
        SPUtil.put(getActivity(), "isCommonUser", false);
        DemoCache.setAccount(username);// 设置云信id缓存
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sadDialog.dismiss();
                Intent intent = new Intent(context, DateMeetingListActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }, 1000);
    }

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_prison_loading, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
    }

    /**
     * 设置云信id登录回调
     */
    private void setNIMLoginCallBack() {
        callback = new RequestCallback<LoginInfo>() {
                    @Override public void onSuccess(LoginInfo loginInfo) {
                        handler.sendEmptyMessage(0);
                        Looper.loop();
                    }

                    @Override public void onFailed(int i) {
                        Log.e(TAG, "NIM login failed : " + i);
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = i;
                        handler.sendMessage(msg);
                        Looper.loop();
                    }

                    @Override public void onException(Throwable throwable) {
                        Toast.makeText(context, "登录异常", Toast.LENGTH_SHORT).show();
                        Log.i("云信id登录异常", throwable.getMessage());
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = "登录异常";
                        handler.sendMessage(msg);
                        Looper.loop();
                    }
                };
    }

    /**
     * @param password
     * @return
     */
    private String tokenFromPassword(String password) {
        String appKey = readAppKey(context);
        boolean isDemo = "45c6af3c98409b18a84451215d0bdd6e".equals(appKey)
                || "fe416640c8e8a72734219e1847ad2547".equals(appKey);
        return isDemo ? MD5Utils.ecoder(password) : password;
    }

    /**
     * 读取appkey
     *
     * @param context
     * @return
     */
    private static String readAppKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @OnClick(R.id.btn_login)
    public void onClick() {
        if (!Utils.isNetworkAvailable()) {
            showToastMsgShort("网络不可用，请检查网络设置");
            return;
        }
        username = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showToastMsgShort("不能为空");
            return;
        } else {
            sadDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText("正在登录...");
            sadDialog.setCancelable(false);
            sadDialog.show();
            LoginInfo info = new LoginInfo(username, tokenFromPassword(password)); // config...
            setNIMLoginCallBack();// 设置云信id登录回调
            NIMClient.getService(AuthService.class).login(info).setCallback(callback);
        }
    }
}
