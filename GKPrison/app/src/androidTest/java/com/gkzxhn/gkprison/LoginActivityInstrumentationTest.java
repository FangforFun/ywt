package com.gkzxhn.gkprison;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.gkzxhn.gkprison.login.LoadingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Author: Huang ZN
 * Date: 2016/11/28
 * Email:943852572@qq.com
 * Description:
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityInstrumentationTest {

    @Rule
    public ActivityTestRule<LoadingActivity> mActivityRule =
            new ActivityTestRule<>(LoadingActivity.class);

    @Test
    public void loginPersonalAccount(){
    }
}
