package com.gkzxhn.gkprison.userport.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.base.BasePager;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.fragment.MenuFragment;
import com.gkzxhn.gkprison.userport.pager.CanteenPager;
import com.gkzxhn.gkprison.userport.pager.HomePager;
import com.gkzxhn.gkprison.userport.pager.RemoteMeetPager;
import com.gkzxhn.gkprison.userport.view.CustomDrawerLayout;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 主activity
 */
public class MainActivity extends BaseActivity {
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private List<Commodity> commodityList = new ArrayList<>();
    private ViewPager home_viewPager;
    private RadioGroup rg_bottom_guide;
    private List<BasePager> pagerList;
    private RadioButton rb_bottom_guide_home;
    private RadioButton rb_bottom_guide_visit;
    private RadioButton rb_bottom_guide_canteen;
    private long mExitTime;//add by hzn 退出按键时间间隔
    private CustomDrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FrameLayout fl_drawer;
    private SharedPreferences sp;
    private boolean isRegisteredUser;
    private MyPagerAdapter adapter;
    private String url = Constants.URL_HEAD + "items?jail_id=1&access_token=";

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
            }

        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_main, null);
        home_viewPager = (ViewPager) view.findViewById(R.id.layout_content);
        rg_bottom_guide = (RadioGroup) view.findViewById(R.id.rg_bottom_guide);
        rb_bottom_guide_home = (RadioButton) view.findViewById(R.id.rb_bottom_guide_home);
        rb_bottom_guide_visit = (RadioButton) view.findViewById(R.id.rb_bottom_guide_visit);
        rb_bottom_guide_canteen = (RadioButton) view.findViewById(R.id.rb_bottom_guide_canteen);
        drawerLayout = (CustomDrawerLayout) view.findViewById(R.id.drawer_layout);
        fl_drawer = (FrameLayout) view.findViewById(R.id.fl_drawer);
//        tv_title_bar_title = (TextView) view.findViewById(R.id.tv_title_bar_title);
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


    @Override
    protected void initData() {
        StatusCode statusCode = NIMClient.getStatus();
        showToastMsgShort(statusCode.toString());
        Log.i("自动登录...", statusCode.toString());
        sp = getSharedPreferences("config", MODE_PRIVATE);
        isRegisteredUser = sp.getBoolean("isRegisteredUser", false);
        getUserInfo();
        if(isRegisteredUser) {
            getCommodity();
        }
        setMessageVisibility(View.VISIBLE);
        setSupportActionBar(tool_bar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.icon_menu, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        MenuFragment menuFragment = new MenuFragment();
        // 拿当前fragment去替换左侧侧拉栏目的帧布局内容
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_drawer, menuFragment, "MENU").commit();
        pagerList = new ArrayList<>();
        pagerList.clear();
        if(isRegisteredUser) {
            pagerList.add(new HomePager(this));
            pagerList.add(new RemoteMeetPager(this));
            pagerList.add(new CanteenPager(this));
        }else {
            pagerList.add(new HomePager(this));
        }

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
                            setTitle("远程会见");
                            setMenuVisibility(View.GONE);
                            setActionBarGone(View.VISIBLE);
                            setMessageVisibility(View.GONE);
                        } else {
                            showToastMsgShort("注册后可用");
                        }
                        break;
                    case R.id.rb_bottom_guide_canteen: // 小卖部
                        if (isRegisteredUser) {
                            home_viewPager.setCurrentItem(2);
                            setTitle("小卖部");
                            setMenuVisibility(View.GONE);
                            setActionBarGone(View.GONE);
                            setMessageVisibility(View.GONE);
                        } else {
                            showToastMsgShort("注册后可用");
                        }
                        break;
                }
            }
        });
        rg_bottom_guide.check(R.id.rb_bottom_guide_home); // 默认选择首页
        home_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_home);
                    setTitle("首页");
                    setMenuVisibility(View.VISIBLE);
                    setActionBarGone(View.VISIBLE);
                    setMessageVisibility(View.VISIBLE);
                } else if (i == 1) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_visit);
                    setTitle("远程会见");
                    setMenuVisibility(View.GONE);
                    setActionBarGone(View.VISIBLE);
                    setMessageVisibility(View.GONE);
                } else if (i == 2) {
                    rg_bottom_guide.check(R.id.rb_bottom_guide_canteen);
                    setTitle("小卖部");
                    setMenuVisibility(View.GONE);
                    setActionBarGone(View.GONE);
                    setMessageVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        rl_home_menu.setOnClickListener(this);
        rl_message.setOnClickListener(this);
    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        if(Utils.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    HttpUtils httpUtils = new HttpUtils();
                    httpUtils.send(HttpRequest.HttpMethod.GET, Constants.URL_HEAD + "prisoner?phone=" + sp.getString("username", "") + "&uuid=" + sp.getString("password", ""), new RequestCallBack<Object>() {
                        @Override
                        public void onSuccess(ResponseInfo<Object> responseInfo) {
                            Log.i("请求成功", responseInfo.result.toString());
                            parseUserInfoResult(responseInfo.result.toString());
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Log.i("请求失败", s + "---" + e.getMessage());
                            showToastMsgShort("请求失败");
                        }
                    });
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
                break;
        }
        super.onClick(v);
    }

    private void getCommodity(){
        if(Utils.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    Message msg = handler.obtainMessage();
                    HttpClient httpClient = new DefaultHttpClient();
                    String token = sp.getString("token", "");
                    HttpGet httpGet = new HttpGet(url + token);
                    try {
                        HttpResponse response = httpClient.execute(httpGet);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity(), "utf-8");
                            msg.obj = "success";
                            Bundle bundle = new Bundle();
                            bundle.putString("result", result);
                            msg.setData(bundle);
                            msg.what = 1;
                            handler.sendMessage(msg);
                        } else {
                            msg.obj = "error";
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
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
     *
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
}
