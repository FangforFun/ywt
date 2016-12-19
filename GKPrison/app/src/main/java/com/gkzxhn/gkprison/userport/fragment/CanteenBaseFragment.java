package com.gkzxhn.gkprison.userport.fragment;

import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.base.BaseFragment;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/2.
 * function:电子商务最底层fragment
 */
public class CanteenBaseFragment extends BaseFragment {

    @Override
    protected View initView() {
        return View.inflate(context, R.layout.fragment_cannteen, null);
    }

    @Override
    protected void initData() {
        CanteenFragment canteenFragment = new CanteenFragment();
        ((BaseActivity)context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_canteen,canteenFragment)
                .commit();
    }
}
