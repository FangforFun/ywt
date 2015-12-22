package com.gkzxhn.gkprison.userport.activity;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

/**
 * 探监服务--探监申请
 */
public class VisitingServiceActivity extends BaseActivity {

    private EditText et_visit_request_time;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_visiting_service,null);
        et_visit_request_time = (EditText) view.findViewById(R.id.et_visit_request_time);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("申请探监");
        setBackVisibility(View.VISIBLE);
        Drawable drawable1 = getResources().getDrawable(R.drawable.down_gray);
        drawable1.setBounds(0, 0, 30, 20);
        et_visit_request_time.setCompoundDrawables(null, null, drawable1, null);
    }
}
