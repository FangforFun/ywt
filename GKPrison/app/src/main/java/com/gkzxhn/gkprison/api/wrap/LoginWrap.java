package com.gkzxhn.gkprison.api.wrap;

import com.gkzxhn.gkprison.api.LoginService;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Huang ZN
 * Date: 2016/12/22
 * Email:943852572@qq.com
 * Description:
 */

public class LoginWrap {

    private static LoginWrap instance;

    public static LoginWrap getInstance(){
        if (instance == null)
            instance = new LoginWrap();
        return instance;
    }

    /**
     * 获取验证码
     * @param body json
     * @param subscriber
     * @return
     */
    public Subscription sendVerifyCode(LoginService loginService,
            RequestBody body, SimpleObserver<ResponseBody> subscriber){
        return loginService.getVerificationCode(body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}
