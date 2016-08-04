package com.gkzxhn.gkprison.login;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.requests.LoginService;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.bean.UserInfo;
import com.gkzxhn.gkprison.userport.view.sweet_alert_dialog.SweetAlertDialog;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 * 个人用户登录界面
 */
public class PersonLoginFragment extends BaseFragment {

    private static final java.lang.String TAG = "PersonLoginFragment";
    @BindView(R.id.tv_login_notice)
    TextView tv_login_notice;
    @BindView(R.id.et_login_username)
    EditText et_login_username;
    @BindView(R.id.et_login_ic_card_num)
    EditText et_login_ic_card_num;
    @BindView(R.id.et_identifying_code)
    EditText et_identifying_code;
    @BindView(R.id.tv_send_identifying_code)
    TextView tv_send_identifying_code;
    @BindView(R.id.ll_checkcode)
    LinearLayout ll_checkcode;
    @BindView(R.id.btn_login)
    Button btn_login;
    @BindView(R.id.bt_register)
    Button bt_register;
    @BindView(R.id.bt_fast_login)
    Button bt_fast_login;
    private SweetAlertDialog sadDialog;
    private String username;
    private String ic_card_num;
    private String identifying_code;// 验证码
    private boolean isRunning = false;// 倒计时任务正在执行
    private boolean isOK = false;// 服务器登录返回的是0才变成true
    private int countdown = 60;
    private int successCode = 0;
    private UserInfo userInfo;
    private RequestCallback<LoginInfo> callback;
    private LoginInfo info;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:// 云信登录成功
                    successCode++;
                    if (successCode == 2) {
                        loginSuccessed();
                    }
                    break;
                case 1:// 云信id登录失败
                    successCode = 0;
                    setFailedUI("登录失败！");
                    break;
            }
        }
    };

    /**
     * 登录成功
     */
    private void loginSuccessed() {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.success_stroke_color));
        sadDialog.setTitleText("登录成功！")
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        SPUtil.put(getActivity(), "username", username);
        SPUtil.put(getActivity(), "password", ic_card_num);
        SPUtil.put(getActivity(), "isCommonUser", true);
        if (userInfo != null) {
            SPUtil.put(getActivity(), "name", userInfo.getUser().getName());
            SPUtil.put(getActivity(), "relationship", userInfo.getUser().getRelationship());
            SPUtil.put(getActivity(), "token", userInfo.getToken());
            SPUtil.put(getActivity(), "family_id", userInfo.getUser().getId());
            SPUtil.put(getActivity(), "jail", userInfo.getJail());
        }
        SPUtil.put(getActivity(), "isRegisteredUser", true);
        DemoCache.setAccount(userInfo.getToken());
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (sadDialog != null && sadDialog.isShowing()) {
                    sadDialog.dismiss();
                }
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }, 500);
    }

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_person_loading, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onDestroy() {
        if (handler != null && isRunning) {
            removeCodeTask();
        }
        if (sadDialog != null && sadDialog.isShowing()) {
            sadDialog.dismiss();
            sadDialog = null;
        }
        super.onDestroy();
    }

    /**
     * 验证码发送倒计时任务
     */
    private Runnable identifying_Code_Task = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            countdown--;
            tv_send_identifying_code.setText(countdown + "秒后可重发");
            if (countdown == 0) {
                removeCodeTask();
            } else {
                handler.postDelayed(identifying_Code_Task, 1000);
            }
        }
    };

    /**
     * 移除倒计时任务
     */
    private void removeCodeTask() {
        handler.removeCallbacks(identifying_Code_Task);
        tv_send_identifying_code.setEnabled(true);
        tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.white));
        tv_send_identifying_code.setTextColor(getResources().getColor(R.color.theme));
        tv_send_identifying_code.setText("发送验证码");
        countdown = 60;
        isRunning = false;
    }

    @OnClick({R.id.tv_send_identifying_code, R.id.btn_login, R.id.bt_register, R.id.bt_fast_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_send_identifying_code:
                username = et_login_username.getText().toString().trim();
                // 判断手机号码是否合法
                if (TextUtils.isEmpty(username)) {
                    showToastMsgShort("手机号为空");
                    return;
                } else {
                    if (!Utils.isMobileNO(username)) {
                        showToastMsgShort("请输入正确的用户名");
                        return;
                    } else {
                        if (Utils.isNetworkAvailable()) {
                            final String phone_str = "{\"apply\":{\"phone\":\"" + username + "\"}}";
                            initAndShowDialog("正在发送...");
                            getVerificationCode(phone_str);
                        } else {
                            showToastMsgLong("没有网络");
                            return;
                        }
                        tv_send_identifying_code.setEnabled(false);
                        tv_send_identifying_code.setBackgroundColor(getResources().getColor(R.color.tv_gray));
                        tv_send_identifying_code.setTextColor(getResources().getColor(R.color.white));
                        tv_send_identifying_code.setText(countdown + "秒后可重发");
                        handler.postDelayed(identifying_Code_Task, 1000);
                    }
                }
                break;
            case R.id.btn_login:
                username = et_login_username.getText().toString().trim();
                ic_card_num = et_login_ic_card_num.getText().toString().trim();
                identifying_code = et_identifying_code.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(ic_card_num) || TextUtils.isEmpty(identifying_code)) {
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (Utils.isNetworkAvailable()) {
                        initAndShowDialog("正在登录...");
                        setNIMLoginCallBack();// 设置云信id登录回调
                        String str = "{\"session\":{ \"phone\":\"" + username + "\", \"uuid\":\""
                                + ic_card_num + "\", \"code\":\"" + identifying_code + "\"}}";
                        loginPersonAccount(str);
                    } else {
                        showToastMsgLong("没有网络");
                    }
                }
                break;
            case R.id.bt_register:
                Intent intent1 = new Intent(context, RegisterActivity.class);
                startActivity(intent1);
                break;
            case R.id.bt_fast_login:
                SPUtil.put(getActivity(), "isRegisteredUser", false);
                Intent intent2 = new Intent(context, MainActivity.class);
                startActivity(intent2);
                getActivity().finish();
                break;
        }
    }

    /**
     * 登录个人用户
     * @param str
     */
    private void loginPersonAccount(String str) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();
        LoginService login = retrofit.create(LoginService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), str);
        login.loginPersonAccount(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        setFailedUI("登录失败，请稍后再试！");
                    }

                    @Override public void onNext(UserInfo userInfo) {
                        PersonLoginFragment.this.userInfo = userInfo;
                        int code = userInfo.getCode();
                        if (code == 401) {
                            setFailedUI("用户身份验证失败！");
                            successCode = 0;
                        } else if (code == 404 || code == 413) {
                            setFailedUI("验证码错误！");
                            successCode = 0;
                        } else if (code == 200) {
                            successCode++;
                            Log.i(TAG, "login success : " + userInfo.toString());
                            info = new LoginInfo(userInfo.getToken(), userInfo.getToken()); // config...
                            NIMClient.getService(AuthService.class).login(info)
                                    .setCallback(callback);
                            SPUtil.put(getActivity(), "avatar", userInfo.getAvatar().split("\\|")[2]);
                        } else {
                            Log.e(TAG, "login failed, code is : " + code);
                            setFailedUI("登录失败！");
                            successCode = 0;
                        }
                    }
                });
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
                switch (i) {
                    case 302:showToastMsgShort("手机号或者身份证号错误");break;
                    case 503:showToastMsgShort("服务器繁忙");break;
                    case 415:showToastMsgShort("网络出错，请检查网络");break;
                    case 408:showToastMsgShort("请求超时，请稍后再试");break;
                    case 403:showToastMsgShort("非法操作或没有权限");break;
                    case 422:showToastMsgShort("您的账号已被禁用");break;
                    case 500:showToastMsgShort("服务器错误");break;
                    default:showToastMsgShort("登录失败");break;
                }
                handler.sendEmptyMessage(1);
                Looper.loop();
            }

            @Override public void onException(Throwable throwable) {
                showToastMsgShort("登录异常");
                Log.i(TAG, "云信id登录异常 : " + throwable.getMessage());
                handler.sendEmptyMessage(1);
                Looper.loop();
            }
        };
    }

    /**
     * 初始化并且显示对话框
     * @param titleText
     */
    private void initAndShowDialog(String titleText) {
        sadDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(titleText);
        sadDialog.setCancelable(false);
        sadDialog.show();
    }

    /**
     * 获取验证码
     * @param phone_str
     */
    private void getVerificationCode(String phone_str) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();
        LoginService login = retrofit.create(LoginService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), phone_str);
        login.getVerificationCode(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "get verification code failed : " + e.getMessage());
                        setFailedUI("验证码请求失败，请稍后再试！");
                        removeCodeTask();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            if (result.contains("200")) {
                                sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                                sadDialog.setTitleText("已发送！")
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sadDialog.dismiss();
                                    }
                                }, 1000);
                            } else{
                                setFailedUI("验证码请求失败，请稍后再试！");
                            }
                            Log.i(TAG, "login verification code : " + result);
                        }catch (Exception e){
                            e.printStackTrace();
                            setFailedUI("验证码请求失败，请稍后再试！");
                        }
                    }
                });
    }

    /**
     * 设置请求失败的dialog提示
     * @param titleText
     */
    private void setFailedUI(String titleText) {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
        sadDialog.setTitleText(titleText)
                .setConfirmText("确定")
                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
        sadDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
    }
}
