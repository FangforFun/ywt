package com.gkzxhn.gkprison.userport.ui.login;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;

import com.blankj.utilcode.utils.RegexUtils;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.LoginService;
import com.gkzxhn.gkprison.api.okhttp.OkHttpUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.api.wrap.LoginWrap;
import com.gkzxhn.gkprison.app.PerActivity;
import com.gkzxhn.gkprison.app.utils.KDInitUtil;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.userport.bean.UserInfo;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
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

import java.io.IOException;
import java.net.InetAddress;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Subscription;

/**
 * Author: Huang ZN
 * Date: 2016/12/22
 * Email:943852572@qq.com
 * Description:登录页面所有逻辑操作类
 */
@PerActivity
public class LoginPresenter implements LoginContract.Presenter {

    private static final String TAG = LoginPresenter.class.getName();
    private LoginContract.View loginContractView;
    private Context mContext;

    private LoginService loginService;
    private Subscription mVerifySubscription;
    private Subscription mLoginPersonSubscription;

    private UserInfo userInfo;// 个人用户登录成功返回的个人信息
    private boolean isCommonUser = true; // 是否普通用户  默认true

    public KedaSDKControl control;

    @Inject
    public LoginPresenter(LoginService loginService, Context context){
        this.mContext = context;
        this.loginService = loginService;
    }

    @Override
    public void login(boolean isCommonUser, String str) {
        if (control == null)
            control = new KedaSDKControl();
        loginContractView.showProgress(mContext.getString(R.string.logining));
        LoginPresenter.this.isCommonUser = isCommonUser;
        if (isCommonUser) {// 普通用户登录
            mLoginPersonSubscription = LoginWrap.getInstance().loginPersonal(loginService, OkHttpUtils.getRequestBody(str), new SimpleObserver<UserInfo>() {
                @Override public void onError(Throwable e) {
                    loginContractView.dismissProgress();
                    loginContractView.showToast(mContext.getString(R.string.login_failed_retry));
                }
                @Override public void onNext(UserInfo userInfo) {
                    LoginPresenter.this.userInfo = userInfo;
                    int statusCode = userInfo.getCode();
                    if (statusCode == 401) {
                        loginContractView.dismissProgress();
                        loginContractView.showToast(mContext.getString(R.string.unexist_account));
                    } else if (statusCode == 404 || statusCode == 413) {
                        loginContractView.dismissProgress();
                        loginContractView.showToast(mContext.getString(R.string.verify_code_error));
                    } else if (statusCode == 200) {
                        Log.i(TAG, "login success : " + userInfo.toString());
                        LoginInfo info = new LoginInfo(userInfo.getToken(), userInfo.getToken()); // config...
                        NIMClient.getService(AuthService.class).login(info)
                                .setCallback(callback);
                        SPUtil.put(mContext, SPKeyConstants.AVATAR, userInfo.getAvatar().split("\\|")[2]);
                    } else {
                        Log.e(TAG, "login failed, code is : " + statusCode);
                        loginContractView.dismissProgress();
                        loginContractView.showToast(mContext.getString(R.string.login_failed_retry));
                    }
                }
            });
        }else {// 监狱管理用户  str = 用户名 + "-" + 密码
            String[] infos = str.split("-");
            LoginInfo info = new LoginInfo(infos[0], infos[1]); // config...
            NIMClient.getService(AuthService.class).login(info)
                    .setCallback(callback);
        }
    }

    /**
     * 云信sdk登录回调
     */
    private RequestCallback<LoginInfo> callback = new RequestCallback<LoginInfo>() {
        @Override public void onSuccess(LoginInfo loginInfo) {
            if (isCommonUser) {// 普通个人用户
                if (userInfo != null && userInfo.getUser() != null) {
                    // 云信登录成功
                    putSP(SPKeyConstants.USERNAME, userInfo.getUser().getPhone());
                    putSP(SPKeyConstants.PASSWORD, userInfo.getUser().getUuid());
                    putSP(SPKeyConstants.NAME, userInfo.getUser().getName());
                    putSP(SPKeyConstants.RELATION_SHIP, userInfo.getUser().getRelationship());
                    putSP(SPKeyConstants.ACCESS_TOKEN, userInfo.getToken());
                    putSP(SPKeyConstants.FAMILY_ID, userInfo.getUser().getId());
                    putSP(SPKeyConstants.JAIL, userInfo.getJail());

                    putSP(SPKeyConstants.IS_COMMON_USER, true);
                    putSP(SPKeyConstants.IS_REGISTERED_USER, true);
                    control.login();
                } else {
                    loginContractView.dismissProgress();
                    loginContractView.showToast(mContext.getString(R.string.login_failed_retry));
                }
            }else {// 监狱管理用户
                putSP(SPKeyConstants.ACCESS_TOKEN, loginInfo.getAccount());
                putSP(SPKeyConstants.USERNAME, loginInfo.getAccount());
                putSP(SPKeyConstants.PASSWORD, loginInfo.getToken());
                putSP(SPKeyConstants.IS_COMMON_USER, false);
                control.login();
            }
        }

        @Override public void onFailed(int i) {
            loginContractView.dismissProgress();
            switch (i) {
                case 302:loginContractView.showToast(mContext.getString(R.string.account_pwd_error));break;
                case 503:loginContractView.showToast(mContext.getString(R.string.server_busy));break;
                case 415:loginContractView.showToast(mContext.getString(R.string.network_error));break;
                case 408:loginContractView.showToast(mContext.getString(R.string.time_out));break;
                case 403:loginContractView.showToast(mContext.getString(R.string.illegal_control));break;
                case 422:loginContractView.showToast(mContext.getString(R.string.account_disable));break;
                case 500:loginContractView.showToast(mContext.getString(R.string.server_error));break;
                default:loginContractView.showToast(mContext.getString(R.string.login_failed_retry));break;
            }
        }

        @Override public void onException(Throwable throwable) {
            loginContractView.dismissProgress();
            loginContractView.showToast(mContext.getString(R.string.login_exception_retry));
            Log.i(TAG, "云信id登录异常 : " + throwable.getMessage());
        }
    };

    @Override
    public void sendVerifyCode(@NonNull EditText editText) {
        String content = editText.getText().toString().trim();
        if (TextUtils.isEmpty(content)){
            loginContractView.showToast(mContext.getString(R.string.null_phone));
            return;
        }
        if (!RegexUtils.isMobileExact(content)){
            loginContractView.showToast(mContext.getString(R.string.unavailable_phone));
            return;
        }
        loginContractView.showProgress(mContext.getString(R.string.sending));
        loginContractView.startCountDown();
        String phone_str = "{\"apply\":{\"phone\":\"" + content + "\"}}";
        mVerifySubscription = LoginWrap.getInstance().sendVerifyCode(loginService, OkHttpUtils.getRequestBody(phone_str), new SimpleObserver<ResponseBody>(){
            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "get verification code failed : " + e.getMessage());
                loginContractView.dismissProgress();
                loginContractView.removeCountDown();
                loginContractView.showToast(mContext.getString(R.string.verify_failed));
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                loginContractView.dismissProgress();
                try {
                    String result = responseBody.string();
                    if (result.contains(mContext.getString(R.string.code_200))){
                        loginContractView.showToast(mContext.getString(R.string.sended));
                        return;
                    }
                    Log.d(TAG, "get verification code success : " + result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loginContractView.showToast(mContext.getString(R.string.verify_failed));
                loginContractView.removeCountDown();
            }
        });
    }

    @Override
    public boolean checkInputText(EditText... editTexts) {
        if (editTexts.length == 2){
            // 监狱端登录输入框验证
            return checkPrisonLoginText(editTexts);
        }else {
            // 3个的话就是普通用户端登录输入框验证
            return checkCommonLoginText(editTexts);
        }
    }

    /**
     * 普通用户登录文本验证
     * @param editTexts 三个输入框  顺序是 手机号  身份证号  验证码
     * @return {@code true}为通过可登录 <br>  {@code true}验证失败
     */
    private boolean checkCommonLoginText(EditText[] editTexts) {
        for (EditText editText : editTexts){
            if (editText == null){
                throw new NullPointerException("check object can not be null");
            }
        }
        String phone_num = editTexts[0].getText().toString().trim();
        String id_num = editTexts[1].getText().toString().trim();
        String verifyCode = editTexts[2].getText().toString().trim();
        if (TextUtils.isEmpty(phone_num)){
            loginContractView.showToast("手机号不能为空");
            return false;
        }
        if (TextUtils.isEmpty(id_num)){
            loginContractView.showToast("身份证号不能为空");
            return false;
        }
        if (TextUtils.isEmpty(verifyCode)){
            loginContractView.showToast("验证码不能为空");
            return false;
        }
        if (RegexUtils.isMobileExact(phone_num)){
            loginContractView.showToast("手机号不存在");
            return false;
        }
        if (!RegexUtils.isIDCard15(id_num) && !RegexUtils.isIDCard18(id_num)){
            loginContractView.showToast("身份证号不存在");
            return false;
        }
        return true;
    }

    /**
     * 监狱用户登录文本验证
     * @param editTexts 两个输入框  顺序是 用户名 密码
     * @return {@code true}为通过可登录 <br>  {@code true}验证失败
     */
    private boolean checkPrisonLoginText(EditText[] editTexts) {
        String username = editTexts[0].getText().toString().trim();
        String password = editTexts[1].getText().toString().trim();
        if (TextUtils.isEmpty(username)){
            loginContractView.showToast(mContext.getString(R.string.username_empty));
            return false;
        }
        if (TextUtils.isEmpty(password)){
            loginContractView.showToast(mContext.getString(R.string.pwd_empty));
            return false;
        }
        return true;
    }

    @Override
    public void attachView(@NonNull LoginContract.View view) {
        loginContractView = view;
    }

    @Override
    public void detachView() {
        if (mVerifySubscription != null && !mVerifySubscription.isUnsubscribed()){
            mVerifySubscription.unsubscribe();
        }
        if (mLoginPersonSubscription != null && !mLoginPersonSubscription.isUnsubscribed()){
            mLoginPersonSubscription.unsubscribe();
        }
        loginContractView = null;
    }

    /**
     * 存sp
     * @param key
     * @param defaultValue
     */
    private void putSP(String key, Object defaultValue){
        SPUtil.put(mContext, key, defaultValue);
    }

    /**
     * 科达sdk登录相关操作
     */
    public class KedaSDKControl{

        // test 普通用户001001305  监狱用户001001227
        private String mAccount = isCommonUser ? "001001305" : "001001227";
        private String mPassword = "";
        private String mAddr = "106.14.18.98";

        private void login() {
            KDInitUtil.isH323 = true;
            if (!KDInitUtil.isH323) {
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
            KDInitUtil.isH323 = isEnable;
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
         * @param isSuccess
         */
        public void loginSuccessed(boolean isSuccess, String failedMsg) {
            loginContractView.dismissProgress();
            if (isSuccess) {
                loginContractView.showToast(mContext.getString(R.string.login_success));
                // 进入下一页
                loginContractView.toNextPage(isCommonUser);
                return;
            }
            if (StringUtils.isNull(failedMsg)) {
                loginContractView.showToast(mContext.getString(R.string.login_failed_retry));
            } else {
                loginContractView.showToast(failedMsg);
            }
        }
    }
}
