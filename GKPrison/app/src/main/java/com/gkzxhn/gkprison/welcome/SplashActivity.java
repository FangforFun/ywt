package com.gkzxhn.gkprison.welcome;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.InputPasswordActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.userport.activity.VersionUpdateActivity;
import com.gkzxhn.gkprison.userport.bean.VersionInfo;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SplashActivity extends BaseActivity {

    private SharedPreferences sp;
    private VersionInfo versionInfo;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_splash,null);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isFirst = sp.getBoolean("is_first", true);
                Intent intent;
                if (!sp.getBoolean("isLock", false)) {
                    if (isFirst) {
                        intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                    } else {
                        if (TextUtils.isEmpty(sp.getString("username", "")) || TextUtils.isEmpty(sp.getString("password", ""))) {
                            intent = new Intent(SplashActivity.this, LoadingActivity.class);
                            startActivity(intent);
                        } else {

                            if (sp.getBoolean("isCommonUser", true)) {
                                intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                intent = new Intent(SplashActivity.this, DateMeetingListActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                } else {
                    intent = new Intent(SplashActivity.this, InputPasswordActivity.class);
                    startActivity(intent);
                }
                SplashActivity.this.finish();
            }
        }, 1500);
        setActionBarGone(View.GONE);
        if(sp.getBoolean("isRegisteredUser", false)) {
            checkNewVersion();
        }
        copyDB("chaoshi.db");
    }

    /**
     * 检查是否有新版本
     */
    private void checkNewVersion() {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.GET, Constants.URL_HEAD + "versions/last?access_token=" + sp.getString("token", ""), new RequestCallBack<Object>() {
            @Override
            public void onSuccess(ResponseInfo<Object> responseInfo) {
                Log.i("检查更新成功", responseInfo.result.toString());
                parseVersionInfo(responseInfo.result.toString());
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i("检查更新失败", e.getMessage() + "-----" + s);
            }
        });
    }

    /**
     * 解析版本信息
     * @param result
     */
    private void parseVersionInfo(String result) {
        Gson gson = new Gson();
        versionInfo = gson.fromJson(result, VersionInfo.class);
        Log.i("版本信息", versionInfo.toString());
        int current_version_name = SystemUtil.getVersionCode(getApplicationContext());
        if(current_version_name < versionInfo.getVersion_code()){
            // 有新版本
            sendNotification(this);
        }else {
            // 没有新版本
        }
    }

    /**
     * 发送通知
     */
    public void sendNotification(Context context){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, VersionUpdateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("发现新版本，点击更新")
                .setContentTitle("狱务通提醒")
                .setContentText("狱务通发现新的版本，点击查看详情")
                .setContentIntent(pendingIntent).setNumber(1).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        manager.notify(2, notification);
    }

    /**
     * 拷贝数据库
     * @param dbName
     */
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
