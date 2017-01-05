package com.gkzxhn.gkprison.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.ui.login.Config;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConfigActivity extends AppCompatActivity {

    @BindView(R.id.server_addr)EditText server_addr;
    @BindView(R.id.account)EditText account;

    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        ButterKnife.bind(this);
        type = getIntent().getIntExtra("type", 0);// 0 表示普通用户  1表示监狱用户
    }

    public void save(View view){
        String addr = server_addr.getText().toString().trim();
        String acc = account.getText().toString().trim();
        if (TextUtils.isEmpty(addr) && TextUtils.isEmpty(acc))
            return;
        if (type == 0){
            if (!TextUtils.isEmpty(addr))
                Config.mAddr = addr;
            if (!TextUtils.isEmpty(acc))
                Config.mAccount = acc;
        }
        if (type == 1){
            if (!TextUtils.isEmpty(addr))
                Config.mAddr = addr;
            if (!TextUtils.isEmpty(acc))
                Config.mAccount = acc;
        }
        ToastUtil.showShortToast("保存成功");
    }
}
