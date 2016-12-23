package com.gkzxhn.gkprison.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.InputPasswordActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.bean.VersionInfo;
import com.gkzxhn.gkprison.userport.db.SQLiteHelper;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SystemUtil;

/**
 * created by huangzhengneng on 2015/12/22
 * 启动页
 */
public class SplashActivity extends BaseActivity {

    private SharedPreferences sp;
    private VersionInfo versionInfo;
    private TextView tv_version;
    private RelativeLayout rl_splash;
    private SQLiteHelper help ;
    private SQLiteDatabase db ;
    private TextView tv1;
    private TextView tv2;
    private int[] screenWidthHeight;
  //  private boolean tabexit;//判断数据库是否存在
    private SQLiteHelper sqLitehelp;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_splash,null);
        tv_version = (TextView) view.findViewById(R.id.tv_version);
        rl_splash = (RelativeLayout) view.findViewById(R.id.rl_splash);
        tv1 = (TextView) view.findViewById(R.id.tv1);
        tv2 = (TextView) view.findViewById(R.id.tv2);
        if(SystemUtil.isTablet(this)){ // 平板
            screenWidthHeight = DensityUtil.getScreenWidthHeight(this);
            Log.i("screenWidthHeight is : ", screenWidthHeight[0] + "---" + screenWidthHeight[1]);
            if(screenWidthHeight[0] == 1280 && screenWidthHeight[1] == 720) { // orange pi
                rl_splash.setBackgroundResource(R.drawable.splash_tablet);
                tv1.setTextSize(20);
                tv2.setTextSize(20);
                tv_version.setTextSize(20);
            }else if(screenWidthHeight[0] == 1536 && screenWidthHeight[1] == 2048){ // 小米平板的尺寸
                rl_splash.setBackgroundResource(R.drawable.splash_common_tablet);
                tv1.setTextSize(16);
                tv2.setTextSize(16);
                tv_version.setTextSize(16);
            }else {
                // 默认平板
                rl_splash.setBackgroundResource(R.drawable.splash);
                tv1.setTextSize(16);
                tv2.setTextSize(16);
                tv_version.setTextSize(16);
            }
        }else { // 手机
            rl_splash.setBackgroundResource(R.drawable.splash);
            tv1.setTextSize(12);
            tv2.setTextSize(12);
            tv_version.setTextSize(12);
        }
        return view;
    }

    @Override
    protected void initData() {
        help = new SQLiteHelper(this);
        db = help.getWritableDatabase();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        tv_version.setText("V " + SystemUtil.getVersionName(this));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isFirst = sp.getBoolean("is_first", true);
                Intent intent;
                if (!sp.getBoolean("isLock", false)) { // 是否加锁
                    if (isFirst) { // 是否是第一次进入应用
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
    }
}
