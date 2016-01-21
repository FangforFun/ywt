package com.gkzxhn.gkprison.userport.activity;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.Utils;
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
    private Button bt_update;
    private TextView tv_version_code;
    private TextView tv_new_function;
    private Handler handler = new Handler();
    private RotateAnimation ra;

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_version_update, null);
        iv_check_update = (ImageView) view.findViewById(R.id.iv_check_update);
        bt_update = (Button) view.findViewById(R.id.bt_update);
        tv_version_code = (TextView) view.findViewById(R.id.tv_version_code);
        tv_new_function = (TextView) view.findViewById(R.id.tv_new_function);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("版本更新");
        setBackVisibility(View.VISIBLE);
        bt_update.setOnClickListener(this);
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
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_update:
                handler.post(rotateTask);
                checkNewVersion();
                break;
        }
    }

    /**
     * 检查新版本
     */
    private void checkNewVersion() {
        //访问服务器检查是否有新版本
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.GET, Constants.URL_HEAD + "version", new RequestCallBack<Object>() {
            @Override
            public void onSuccess(ResponseInfo<Object> responseInfo) {
                Log.i("检查更新成功", responseInfo.result.toString());
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i("检查更新失败", e.getMessage() + "-----" + s);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null) {
            handler.removeCallbacks(rotateTask);
            handler = null;
        }
    }
}
