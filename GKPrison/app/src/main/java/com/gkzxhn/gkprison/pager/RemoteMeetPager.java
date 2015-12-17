package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

/**
 * Created by hzn on 2015/12/3.
 */
public class RemoteMeetPager extends BasePager {

    private TextView tv_meeting_request_name;
    private TextView tv_meeting_request_id_num;
    private TextView tv_meeting_request_relationship;
    private TextView tv_meeting_request_phone;
    private EditText et_meeting_request_time;
    private Button bt_commit_request;

    public RemoteMeetPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        view = View.inflate(context, R.layout.pager_remote_meeting, null);
        tv_meeting_request_name = (TextView) view.findViewById(R.id.tv_meeting_request_name);
        tv_meeting_request_id_num = (TextView) view.findViewById(R.id.tv_meeting_request_id_num);
        tv_meeting_request_relationship = (TextView) view.findViewById(R.id.tv_meeting_request_relationship);
        tv_meeting_request_phone = (TextView) view.findViewById(R.id.tv_meeting_request_phone);
        et_meeting_request_time = (EditText) view.findViewById(R.id.et_meeting_request_time);
        bt_commit_request = (Button) view.findViewById(R.id.bt_commit_request);
        return view;
    }

    @Override
    public void initData() {
        Drawable drawable1 = context.getResources().getDrawable(R.drawable.down_gray);
        drawable1.setBounds(0, 0, 30, 20);
        et_meeting_request_time.setCompoundDrawables(null, null, drawable1, null);
        bt_commit_request.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_commit_request:
                showToastMsgShort("提交申请");
                break;
        }
    }
}
