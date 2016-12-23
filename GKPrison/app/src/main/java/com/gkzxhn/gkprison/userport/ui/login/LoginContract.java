package com.gkzxhn.gkprison.userport.ui.login;

import android.widget.EditText;

import com.gkzxhn.gkprison.base.BasePresenter;
import com.gkzxhn.gkprison.base.BaseView;

/**
 * Author: Huang ZN
 * Date: 2016/12/22
 * Email:943852572@qq.com
 * Description:
 */

public interface LoginContract {

    interface View extends BaseView{
        void showMainUi();

        void toRegister();

        void toLoginWithoutAccount();

        void showProgress();

        void dismissProgress();

        void showFailed(String msg);

        void showSuccess(String msg);

        void showToast(String msg);

        void startCountDown();

        void removeCountDown();
    }

    interface Presenter extends BasePresenter<View>{

        void login();

        void sendVerifyCode(EditText editText);
    }
}
