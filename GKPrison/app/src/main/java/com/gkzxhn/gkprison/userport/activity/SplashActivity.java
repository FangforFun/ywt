package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SplashActivity extends BaseActivity {

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_splash,null);
        return view;
    }

    @Override
    protected void initData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoadingActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 1500);
        setActionBarGone(View.GONE);
        copyDB("chaoshi.db");
    }

    private void copyDB(String dbName) {
        File file = new File(getFilesDir(), dbName);
        if (file.exists() && file.length() > 0) {
            System.out.println("数据库已存在");
        } else {
            try {
                InputStream is = getAssets().open(dbName);
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] b = new byte[1024];
                while ((len = is.read(b)) != -1) {
                    fos.write(b, 0, len);
                }
                is.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
