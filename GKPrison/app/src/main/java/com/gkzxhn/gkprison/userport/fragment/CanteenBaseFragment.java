package com.gkzxhn.gkprison.userport.fragment;

import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragmentNew;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/2.
 * function:电子商务最底层fragment
 */
public class CanteenBaseFragment extends BaseFragmentNew {

    @Override
    protected void initUiAndListener(View view) {

    }

    @Override
    protected int setLayoutResId() {
        return R.layout.fragment_cannteen;
    }

    @Override
    protected void initData() {
        CanteenFragment canteenFragment = new CanteenFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_canteen,canteenFragment)
                .commit();
    }
}
