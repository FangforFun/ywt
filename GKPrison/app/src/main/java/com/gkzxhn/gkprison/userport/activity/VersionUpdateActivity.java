package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.VersionInfo;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;


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
    private TextView tv_progress;
    private ProgressBar pb_update;
    private boolean has_new_version = false;// 是否有新版本
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_version_update, null);
        iv_check_update = (ImageView) view.findViewById(R.id.iv_check_update);
        bt_update = (Button) view.findViewById(R.id.bt_update);
        tv_version_code = (TextView) view.findViewById(R.id.tv_version_code);
        tv_new_function = (TextView) view.findViewById(R.id.tv_new_function);
        tv_new_version = (TextView) view.findViewById(R.id.tv_new_version);
        tv_new_function_contents = (TextView) view.findViewById(R.id.tv_new_function_contents);
        return view;
    }

    @Override
    protected void initData() {
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
    protected void onDestroy() {
        super.onDestroy();
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
                    showUpdateDialog();
                }
                break;
        }
    }

    /**
     * 更新对话框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VersionUpdateActivity.this);
        builder.setCancelable(false);
        View update_view = View.inflate(VersionUpdateActivity.this, R.layout.update_dialog, null);
        tv_progress = (TextView) update_view.findViewById(R.id.tv_progress);
        pb_update = (ProgressBar) update_view.findViewById(R.id.pb_update);
        builder.setView(update_view);
        AlertDialog dialog = builder.create();
        dialog.show();
        /**
         * Intent intent = new Intent();
         intent.setAction("android.intent.action.VIEW");
         Uri content_url = Uri.parse(url);
         intent.setData(content_url);
         startActivity(intent);
         */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //sd卡可用，用于存放下载的apk
            //1.下载
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.download(Constants.NEW_VERSION_APK_URL, Environment.getExternalStorageDirectory() + "/ywt_newVersion.apk", new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    //2.安装apk
                    Toast.makeText(VersionUpdateActivity.this, "下载成功...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/ywt_newVersion.apk")),
                            "application/vnd.android.package-archive");
                    startActivity(intent);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    e.printStackTrace();
                    Log.i("版本更新...", e.getMessage() + "----" + s);
                    Toast.makeText(VersionUpdateActivity.this, "网络不好，下载失败啦", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    pb_update.setMax(100);
                    int progress = (int) (current * 100 / total);
                    pb_update.setProgress(progress);
                    tv_progress.setText(progress + "%");
                    Log.i("下载进度", current + "----" +  progress + "---" + total);
                }
            });
        } else {
            //sd卡不可用
            Toast.makeText(VersionUpdateActivity.this, "sdcard不可用, 下载失败", Toast.LENGTH_SHORT).show();
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
            bt_update.setText("检查更新");
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
