package com.gkzxhn.gkprison.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.ui.login.Config;
import com.keda.vconf.manager.VConferenceManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConfigActivity extends AppCompatActivity {

    @BindView(R.id.server_addr)EditText server_addr;
    @BindView(R.id.account)EditText account;
    @BindView(R.id.rate) EditText rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        ButterKnife.bind(this);
    }

    public void save(View view){
        String addr = server_addr.getText().toString().trim();
        String acc = account.getText().toString().trim();
        String mRate = rate.getText().toString().trim();
        if (TextUtils.isEmpty(addr) && TextUtils.isEmpty(acc) && TextUtils.isEmpty(mRate))
            return;
        if (!TextUtils.isEmpty(addr))
            Config.mAddr = addr;
        if (!TextUtils.isEmpty(acc))
            Config.mAccount = acc;
        if (!TextUtils.isEmpty(mRate))
            VConferenceManager.callRate = Integer.parseInt(mRate);
        ToastUtil.showShortToast("保存成功");
    }
}
