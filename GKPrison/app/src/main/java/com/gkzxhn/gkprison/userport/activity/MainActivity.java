package com.gkzxhn.gkprison.userport.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.base.BasePager;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.login.adapter.AutoTextAdapater;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.db.SQLitehelp;
import com.gkzxhn.gkprison.userport.event.MeetingTimeEvent;
import com.gkzxhn.gkprison.userport.fragment.MenuFragment;
import com.gkzxhn.gkprison.userport.pager.CanteenPager;
import com.gkzxhn.gkprison.userport.pager.HomePager;
import com.gkzxhn.gkprison.userport.pager.RemoteMeetPager;
import com.gkzxhn.gkprison.userport.view.CustomDrawerLayout;
import com.gkzxhn.gkprison.userport.view.LazyViewPager;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.Utils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * created by huangzhengneng on 2015/12/22
 * 主activity
 *
 *
 *                                  _ooOoo_
                                   o8888888o
                                   88" . "88
                                   (| -_- |)
                                   O\  =  /O
                                ____/`---'\____
                              .'  \\|     |//  `.
                             /  \\|||  :  |||//  \
                            /  _||||| -:- |||||-  \
                           |   | \\\  -  /// |    |
                           | \_|  ''\---/''  |   |
                           \  .-\__  `-`  ___/-. /
                         ___`. .'  /--.--\  `. . __
                       ."" '<  `.___\_<|>_/___.'  >'"".
                      | | :  `- \`.;`\ _ /`;.`/ - ` : | |
                      \  \ `-.   \_ __\ /__ _/   .-` /  /
                 ======`-.____`-.___\_____/___.-`____.-'======
                                    `=---='
                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                            佛祖保佑       永无BUG
 */
public class MainActivity extends BaseActivity {
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/databases/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private List<Commodity> commodityList = new ArrayList<>();
    private LazyViewPager home_viewPager;
    private RadioGroup rg_bottom_guide; // 底部导航栏组
    private List<BasePager> pagerList;
    private RadioButton rb_bottom_guide_home; // 首页
    private RadioButton rb_bottom_guide_visit; // 探监
    private RadioButton rb_bottom_guide_canteen; // 电子商务
    private long mExitTime;//add by hzn 退出按键时间间隔
    private CustomDrawerLayout drawerLayout; // 侧拉菜单
    private ActionBarDrawerToggle toggle;
    private FrameLayout fl_drawer; // 侧拉菜单底层帧布局
    private SharedPreferences sp;
    private boolean isRegisteredUser; // 是否注册登录用户
    private MyPagerAdapter adapter;
    private String times;
    private AutoCompleteTextView actv_prison_choose; // 监狱选择
    private AutoTextAdapater autoTextAdapater;
    private String data; // 监狱选择访问服务器返回的字符串
    private List<String> suggest;// 自动提示的集合
    private Map<String, Integer> prison_map; // 服务器返回的监狱列表存储需要的集合
    private int jail_id;  // 监狱id
    private String url; // 商品列表url
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/xml; charset=utf-8");

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String m = (String)msg.obj;
                    if (m.equals("success")){
                        Bundle bundle = msg.getData();
                        String commodity = bundle.getString("result");
                        commodityList = analysiscommodity(commodity);
                        if (commodityList.size() != 0){
                            String sql = "delete from Items where 1=1";
                            db.execSQL(sql);
                            for (int i = 0;i < commodityList.size();i++){
                                String sql1 = "insert into Items (id,title,description,price,avatar_url,category_id,ranking) values ("+commodityList.get(i).getId()+",'"+commodityList.get(i).getTitle()+"','"+commodityList.get(i).getDescription()+"','"+ commodityList.get(i).getPrice()+"','"+ commodityList.get(i).getAvatar_url()+"',"+commodityList.get(i).getCategory_id()+","+commodityList.get(i).getRanking()+")";
                                db.execSQL(sql1);
                            }
                        }
                    }else if (m.equals("error")){
                        Toast.makeText(getApplicationContext(),"同步数据失败",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:// 无账号快捷登录监狱选择没有网络
                    showToastMsgShort("没有网络,请检查网络设置");
                    break;
                case 3:
                    pagerList.clear();
                    pagerList.add(new HomePager(MainActivity.this));
                    pagerList.add(new RemoteMeetPager(MainActivity.this));
                    pagerList.add(new CanteenPager(MainActivity.this));
                    layoutMain();
                    break;
                case 4: // 获取用户信息失败   提示重新登录
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("获取用户信息失败，请重新登录");
                    builder.setCancelable(false);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(isRegisteredUser) {
                                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.clear();
                                editor.commit();
                                startActivity(intent);
                                NIMClient.getService(AuthService.class).logout();
                            }else {
                                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                    builder.create().show();
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_main, null);
        home_viewPager = (LazyViewPager) view.findViewById(R.id.layout_content);
        rg_bottom_guide = (RadioGroup) view.findViewById(R.id.rg_bottom_guide);
        rb_bottom_guide_home = (RadioButton) view.findViewById(R.id.rb_bottom_guide_home);
        rb_bottom_guide_visit = (RadioButton) view.findViewById(R.id.rb_bottom_guide_visit);
        rb_bottom_guide_canteen = (RadioButton) view.findViewById(R.id.rb_bottom_guide_canteen);
        drawerLayout = (CustomDrawerLayout) view.findViewById(R.id.drawer_layout);
        fl_drawer = (FrameLayout) view.findViewById(R.id.fl_drawer);
        Drawable[] drawables = rb_bottom_guide_home.getCompoundDrawables();
        drawables[1].setBounds(0, DensityUtil.dip2px(getApplicationContext(), 5), getResources().getDimensionPixelSize(R.dimen.home_tab_width), getResources().getDimensionPixelSize(R.dimen.home_tab_height));
        rb_bottom_guide_home.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        Drawable[] drawables2 = rb_bottom_guide_visit.getCompoundDrawables();
        drawables2[1].setBounds(0, DensityUtil.dip2px(getApplicationContext(), 5), getResources().getDimensionPixelSize(R.dimen.home_tab_width), getResources().getDimensionPixelSize(R.dimen.home_tab_height));
        rb_bottom_guide_visit.setCompoundDrawables(drawables2[0], drawables2[1], drawables2[2], drawables2[3]);
        Drawable[] drawables3 = rb_bottom_guide_canteen.getCompoundDrawables();
        drawables3[1].setBounds(0, DensityUtil.dip2px(getApplicationContext(), 5), getResources().getDimensionPixelSize(R.dimen.home_tab_width), getResources().getDimensionPixelSize(R.dimen.home_tab_height));
        rb_bottom_guide_canteen.setCompoundDrawables(drawables3[0], drawables3[1], drawables3[2], drawables3[3]);
        return view;
    }

    public void onEvent(MeetingTimeEvent event){
        RemoteMeetPager remoteMeetPager = (RemoteMeetPager) pagerList.get(1);
        remoteMeetPager.setLastMeetingTime();
    }

    /**
     * 重新登录任务
     */
    private Runnable reLoginTask = new Runnable() {
        @Override
        public void run() {
            RequestCallback callback = new RequestCallback() {
                @Override
                public void onSuccess(Object o) {
                    Log.i("MainActivity", "MainActivity重新登录了");
                }

                @Override
                public void onFailed(int i) {
                    switch (i) {
                        case 302:
                            Toast.makeText(MainActivity.this, "手机号或者身份证号错误", Toast.LENGTH_SHORT).show();
                            break;
                        case 503:
                            Toast.makeText(MainActivity.this, "服务器繁忙", Toast.LENGTH_SHORT).show();
                            break;
                        case 415:
                            Toast.makeText(MainActivity.this, "网络出错，请检查网络", Toast.LENGTH_SHORT).show();
                            break;
                        case 408:
                            Toast.makeText(MainActivity.this, "请求超时，请稍后再试", Toast.LENGTH_SHORT).show();
                            break;
                        case 403:
                            Toast.makeText(MainActivity.this, "非法操作或没有权限", Toast.LENGTH_SHORT).show();
                            break;
                        case 200:
                            Toast.makeText(MainActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 422:
                            Toast.makeText(MainActivity.this, "您的账号已被禁用", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(MainActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    showToastMsgShort("登录异常");
                    Log.i("MainActivity", "MainActivity重新登录异常" + throwable.getMessage());
                }
            };
            LoginInfo info = new LoginInfo(sp.getString("token", ""), sp.getString("token", "")); // config...
            NIMClient.getService(AuthService.class).login(info)
                    .setCallback(callback);
        }
    };

    @Override
    protected void initData() {
        StatusCode statusCode = NIMClient.getStatus();
        Log.i("云信id状态...", statusCode.toString());
        sp = getSharedPreferences("config", MODE_PRIVATE);
        isRegisteredUser = sp.getBoolean("isRegisteredUser", false);
        if(isRegisteredUser && statusCode != StatusCode.LOGINED && statusCode != StatusCode.KICKOUT){
            // 如果是注册用户并且不是被其他端踢掉的未上线就重新登录
            handler.post(reLoginTask);
        }
        pagerList = new ArrayList<>();
        if(isRegisteredUser) {
            getUserInfo();// 获取当前登录用户的信息
            if(sp.getBoolean("has_new_notification", false)){
                view_red_point.setVisibility(View.VISIBLE);
            }
            times = getIntent().getStringExtra("times");
            if (times != null){
                final String url = "https://api.mch.weixin.qq.com/pay/orderquery";
                final String str = getXml();
                new Thread(){
                    @Override
                    public void run() {
                        RequestBody body = RequestBody.create(JSON, str);
                        Request request = new Request.Builder().url(url).post(body).build();
                        try {
                            Response response = client.newCall(request).execute();
                            String result = response.body().string();
                            Log.d("MainActivity",result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                String type = "微信支付";
                String sql = "update Cart set isfinish = 1,payment_type = '"+type+"' where time = '"+times+"'";
                db.execSQL(sql);
            }
        }else {
            pagerList.clear();
            pagerList.add(new HomePager(this));
            prison_map = new HashMap<>();
            showPrisonDialog();// 弹出监狱选择框
        }
        if(statusCode == StatusCode.KICKOUT){
            showKickoutDialog();// 其他设备登录
        }
        setMessageVisibility(View.VISIBLE);
        setSupportActionBar(tool_bar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.icon_menu, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_drawer, menuFragment, "MENU").commit();
        rl_home_menu.setOnClickListener(this);
        rl_message.setOnClickListener(this);
    }

    /**
     * 布局
     */
    private void layoutMain() {
        adapter = new MyPagerAdapter();
        home_viewPager.setAdapter(adapter);
        rg_bottom_guide.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_bottom_guide_home: // 首页
                        home_viewPager.setCurrentItem(0);
                        setTitle("首页");
                        setMenuVisibility(View.VISIBLE);
                        setActionBarGone(View.VISIBLE);
                        setMessageVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_bottom_guide_visit: // 远程会见
                        if (isRegisteredUser) {
                            home_viewPager.setCurrentItem(1);
                            setTitle("探监");
                            setMenuVisibility(View.GONE);
                            setActionBarGone(View.VISIBLE);
                            setMessageVisibility(View.GONE);
                        } else {
                            showToastMsgShort(getString(R.string.enable_logined));
                        }
                        break;
                    case R.id.rb_bottom_guide_canteen: // 小卖部
                        if (isRegisteredUser) {
                            home_viewPager.setCurrentItem(2);
                            setTitle("电子商务");
                            setMenuVisibility(View.GONE);
                            setActionBarGone(View.VISIBLE);
                            setMessageVisibility(View.GONE);
                        } else {
                            showToastMsgShort(getString(R.string.enable_logined));
                        }
                        break;
                }
            }
        });
        rg_bottom_guide.check(R.id.rb_bottom_guide_home); // 默认选择首页
        home_viewPager.setOnPageChangeListener(new LazyViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_home);
                    setTitle("首页");
                    setMenuVisibility(View.VISIBLE);
                    setActionBarGone(View.VISIBLE);
                    setMessageVisibility(View.VISIBLE);
                } else if (position == 1) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_visit);
                    setTitle("探监");
                    setMenuVisibility(View.GONE);
                    setActionBarGone(View.VISIBLE);
                    setMessageVisibility(View.GONE);
                } else if (position == 2) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_canteen);
                    setTitle("电子商务");
                    setMenuVisibility(View.GONE);
                    setActionBarGone(View.VISIBLE);
                    setMessageVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 无账号快捷登录进入首页弹出对话框选择监狱
     *   不能不选
     */
    private void showPrisonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final View prison_choose = View.inflate(this, R.layout.prison_choose_dialog, null);
        builder.setView(prison_choose);
        Button bt_ok = (Button) prison_choose.findViewById(R.id.bt_ok);
        actv_prison_choose = (AutoCompleteTextView) prison_choose.findViewById(R.id.actv_prison_choose);
        actv_prison_choose.setThreshold(1);
        actv_prison_choose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new GetSuggestData().execute(newText);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final AlertDialog dialog = builder.create();
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = actv_prison_choose.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    showToastMsgShort("请输入监狱名称");
                    return;
                } else {
                    if (prison_map.containsKey(content)) {
                        int jail_id = prison_map.get(content);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("jail_id", jail_id);
                        editor.commit();
                        dialog.dismiss();
                        layoutMain();// 布局
                    } else {
                        showToastMsgShort("抱歉，暂未开通此监狱");
                        return;
                    }
                }
            }
        });
        dialog.show();
    }

    /**
     * 监狱提示任务
     */
    private class GetSuggestData extends AsyncTask<String,String,String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            autoTextAdapater = new AutoTextAdapater(suggest, MainActivity.this);
            actv_prison_choose.setAdapter(autoTextAdapater);
        }

        @Override
        protected String doInBackground(String... key) {
            String newText = key[0];
            newText = newText.trim();
            newText = newText.replace(" ", "+");
            suggest = new ArrayList<>();
            if(Utils.isNetworkAvailable()) {
                try {
                    data = HttpRequestUtil.doHttpsGet(Constants.URL_HEAD + "jails/" + newText);
                    Log.i("监狱。。。。", data);
                    prison_map.clear();
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray jArray = jsonObject.getJSONArray("jails");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jsonObject1 = jArray.getJSONObject(i);
                        String suggestKey = jsonObject1.getString("title");
                        int id = jsonObject1.getInt("id");
                        suggest.add(suggestKey);
                        prison_map.put(suggestKey, id);
                    }
                } catch (Exception e) {
                    Log.w("Error", e.getMessage());
                }
                return null;
            } else {
                handler.sendEmptyMessage(2);
                return "";
            }
        }
    }

    /**
     * 云信id在其他设备登录
     */
    private void showKickoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("账号下线提示");
        builder.setCancelable(false);
        builder.setMessage("您的账号" + sp.getString("token", "") + "在其他设备登录，点击重新登录。");
        builder.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.putBoolean("is_first", false); // 防止不重新登录直接退出当再次进来还需要经过欢迎页面
                editor.commit();
                startActivity(intent);
                NIMClient.getService(AuthService.class).logout();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        if(Utils.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        String result = HttpRequestUtil.doHttpsGet(Constants.URL_HEAD + "prisoner?phone=" + sp.getString("username", "") + "&uuid=" + sp.getString("password", ""));
                        Log.i("请求成功", result);
                        parseUserInfoResult(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(4); // 获取用户信息失败  则显示无账号快捷登录界面
                    }
                }
            }.start();
        }else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 解析用户和囚犯关系信息
     */
    private void parseUserInfoResult(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("prison_term_started_at", jsonObject1.getString("prison_term_started_at"));
                editor.putString("prison_term_ended_at", jsonObject1.getString("prison_term_ended_at"));
                editor.putString("gender", jsonObject1.getString("gender"));
                editor.putString("prisoner_name", jsonObject1.getString("name"));
                editor.putInt("jail_id", jsonObject1.getInt("jail_id"));
                editor.putString("prisoner_number", jsonObject1.getString("prisoner_number"));
                editor.commit();
                jail_id = sp.getInt("jail_id",0);
               // getCommodity();// 获取商品
                handler.sendEmptyMessage(3);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pagerList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return (view == o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pagerList.get(position).getView());
            pagerList.get(position).initData();
            return pagerList.get(position).getView();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * 按兩次返回退出程序   add by hzn
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
                drawerLayout.closeDrawer(Gravity.LEFT);
            }else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取测拉菜单Fragment对象
     * @return
     */
    public MenuFragment getMenuFragment() {
        return (MenuFragment) getSupportFragmentManager().findFragmentByTag(
                "MENU");
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.rl_home_menu:
                if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.rl_message:
                intent = new Intent(MainActivity.this, SystemMessageActivity.class);
                startActivity(intent);
                view_red_point.setVisibility(View.GONE);
                break;
        }
        super.onClick(v);
    }

    /**
     * 获取商品列表
     */
    private void getCommodity(){
        if(Utils.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    Message msg = handler.obtainMessage();
                    String token = sp.getString("token", "");
                    try {
                        url = Constants.URL_HEAD + "items?jail_id=" + jail_id + "&access_token=";
                        String result = HttpRequestUtil.doHttpsGet(url + token);
                        if(result.contains("StatusCode is ")){
                            msg.obj = "error";
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }else {
                            msg.obj = "success";
                            Bundle bundle = new Bundle();
                            bundle.putString("result", result);
                            msg.setData(bundle);
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.obj = "error";
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        }else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     *  解析商品列表
     * @param s
     * @return
     */
    private List<Commodity> analysiscommodity(String s){
        List<Commodity> commodities = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0;i < jsonArray.length();i++){
                Commodity commodity = new Commodity();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                commodity.setId(jsonObject.getInt("id"));
                commodity.setTitle(jsonObject.getString("title"));
                commodity.setDescription(jsonObject.getString("description"));
                commodity.setAvatar_url(jsonObject.getString("avatar_url"));
                commodity.setPrice(jsonObject.getString("price"));
                commodity.setCategory_id(jsonObject.getInt("category_id"));
                commodities.add(commodity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return commodities;
    }

    /**
     * 获取微信签名
     * @param params
     * @return
     */
    private String genAppSign(List<NameValuePair> params) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        com.gkzxhn.gkprison.utils.Log.d("sa", sb.toString());
        sb.append("key=");
        sb.append("d75699d893882dea526ea05e9c7a4090");
        com.gkzxhn.gkprison.utils.Log.d("dd", sb.toString());
        //  sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5Utils.ecoder(sb.toString()).toUpperCase();
        com.gkzxhn.gkprison.utils.Log.d("orion1", appSign);
        return appSign;
    }

    /**
     * 获得32位随机字符串
     * @return
     */
    private String getRandomString() {
        String suiji = "";
        int len = 32;
        char[] chars = new char[len];
        Random random = new Random();
        for (int i = 0;i < len;i++){
            if (random.nextBoolean() == true){
                chars[i] = (char)(random.nextInt(25) + 97);
            }else {
                chars[i] = (char)(random.nextInt(9) + 48);
            }
        }
        suiji = new String(chars);
        return suiji;
    }

    /**
     * 获取发至微信的xml
     * @return
     */
    private String getXml(){
        String nonce_str = getRandomString();
        List<NameValuePair> list = new LinkedList<NameValuePair>();
        list.add(new BasicNameValuePair("appid", WeixinConstants.APP_ID));
        list.add(new BasicNameValuePair("mch_id",PaymentActivity.mch_id));
        list.add(new BasicNameValuePair("nonce_str",nonce_str));
        list.add(new BasicNameValuePair("out_trade_no", PaymentActivity.TradeNo));
        String sign = genAppSign(list);
        StringBuffer  xml = new StringBuffer();
        xml.append("<xml>");
        xml.append("<appid>");
        xml.append(WeixinConstants.APP_ID);
        xml.append("</appid>");
        xml.append("<mch_id>");
        xml.append(PaymentActivity.mch_id);
        xml.append("</mch_id>");
        xml.append("<nonce_str>");
        xml.append(nonce_str);
        xml.append("</nonce_str>");
        xml.append("<out_trade_no>");
        xml.append(PaymentActivity.TradeNo);
        xml.append("</out_trade_no>");
        xml.append("<sign>");
        xml.append(sign);
        xml.append("</sign>");
        xml.append("</xml>");
        return xml.toString();
    }
}