package com.gkzxhn.gkprison;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
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
        Espresso.onView(ViewMatchers.withId(R.id.et_login_username))
                .perform(ViewActions.typeText("18774810958"),
                        ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_login_ic_card_num))
                .perform(ViewActions.typeText("430482199404073618"),
                        ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withText("登    录")).perform(ViewActions.click());
    }
}
