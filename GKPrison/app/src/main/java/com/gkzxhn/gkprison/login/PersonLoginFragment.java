package com.gkzxhn.gkprison.login;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.MyApplication;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.requests.LoginService;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.bean.UserInfo;
import com.gkzxhn.gkprison.userport.view.sweet_alert_dialog.SweetAlertDialog;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.keda.sky.app.GKStateMannager;
import com.keda.sky.app.LoginStateManager;
import com.kedacom.kdv.mt.api.Configure;
import com.kedacom.kdv.mt.bean.TMtH323PxyCfg;
import com.kedacom.kdv.mt.constant.EmConfProtocol;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.pc.utils.DNSParseUtil;
import com.pc.utils.FormatTransfer;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.StringUtils;

import java.net.InetAddress;

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
 * created by huangzhengneng on 2016.1.12
 * description:个人用户登录界面
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
    private String username;// 用户名(手机号)
    private String ic_card_num;// 身份证
    private boolean isRunning = false;// 倒计时任务正在执行
    private int countdown = 60;// 倒计时
    private int successCode = 0;// 登录成功码  先等于自己服务器  成功之后加1登录云信服务器  等于2才算登录成功
    private UserInfo userInfo;// 用户信息
    private RequestCallback<LoginInfo> callback; // 云信id登录回调
    private LoginInfo info;// 云信id登录信息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:// 云信登录成功
                    successCode++;
                    if (successCode == 2) {
                        loginSuccess();
                    }
                    break;
                case 1:// 云信id登录失败
                    successCode = 0;
                    setFailedUI("登录失败！");
                    break;
            }
        }
    };

    private String mAccount = "001001888";
    private String mPassword = "";
    private String mAddr = "222.240.225.6";

    /**
     * 登录成功
     */
    private void loginSuccess() {
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
        loginKeda();
    }

    /**
     * 下一页
     */
    private void move() {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.success_stroke_color));
        sadDialog.setTitleText("登录成功！")
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
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

    private void loginKeda() {
        new Thread(){
            @Override
            public void run() {
                login();
            }
        }.start();
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
                } else {
                    if (!Utils.isMobileNO(username)) {
                        showToastMsgShort("请输入正确的用户名");
                    } else {
                        if (Utils.isNetworkAvailable(getActivity())) {
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
                String identifying_code = et_identifying_code.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(ic_card_num) || TextUtils.isEmpty(identifying_code)) {
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (Utils.isNetworkAvailable(getActivity())) {
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
     */
    private void loginPersonAccount(String userInfo) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HEAD).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build();
        LoginService login = retrofit.create(LoginService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), userInfo);
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
                            setFailedUI("验证码请求异常，请稍后再试！");
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

    private void login() {
        MyApplication.getApplication().isH323 = true;
        if (!MyApplication.getApplication().isH323) {
            Configure.setAudioPriorCfgCmd(false);
            if (isMtH323Local()) {
                // 取消代理，成功则 登陆aps
                setCancelH323PxyCfgCmd();
                return;
            }
            LoginStateManager.loginAps(mAccount, mPassword, mAddr);
        } else {
            Configure.setAudioPriorCfgCmd(true);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    String ip = DNSParseUtil.dnsParse(mAddr);
                    // 解析成功，注册代理
                    long dwIp = 0;
                    try {
                        dwIp = FormatTransfer.lBytesToLong(InetAddress.getByName(ip).getAddress());
                    } catch (Exception e) {
                        dwIp = FormatTransfer.reverseInt((int) NetWorkUtils.ip2int(ip));
                    }
                    long localH323Ip = getMtH323IpLocal();
                    // 没有注册代理，或者 注册代理的ip 改变了
                    if (localH323Ip == 0 || dwIp != localH323Ip) {
                        setH323PxyCfgCmd(dwIp);
                        return;
                    }
                    // 注册代理
                    GKStateMannager.instance().registerGKFromH323(mAccount, mPassword, "");
                }
            }).start();
        }
    }

    /**
     * 注册H323代理
     */
    private void setH323PxyCfgCmd(final long dwIp) {
        android.util.Log.i("Login setting", "H323设置代理:" + dwIp);
        new Thread(new Runnable() {

            @Override
            public void run() {
                Configure.setH323PxyCfgCmd(true, true, dwIp);
                // 关闭并重新开启协议栈
                Configure.stackOnOff((short) EmConfProtocol.em323.ordinal());
            }
        }).start();
    }

    /**
     * 检测本地 是否是代理 代理ip
     * @return
     */

    private long getMtH323IpLocal() {
        // 从数据库获取当前 是否注册了代理
        StringBuffer H323PxyStringBuf = new StringBuffer();
        Configure.getH323PxyCfg(H323PxyStringBuf);
        String h323Pxy = H323PxyStringBuf.toString();
        TMtH323PxyCfg tmtH323Pxy = new Gson().fromJson(h323Pxy, TMtH323PxyCfg.class);
        // { "achNumber" : "", "achPassword" : "", "bEnable" : true, "dwSrvIp" : 1917977712, "dwSrvPort" : 2776 }
        if (null != tmtH323Pxy && tmtH323Pxy.bEnable) {
            android.util.Log.i("Login", "tmtH323Pxy.dwSrvIp   " + tmtH323Pxy.dwSrvIp);
            return tmtH323Pxy.dwSrvIp;
        }
        return 0;
    }

    /**
     * 检测本地 是否是代理
     * @return
     */

    private boolean isMtH323Local() {
        // 从数据库获取当前 是否注册了代理
        StringBuffer H323PxyStringBuf = new StringBuffer();
        Configure.getH323PxyCfg(H323PxyStringBuf);
        String h323Pxy = H323PxyStringBuf.toString();
        TMtH323PxyCfg tmtH323Pxy = new Gson().fromJson(h323Pxy, TMtH323PxyCfg.class);
        // { "achNumber" : "", "achPassword" : "", "bEnable" : true, "dwSrvIp" : 1917977712, "dwSrvPort" : 2776 }
        if (null != tmtH323Pxy) {
            android.util.Log.i("Login", "是否h323代理   " + tmtH323Pxy.bEnable);
            return tmtH323Pxy.bEnable;
        }
        return false;
    }

    /**
     * 设置取消注册H323代理
     */
    private void setCancelH323PxyCfgCmd() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                // 取消代理
                Configure.setH323PxyCfgCmd(false, false, 0);
                // 关闭并重新开启协议栈
                Configure.stackOnOff((short) EmConfProtocol.em323.ordinal());
            }
        }).start();
    }

    /**
     * 设置代理模式成功/失败
     * @param isEnable true:设置代理可用
     */
    public void setH323PxyCfgCmdResult(final boolean isEnable) {
        MyApplication.getApplication().isH323 = isEnable;
        if (!isEnable) {
            Log.i("Login", "取消代理 -- 登录APS " + mAccount + "-" + mPassword);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LoginStateManager.loginAps(mAccount, mPassword, mAddr);
                }
            }).start();
        } else {
            Log.i("Login", " 注册代理 -- 登录gk ");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 注册代理
                    GKStateMannager.instance().registerGKFromH323(mAccount, mPassword, "");
                }
            }).start();

            return;

        }
    }

    /**
     * 登录成功/失败
     *
     * @param isSuccessed
     */
    public void loginSuccessed(final boolean isSuccessed, final String failedMsg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isSuccessed) {
                    Toast.makeText(MyApplication.getApplication(), "登录成功", Toast.LENGTH_SHORT).show();
                    move();// 下一页
                    return;
                }
                setFailedUI("登录失败！");
                if (StringUtils.isNull(failedMsg)) {
                    Toast.makeText(MyApplication.getApplication(), "登录失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyApplication.getApplication(), failedMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
