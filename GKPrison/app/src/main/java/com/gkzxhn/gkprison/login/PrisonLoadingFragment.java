package com.gkzxhn.gkprison.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.gkzxhn.gkprison.utils.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;


/**
 * created by hzn 2015/12/14
 * 监狱用户登录界面
 */
public class PrisonLoadingFragment extends BaseFragment {

    private SweetAlertDialog sadDialog;
    private Button btn_login;
    private EditText et_username;
    private EditText et_password;
    private String username;
    private String password;
    private SharedPreferences sp;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:// 云信登录成功
                    sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    sadDialog.setTitleText("登录成功！")
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", username);
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.putBoolean("isCommonUser", false);
                    editor.commit();
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
                    break;
                case 1:// 云信id登录失败
                    int error_code = (int) msg.obj;
                    String error_reason = "登录失败！";
                    switch (error_code){
                        case 302:
                            error_reason = "用户名或密码错误！";
                            break;
                        case 503:
                            error_reason = "服务器繁忙！";
                            break;
                        case 415:
                            error_reason = "网络出错，请检查网络！";
                            break;
                        case 408:
                            error_reason = "请求超时，请稍后再试！";
                            break;
                        case 403:
                            error_reason = "非法操作或没有权限！";
                            break;
                        case 200:
//                            error_reason = "非法操作或没有权限！";
//                            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 422:
                            error_reason = "您的账号已被禁用！";
                            break;
                        case 500:
                            error_reason = "服务器错误！";
                            break;
                        case 416:
                            error_reason = "操作频繁，请稍后再试！";
                            break;
                        default:
                            Toast.makeText(context, "登录失败！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                    sadDialog.setTitleText(error_reason)
                            .setConfirmText("确定")
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    sadDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    });
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_prison_loading, null);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        et_username = (EditText) view.findViewById(R.id.et_username);
        et_password = (EditText) view.findViewById(R.id.et_password);
        return view;
    }

    @Override
    protected void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isNetworkAvailable()){
                    showToastMsgShort("网络不可用，请检查网络设置");
                    return;
                }
                username = et_username.getText().toString().trim();
                password = et_password.getText().toString().trim();
                if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    sadDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                        .setTitleText("正在登录...");
                    sadDialog.setCancelable(false);
                    sadDialog.show();
                    LoginInfo info = new LoginInfo(username, tokenFromPassword(password)); // config...
                    final RequestCallback<LoginInfo> callback =
                            new RequestCallback<LoginInfo>() {
                                @Override
                                public void onSuccess(LoginInfo loginInfo) {
                                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                                    handler.sendEmptyMessage(0);
                                    Looper.loop();
                                }

                                @Override
                                public void onFailed(int i) {
                                    switch (i){
                                        case 302:
                                            Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 503:
                                            Toast.makeText(context, "服务器繁忙", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 415:
                                            Toast.makeText(context, "网络出错，请检查网络", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 408:
                                            Toast.makeText(context, "请求超时，请稍后再试", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 403:
                                            Toast.makeText(context, "非法操作或没有权限", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 200:
                                            Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 422:
                                            Toast.makeText(context, "您的账号已被禁用", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 500:
                                            Toast.makeText(context, "服务器错误", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 416:
                                            Toast.makeText(context, "操作频繁，请稍后再试", Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            Toast.makeText(context, "登录失败1", Toast.LENGTH_SHORT).show();
                                            Log.i("登录失败1", i + "");
                                            break;
                                    }
                                    Message msg = handler.obtainMessage();
                                    msg.what = 1;
                                    msg.obj = i;
                                    handler.sendMessage(msg);
                                    Looper.loop();
                                }

                                @Override
                                public void onException(Throwable throwable) {
                                    Toast.makeText(context, "登录异常", Toast.LENGTH_SHORT).show();
                                    Log.i("云信id登录异常", throwable.getMessage());
                                    handler.sendEmptyMessage(1);
                                    Looper.loop();
                                }
                                // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
                            };
                    NIMClient.getService(AuthService.class).login(info)
                            .setCallback(callback);
                }
            }
        });
    }

    private String tokenFromPassword(String password) {
        String appKey = readAppKey(context);
        boolean isDemo = "45c6af3c98409b18a84451215d0bdd6e".equals(appKey)
                || "fe416640c8e8a72734219e1847ad2547".equals(appKey);

        return isDemo ? MD5Utils.ecoder(password) : password;
    }

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
}
