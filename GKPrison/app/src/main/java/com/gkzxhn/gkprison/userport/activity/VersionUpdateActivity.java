package com.gkzxhn.gkprison.userport.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.MyApplication;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.VersionInfo;
import com.gkzxhn.gkprison.userport.service.DownloadService;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * 版本更新页面
 */
public class VersionUpdateActivity extends BaseActivity {

    private ImageView iv_check_update;
    private Button bt_update;// 检查更新&更新按钮
    private TextView tv_version_code;// 当前版本号
    private TextView tv_new_function;// 新功能tv
    private TextView tv_new_version;// 新版本号
    private TextView tv_new_function_contents;// 新版本功能内容
    private RotateAnimation ra;
    private SharedPreferences sp;
    private VersionInfo versionInfo;
    private boolean has_new_version = false;// 是否有新版本
    private TextView tv_progress;
    private boolean isPause = false;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            tv_progress.setText("当前进度 ： " + msg.what + "%");
        }
    };

    private DownloadService.DownloadBinder binder;
    private boolean isBinded;
    private ProgressBar mProgressBar;
    // 获取到下载url后，直接复制给MapApp,里面的全局变量
    private String downloadUrl;
    //
    private boolean isDestroy = true;
    private MyApplication app;
    private ServiceConnection conn;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_version_update, null);
        iv_check_update = (ImageView) view.findViewById(R.id.iv_check_update);
        bt_update = (Button) view.findViewById(R.id.bt_update);
        tv_version_code = (TextView) view.findViewById(R.id.tv_version_code);
        tv_new_function = (TextView) view.findViewById(R.id.tv_new_function);
        tv_new_version = (TextView) view.findViewById(R.id.tv_new_version);
        tv_new_function_contents = (TextView) view.findViewById(R.id.tv_new_function_contents);
        tv_progress = (TextView) view.findViewById(R.id.tv_progress);
        mProgressBar = (ProgressBar) view.findViewById(R.id.mProgressBar);
        return view;
    }

    @Override
    protected void initData() {
        app = (MyApplication) getApplication();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        setTitle("版本更新");
        setBackVisibility(View.VISIBLE);
        bt_update.setOnClickListener(this);
        tv_version_code.setText(getVersionName());
    }

    private Runnable rotateTask = new Runnable() {
        @Override
        public void run() {
            ra = new RotateAnimation(0, -360 * 100, iv_check_update.getWidth()/2, iv_check_update.getHeight()/2);
            ra.setDuration(1500 * 100);
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            ra.setInterpolator(linearInterpolator);// 匀速
            iv_check_update.startAnimation(ra);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        if (isDestroy && app.isDownload()) {
//            Intent it = new Intent(VersionUpdateActivity.this, DownloadService.class);
//            startService(it);
//            bindService(it, conn, Context.BIND_AUTO_CREATE);
//        }
//        System.out.println(" notification  onresume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isDestroy = false;
        System.out.println(" notification  onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBinded) {
            System.out.println(" onDestroy   unbindservice");
            unbindService(conn);
        }
        if (binder != null && binder.isCanceled()) {
            System.out.println(" onDestroy  stopservice");
            Intent it = new Intent(this, DownloadService.class);
            stopService(it);
        }
    }

    private ICallbackResult callback = new ICallbackResult() {
        @Override
        public void OnBackResult(Object result) {
            // TODO Auto-generated method stub
            if ("finish".equals(result)) {
                finish();
                return;
            }
            int i = (Integer) result;
            mProgressBar.setProgress(i);
            handler.sendEmptyMessage(i);
        }
    };

    public interface ICallbackResult {
        public void OnBackResult(Object result);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_update:
                if(!has_new_version) {
                    handler.post(rotateTask);
                    checkNewVersion();
                    bt_update.setClickable(false);// 不可点
                }else {
                    // 更新 www.fushuile.com/dist/android/1.0.0/*.apk
                    showToastMsgShort("正在更新");
                    bt_update.setClickable(false);
                    app.setDownload(true);
                    tv_progress.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    conn = new ServiceConnection() {

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            // TODO Auto-generated method stub
                            isBinded = false;
                        }

                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            // TODO Auto-generated method stub
                            binder = (DownloadService.DownloadBinder) service;
                            System.out.println("服务启动!!!");
                            // 开始下载
                            isBinded = true;
                            binder.addCallback(callback);
                            binder.start();
                        }
                    };
                    if (isDestroy && app.isDownload()) {
                        Intent it = new Intent(VersionUpdateActivity.this, DownloadService.class);
                        startService(it);
                        bindService(it, conn, Context.BIND_AUTO_CREATE);
                    }
                    System.out.println(" notification  onNewIntent");
                }
                break;
        }
    }

    /**
     * 检查新版本
     */
    private void checkNewVersion() {
        //访问服务器检查是否有新版本
        final HttpUtils httpUtils = new HttpUtils();
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                httpUtils.send(HttpRequest.HttpMethod.GET,Constants.URL_HEAD+"versions/last?access_token="+sp.getString("token",""),new RequestCallBack<Object>() {
                    @Override
                    public void onSuccess (ResponseInfo < Object > responseInfo) {
                        Log.i("检查更新成功", responseInfo.result.toString());
                        parseVersionInfo(responseInfo.result.toString());
                    }

                    @Override
                    public void onFailure (HttpException e, String s){
                        Log.i("检查更新失败", e.getMessage() + "-----" + s);
                        bt_update.setEnabled(true);
                        ra.cancel();
                    }
                });
            }
        }.start();
    }

    /**
     * 解析版本信息
     * @param result
     */
    private void parseVersionInfo(String result) {
        Gson gson = new Gson();
        versionInfo = gson.fromJson(result, VersionInfo.class);
        Log.i("版本信息", versionInfo.toString());
        int current_version_name = getVersionCode();
        if(current_version_name < versionInfo.getVersion_code()){
            // 有新版本
            has_new_version = true;
            ra.cancel();
            bt_update.setClickable(true);
            bt_update.setText("点击更新");
            tv_new_version.setVisibility(View.VISIBLE);
            tv_new_function_contents.setVisibility(View.VISIBLE);
            tv_new_function.setVisibility(View.VISIBLE);
            tv_new_version.setText("新版本：" + versionInfo.getVersion_name());
            tv_new_function.setText("新版本功能:");
            String contents = versionInfo.getContents();
            if(contents.contains("|")) {
                tv_new_function_contents.setText(contents.replace("|", "\n"));
            }else {
                tv_new_function_contents.setText(contents);
            }
        }else {
            // 没有新版本
            tv_new_function.setVisibility(View.VISIBLE);
            bt_update.setClickable(true);
            bt_update.setText("点击更新");
            tv_new_function.setText("已经是最新版本!");
            ra.cancel();
        }
    }

    /**
     * 得到versionCode
     *
     * @return
     */
    public int getVersionCode() {
        // 包管理器
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packInfo = pm.getPackageInfo(getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 得到versionCode
     *
     * @return
     */
    public String getVersionName() {
        // 包管理器
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packInfo = pm.getPackageInfo(getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
