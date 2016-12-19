package com.gkzxhn.gkprison.login;

import com.gkzxhn.gkprison.utils.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Author: Huang ZN
 * Date: 2016/11/30
 * Email:943852572@qq.com
 * Description:注册页面相关方法测试
 */
public class RegisterActivityTest {

    private static final String TAG = "RegisterActivityTest";
    private RegisterActivity register;

    public RegisterActivityTest() {
        register = new RegisterActivity();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void onBackPressed() throws Exception {

    }

    @Test
    public void judgeIDCard() throws Exception {
        if (register.judgeIDCard("430482199404073618")){
            Log.d(TAG, "id is ok");
        }else {
            Log.d(TAG, "id is not ok");
        }
    }
}