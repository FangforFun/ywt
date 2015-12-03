package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.view.View;

import com.gkzxhn.gkprison.R;

/**
 * Created by hzn on 2015/12/3.
 */
public class VisitPager extends BasePager {

    public VisitPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_visit, null);
        return view;
    }

    @Override
    public void initData() {

    }
}
