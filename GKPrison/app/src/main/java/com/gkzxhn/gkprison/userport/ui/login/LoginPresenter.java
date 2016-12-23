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
import com.gkzxhn.gkprison.utils.Log;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Subscription;

/**
 * Author: Huang ZN
 * Date: 2016/12/22
 * Email:943852572@qq.com
 * Description:
 */
@PerActivity
public class LoginPresenter implements LoginContract.Presenter {

    private static final String TAG = LoginPresenter.class.getName();
    private LoginContract.View loginContractView;
    private Context mContext;
    private LoginService loginService;

    private Subscription mVerifySubscription;

    @Inject
    public LoginPresenter(LoginService loginService, Context context){
        this.mContext = context;
        this.loginService = loginService;
    }

    @Override
    public void login() {

    }

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
        loginContractView.showProgress();
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
    public void attachView(@NonNull LoginContract.View view) {
        loginContractView = view;
    }

    @Override
    public void detachView() {
        if (mVerifySubscription != null && !mVerifySubscription.isUnsubscribed()){
            mVerifySubscription.unsubscribe();
        }
        loginContractView = null;
    }
}
