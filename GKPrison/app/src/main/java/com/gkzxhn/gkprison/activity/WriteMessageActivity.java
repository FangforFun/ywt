package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;

public class WriteMessageActivity extends BaseActivity {

    private EditText et_theme;
    private EditText et_content;
    private Button bt_commit_write_message;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_write_message, null);
        et_theme = (EditText) view.findViewById(R.id.et_theme);
        et_content = (EditText) view.findViewById(R.id.et_content);
        bt_commit_write_message = (Button) view.findViewById(R.id.bt_commit_write_message);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("写信");
        setBackVisibility(View.VISIBLE);
        bt_commit_write_message.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_commit_write_message:
                showToastMsgShort("提交...");
                break;
        }
    }
}
