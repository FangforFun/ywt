package com.gkzxhn.gkprison.userport.activity;

import android.view.View;
import android.widget.ArrayAdapter;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.weiwangcn.betterspinner.library.BetterSpinner;

/**
 * 探监服务--探监申请
 */
public class VisitingServiceActivity extends BaseActivity {

    private BetterSpinner bs_visit_request_time;
    private static final String[] REQUEST_TIME = new String[] {
            "1月5日", "1月6日", "1月7日", "1月8日", "1月9日"
    };
    private ArrayAdapter<String> adapter;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_visiting_service,null);
        bs_visit_request_time = (BetterSpinner) view.findViewById(R.id.bs_visit_request_time);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请探监");
        setBackVisibility(View.VISIBLE);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, REQUEST_TIME);
        bs_visit_request_time.setAdapter(adapter);
    }
}
