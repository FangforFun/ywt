package com.gkzxhn.gkprison.userport.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.constant.WeixinConstants;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.login.adapter.AutoTextAdapater;
import com.gkzxhn.gkprison.prisonport.http.HttpRequestUtil;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.bean.PrisonerUserInfo;
import com.gkzxhn.gkprison.userport.event.MeetingTimeEvent;
import com.gkzxhn.gkprison.userport.fragment.CanteenBaseFragment;
import com.gkzxhn.gkprison.userport.fragment.HomeFragment;
import com.gkzxhn.gkprison.userport.fragment.MenuFragment;
import com.gkzxhn.gkprison.userport.fragment.RemoteMeetFragment;
import com.gkzxhn.gkprison.userport.requests.ApiRequest;
import com.gkzxhn.gkprison.userport.view.CustomDrawerLayout;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.MD5Utils;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * created by huangzhengneng on 2015/12/22
 * 主activity
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    public static final MediaType JSON = MediaType.parse("application/xml; charset=utf-8");

    @BindView(R.id.fl_main_content) FrameLayout fl_main_content;// 主布局内容
    @BindView(R.id.rg_bottom_guide) RadioGroup rg_bottom_guide; // 底部导航栏组
    @BindView(R.id.rb_bottom_guide_home) RadioButton rb_bottom_guide_home; // 首页
    @BindView(R.id.rb_bottom_guide_visit) RadioButton rb_bottom_guide_visit; // 探监
    @BindView(R.id.rb_bottom_guide_canteen) RadioButton rb_bottom_guide_canteen; // 电子商务
    @BindView(R.id.drawer_layout) CustomDrawerLayout drawerLayout; // 侧拉菜单
    @BindView(R.id.fl_drawer) FrameLayout fl_drawer; // 侧拉菜单底层帧布局

    private List<BaseFragment> fragments = new ArrayList<>();
    private FragmentManager manager;
    private FragmentTransaction transaction = null;
    private HomeFragment homeFragment = null;
    private RemoteMeetFragment remoteMeetFragment = null;
    private CanteenBaseFragment canteenBaseFragment = null;
    private String datebase_path = getFilesDir().getPath() + "/databases/chaoshi.db";
    private SQLiteDatabase db = SQLiteDatabase.openDatabase(datebase_path, null, SQLiteDatabase.OPEN_READWRITE);
    private List<Commodity> commodityList = new ArrayList<>();
    private long mExitTime;//add by hzn 退出按键时间间隔
    private boolean isRegisteredUser; // 是否注册登录用户
    private int jail_id;  // 监狱id
    private ActionBarDrawerToggle toggle;
    private String times;
    private AutoCompleteTextView actv_prison_choose; // 监狱选择
    private AutoTextAdapater autoTextAdapater;
    private String data; // 监狱选择访问服务器返回的字符串
    private List<String> suggest;// 自动提示的集合
    private Map<String, Integer> prison_map; // 服务器返回的监狱列表存储需要的集合
    private OkHttpClient client = new OkHttpClient();

    private AlertDialog fastLoginDialog;// 快速登录弹窗

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String m = (String)msg.obj;
                    if (m.equals("success")){
                        Bundle bundle = msg.getData();
                        String commodity = bundle.getString("result");
                        commodityList = analysis_commodity(commodity);
                        if (commodityList.size() != 0){
                            String sql = "delete from Items where 1=1";
                            db.execSQL(sql);
                            for (int i = 0;i < commodityList.size();i++){
                                String sql1 = "insert into Items (id,title,description,price,avatar_url,category_id,ranking) values ("
                                        + commodityList.get(i).getId() + ",'" + commodityList.get(i).getTitle() + "','"
                                        + commodityList.get(i).getDescription() + "','" + commodityList.get(i).getPrice()
                                        + "','" + commodityList.get(i).getAvatar_url() + "'," + commodityList.get(i).getCategory_id()
                                        + "," + commodityList.get(i).getRanking() + ")";
                                db.execSQL(sql1);
                            }
                        }
                    }else if (m.equals("error")){
                        showToastMsgShort("同步数据失败");
                    }
                    break;
                case 2:// 无账号快捷登录监狱选择没有网络
                    showToastMsgShort("没有网络,请检查网络设置");
                    break;
                case 3:
                    initFragment();
                    layoutMain();
                    break;
                case 5:
                    // 用户信息为空
                    break;
                case 4: // 获取用户信息失败   提示重新登录
                    reLogin();
                    break;
            }
        }
    };

    /**
     *  获取用户信息失败   提示重新登录
     */
    private void reLogin() {
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
                    SPUtil.clear(MainActivity.this);
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
    }

    @Override
    protected View initView() {
        View view = View.inflate(this, R.layout.activity_main, null);
        ButterKnife.bind(this, view);
        adjustmentIcon(); // 调整底部导航栏图标
        return view;
    }

    /**
     * 调整底部导航栏图标
     */
    private void adjustmentIcon() {
        Drawable[] drawables = rb_bottom_guide_home.getCompoundDrawables();
        drawables[1].setBounds(0, DensityUtil.dip2px(getApplicationContext(), 5),
                getResources().getDimensionPixelSize(R.dimen.home_tab_width),
                getResources().getDimensionPixelSize(R.dimen.home_tab_height));
        rb_bottom_guide_home.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        Drawable[] drawables2 = rb_bottom_guide_visit.getCompoundDrawables();
        drawables2[1].setBounds(0, DensityUtil.dip2px(getApplicationContext(), 5),
                getResources().getDimensionPixelSize(R.dimen.home_tab_width),
                getResources().getDimensionPixelSize(R.dimen.home_tab_height));
        rb_bottom_guide_visit.setCompoundDrawables(drawables2[0], drawables2[1], drawables2[2], drawables2[3]);
        Drawable[] drawables3 = rb_bottom_guide_canteen.getCompoundDrawables();
        drawables3[1].setBounds(0, DensityUtil.dip2px(getApplicationContext(), 5),
                getResources().getDimensionPixelSize(R.dimen.home_tab_width),
                getResources().getDimensionPixelSize(R.dimen.home_tab_height));
        rb_bottom_guide_canteen.setCompoundDrawables(drawables3[0], drawables3[1], drawables3[2], drawables3[3]);
    }

    /**
     * 更新上次会见时间
     * @param event
     */
    public void onEvent(MeetingTimeEvent event){
        RemoteMeetFragment remoteMeetFragment = (RemoteMeetFragment) fragments.get(1);
        remoteMeetFragment.setLastMeetingTime();
    }

    /**
     * 重新登录任务
     */
    private Runnable reLoginTask = new Runnable() {
        @Override
        public void run() {
            RequestCallback callback = new RequestCallback() {
                @Override public void onSuccess(Object o) {
                    Log.i(TAG, "MainActivity重新登录了");
                }

                @Override public void onFailed(int i) {
                    switch (i) {
                        case 302:showToastMsgShort("手机号或者身份证号错误");break;
                        case 503:showToastMsgShort("服务器繁忙");break;
                        case 415:showToastMsgShort("网络出错，请检查网络");break;
                        case 408:showToastMsgShort("请求超时，请稍后再试");break;
                        case 403:showToastMsgShort("非法操作或没有权限");break;
                        case 422:showToastMsgShort("您的账号已被禁用");break;
                        case 500:showToastMsgShort("服务器错误");break;
                        default:showToastMsgShort("登录失败");break;
                    }
                }

                @Override public void onException(Throwable throwable) {
                    showToastMsgShort("登录异常");
                    Log.i(TAG, "MainActivity重新登录异常" + throwable.getMessage());
                }
            };
            String mToken = SPUtil.get(MainActivity.this, "token", "") + "";
            LoginInfo info = new LoginInfo(mToken, mToken); // config...
            NIMClient.getService(AuthService.class).login(info)
                    .setCallback(callback);
        }
    };

    @Override
    protected void initData() {
        StatusCode statusCode = NIMClient.getStatus();
        Log.i("云信id状态...", statusCode.toString());
        isRegisteredUser = (Boolean) SPUtil.get(MainActivity.this, "isRegisteredUser", false);
        if(isRegisteredUser && statusCode != StatusCode.LOGINED && statusCode != StatusCode.KICKOUT){
            // 如果是注册用户并且不是被其他端踢掉的未上线就重新登录
            handler.post(reLoginTask);
        }
        if(isRegisteredUser) {
            getUserInfo();// 获取当前登录用户的信息
            if((Boolean)SPUtil.get(MainActivity.this, "has_new_notification", false)){
                view_red_point.setVisibility(View.VISIBLE);
            }
            doWXPayController();
        }else {
            prison_map = new HashMap<>();
            showPrisonDialog();// 弹出监狱选择框
        }
        if(statusCode == StatusCode.KICKOUT){
            showKickoutDialog();// 其他设备登录
        }
        setMessageVisibility(View.VISIBLE); // 显示系统消息图标

        setSupportActionBar(tool_bar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.icon_menu, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        // 侧拉栏
        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_drawer, menuFragment, "MENU").commit();

        rl_home_menu.setOnClickListener(this);
        rl_message.setOnClickListener(this);
    }

    /**
     * 更新微信支付订单
     */
    private void doWXPayController() {
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
                        Log.d(TAG, result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            String type = "微信支付";
            String sql = "update Cart set isfinish = 1,payment_type = '"+type+"' where time = '" + times + "'";
            db.execSQL(sql);
        }
    }

    /**
     * 布局
     */
    private void layoutMain() {
        rg_bottom_guide.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_bottom_guide_home: // 首页
                        switchUI(0, "首页", View.VISIBLE, View.VISIBLE, View.VISIBLE);
                        break;
                    case R.id.rb_bottom_guide_visit: // 远程会见
                        if (isRegisteredUser) {
                            switchUI(1, "探监", View.GONE, View.VISIBLE, View.GONE);
                        } else {
                            showToastMsgShort(getString(R.string.enable_logined));
                        }
                        break;
                    case R.id.rb_bottom_guide_canteen: // 小卖部
                        if (isRegisteredUser) {
                            switchUI(2, "电子商务", View.GONE, View.VISIBLE, View.GONE);
                        } else {
                            showToastMsgShort(getString(R.string.enable_logined));
                        }
                        break;
                }
            }
        });
        rg_bottom_guide.check(R.id.rb_bottom_guide_home); // 默认选择首页
    }

    /**
     * 切换并设置相关ui
     * @param index
     * @param title
     * @param menuVisibility
     * @param ActionBarVisibility
     * @param messageVisibility
     */
    private void switchUI(int index, String title, int menuVisibility,
                          int ActionBarVisibility, int messageVisibility) {
        switchFragment(index); // 切换fragment
        setTitle(title);// 设置标题
        setMenuVisibility(menuVisibility); // 设置菜单图标
        setActionBarGone(ActionBarVisibility); // 设置标题栏
        setMessageVisibility(messageVisibility);// 设置消息图标
    }

    /**
     * 切换fragment
     * @param index 索引
     */
    @SuppressLint("CommitTransaction")
    private void switchFragment(int index) {
        transaction = manager.beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            if(index == i) {
                transaction.show(fragments.get(index));
            }else {
                transaction.hide(fragments.get(i));
            }
        }
        transaction.commit();
    }

    /**
     * 初始化fragment
     */
    @SuppressLint("CommitTransaction")
    private void initFragment() {
        fragments.clear();
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        if(homeFragment == null) {
            homeFragment = new HomeFragment();
            fragments.add(homeFragment);
        }
        if(homeFragment.isAdded()){
            transaction.remove(homeFragment);
        }
        transaction.add(R.id.fl_main_content, homeFragment);
        if(remoteMeetFragment == null) {
            remoteMeetFragment = new RemoteMeetFragment();
            fragments.add(remoteMeetFragment);
        }
        if(remoteMeetFragment.isAdded()){
            transaction.remove(remoteMeetFragment);
        }
        transaction.add(R.id.fl_main_content, remoteMeetFragment);
        if(canteenBaseFragment == null) {
            canteenBaseFragment = new CanteenBaseFragment();
            fragments.add(canteenBaseFragment);
        }
        if(remoteMeetFragment.isAdded()){
            transaction.remove(remoteMeetFragment);
        }
        transaction.add(R.id.fl_main_content, canteenBaseFragment);
        transaction.show(homeFragment).hide(remoteMeetFragment)
                .hide(canteenBaseFragment);
        transaction.commitAllowingStateLoss();
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new GetSuggestData().execute(newText);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
        fastLoginDialog = builder.create();
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
                        SPUtil.put(MainActivity.this, "jail_id", jail_id);
                        fastLoginDialog.dismiss();
                        addHomeFragment();
                        layoutMain();// 布局
                    } else {
                        showToastMsgShort("抱歉，暂未开通此监狱");
                        return;
                    }
                }
            }
        });
        fastLoginDialog.show();
    }

    /**
     * 添加主Fragment
     */
    @SuppressLint("CommitTransaction")
    private void addHomeFragment() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        if(homeFragment != null && homeFragment.isAdded()){
            transaction.remove(homeFragment);
        }
        if(homeFragment == null){
            homeFragment = new HomeFragment();
        }
        transaction.add(R.id.fl_main_content, homeFragment);
        transaction.commit();
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
        builder.setMessage("您的账号" + SPUtil.get(MainActivity.this, "token", "") + "在其他设备登录，点击重新登录。");
        builder.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                SPUtil.clear(MainActivity.this);
                // 防止不重新登录直接退出当再次进来还需要经过欢迎页面
                SPUtil.put(MainActivity.this, "is_first", false);
                startActivity(intent);
                NIMClient.getService(AuthService.class).logout();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        if(Utils.isNetworkAvailable()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Constants.URL_HEAD)
                    .build();
            ApiRequest repo = retrofit.create(ApiRequest.class);
            Map<String, String> map = new HashMap<>();
            map.put("uuid", (String) SPUtil.get(MainActivity.this, "password", ""));
            mSubscriptions.add(
                    repo.getUserInfo((String) SPUtil.get(MainActivity.this, "username", ""), map)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<PrisonerUserInfo>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onError(Throwable e) {
                                    handler.sendEmptyMessage(4); // 获取用户信息失败  则显示无账号快捷登录界面
                                    Log.i(TAG, e.getMessage());
                                }

                                @Override
                                public void onNext(PrisonerUserInfo prisonerUserInfo) {
                                    Log.i(TAG, prisonerUserInfo.getResult().toString());
                                    savePrisonerInfo(prisonerUserInfo);
                                    jail_id = (int) SPUtil.get(MainActivity.this, "jail_id",0);
                                    handler.sendEmptyMessage(3);
                                }
                            })
            );
        }else {
            showToastMsgShort("没有网络");
        }
    }

    /**
     * 保存囚犯信息
     * @param prisonerUserInfo
     */
    private void savePrisonerInfo(PrisonerUserInfo prisonerUserInfo) {
        SPUtil.put(MainActivity.this, "prison_term_started_at",
                prisonerUserInfo.getResult().get(0).getPrison_term_started_at());
        SPUtil.put(MainActivity.this, "prison_term_ended_at",
                prisonerUserInfo.getResult().get(0).getPrison_term_ended_at());
        SPUtil.put(MainActivity.this, "gender",
                prisonerUserInfo.getResult().get(0).getGender());
        SPUtil.put(MainActivity.this, "prisoner_name",
                prisonerUserInfo.getResult().get(0).getName());
        SPUtil.put(MainActivity.this, "jail_id",
                prisonerUserInfo.getResult().get(0).getJail_id());
        SPUtil.put(MainActivity.this, "prisoner_number",
                prisonerUserInfo.getResult().get(0).getPrisoner_number());
    }

    @Override
    public void onBackPressed() {
        if(fastLoginDialog != null && fastLoginDialog.isShowing()){
            // 快速登录
            fastLoginDialog.dismiss();
            finish();
        }else {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    super.onBackPressed();
                }
            }
        }
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
     *  解析商品列表
     * @param s
     * @return
     */
    private List<Commodity> analysis_commodity(String s){
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