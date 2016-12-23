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
import com.gkzxhn.gkprison.application.MyApplication;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.view.sweet_alert_dialog.SweetAlertDialog;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;
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

    private String mAccount = "001001002";
    private String mPassword = "";
    private String mAddr = "106.14.18.98";

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
        SPUtil.put(getActivity(), "token", username);
        SPUtil.put(getActivity(), "username", username);
        SPUtil.put(getActivity(), "password", password);
        SPUtil.put(getActivity(), "isCommonUser", false);
        loginKeda();
//        move();
    }

    private void loginKeda() {
        new Thread(){
            @Override
            public void run() {
                login();
            }
        }.start();
    }

    private void move() {
        sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
        sadDialog.setTitleText("登录成功！").changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
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
        if (!Utils.isNetworkAvailable(getActivity())) {
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
                sadDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.tv_red));
                sadDialog.setTitleText("登录失败").setConfirmText("确定").changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sadDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                });
                if (StringUtils.isNull(failedMsg)) {
                    Toast.makeText(MyApplication.getApplication(), "登录失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyApplication.getApplication(), failedMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
