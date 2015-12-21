package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.activity.BaseActivity;
import com.gkzxhn.gkprison.fragment.CanteenFragment;

/**
 * Created by hzn on 2015/12/3.
 */
public class CanteenPager extends BasePager{

    public CanteenPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_cannteen, null);
        return view;
    }

    @Override
    public void initData() {
        CanteenFragment canteenFragment = new CanteenFragment();
        ((BaseActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_canteen,canteenFragment).commit();
    }
}
