package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.VersionInfo;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 版本更新页面
 */
public class VersionUpdateActivity extends BaseActivityNew{

    private static final java.lang.String TAG = "VersionUpdateActivity";
    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.rl_back) RelativeLayout rl_back;
    @BindView(R.id.iv_check_update) ImageView iv_check_update;
    @BindView(R.id.bt_update) Button bt_update;// 检查更新&更新按钮
    @BindView(R.id.tv_version_code) TextView tv_version_code;// 当前版本号
    @BindView(R.id.tv_new_function) TextView tv_new_function;// 新功能tv
    @BindView(R.id.tv_new_version) TextView tv_new_version;// 新版本号
    @BindView(R.id.tv_new_function_contents) TextView tv_new_function_contents;// 新版本功能内容
    private RotateAnimation ra;
    private VersionInfo versionInfo;
    private TextView tv_progress;
    private ProgressBar pb_update;
//    private DownloadProgressBar dpv_update;
    private AlertDialog dialog;//升级对话框
    private boolean has_new_version = false;// 是否有新版本
    private boolean download_successed = false;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        }
    };

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
    public int setLayoutResId() {
        return R.layout.activity_version_update;
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        tv_title.setText(R.string.vs_update);
        rl_back.setVisibility(View.VISIBLE);
        tv_version_code.setText(SystemUtil.getVersionName(getApplicationContext()));
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.bt_update, R.id.rl_back})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_update:
                if(!has_new_version) {
                    handler.post(rotateTask);
                    checkNewVersion();
                    bt_update.setClickable(false);// 不可点
                }else {
                    bt_update.setClickable(false);
                    showUpdateDialog();
                }
                break;
            case R.id.rl_back:
                finish();
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
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //sd卡可用，用于存放下载的apk
            //1.下载
            HttpUtils httpUtils = new HttpUtils();
            String APK_URL = Constants.NEW_VERSION_APK_URL + "yuwutong.apk";
            Log.i(TAG, APK_URL);
            File file = new File(Environment.getExternalStorageDirectory() + "/yuwutong-" + versionInfo.getVersion_name() + ".apk");
            // 若文件已下载则直接安装
            if(!file.exists()) {
                httpUtils.download(APK_URL, Environment.getExternalStorageDirectory() + "/yuwutong-" + versionInfo.getVersion_name() + ".apk", new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        //2.安装apk
                        download_successed = true;
                        Log.i("变啦", "变啦" + download_successed);
                        dialog.dismiss();
                        handler.postDelayed(install_apk_task, 1000);
                        bt_update.setClickable(true);
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        e.printStackTrace();
                        Log.i("版本更新...", e.getMessage() + "----" + s);
                        Toast.makeText(VersionUpdateActivity.this, "网络不好，下载失败啦", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        bt_update.setClickable(true);
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        super.onLoading(total, current, isUploading);
                        int progress = (int) (current * 100 / total);
                        pb_update.setMax(100);
                        pb_update.setProgress(progress);
                        tv_progress.setText(progress + "%");
                        Log.i("下载进度", current + "----" + progress + "---" + total);
                    }
                });
            }else {
                handler.postDelayed(install_apk_task, 1000);
            }
        } else {
            //sd卡不可用
            Toast.makeText(VersionUpdateActivity.this, "sdcard不可用, 下载失败", Toast.LENGTH_SHORT).show();
        }
        builder.setView(update_view);
        dialog = builder.create();
        dialog.show();
        /**
         * Intent intent = new Intent();
         intent.setAction("android.intent.action.VIEW");
         Uri content_url = Uri.parse(url);
         intent.setData(content_url);
         startActivity(intent);
         */
    }

    /**
     * 安装apk任务
     */
    private Runnable install_apk_task = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/ywt_newVersion.apk")),
                    "application/vnd.android.package-archive");
            startActivity(intent);
        }
    };

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
                httpUtils.send(HttpRequest.HttpMethod.GET,Constants.URL_HEAD+"versions/last",new RequestCallBack<Object>() {
                    @Override
                    public void onSuccess (ResponseInfo < Object > responseInfo) {
                        Log.i("检查更新成功", responseInfo.result.toString());
                        parseVersionInfo(responseInfo.result.toString());
                        bt_update.setClickable(true);
                    }

                    @Override
                    public void onFailure (HttpException e, String s){
                        Log.i("检查更新失败", e.getMessage() + "-----" + s);
                        bt_update.setClickable(true);
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
        int current_version_name = SystemUtil.getVersionCode(getApplicationContext());
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
}
