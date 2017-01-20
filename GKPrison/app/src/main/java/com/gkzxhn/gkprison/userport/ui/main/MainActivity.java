package com.gkzxhn.gkprison.userport.ui.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.BuildConfig;
import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.base.BaseFragmentNew;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.activity.RemittanceRecordActivity;
import com.gkzxhn.gkprison.userport.activity.SettingActivity;
import com.gkzxhn.gkprison.userport.activity.ShoppingRecoderActivity;
import com.gkzxhn.gkprison.userport.ui.UserInfoActivity;
import com.gkzxhn.gkprison.userport.ui.login.Config;
import com.gkzxhn.gkprison.userport.ui.login.LoginActivity;
import com.gkzxhn.gkprison.userport.ui.main.canteen.CanteenBaseFragment;
import com.gkzxhn.gkprison.userport.ui.main.home.HomeFragment;
import com.gkzxhn.gkprison.userport.ui.main.visit.RemoteMeetFragment;
import com.gkzxhn.gkprison.userport.view.AutoCompleteTv;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StatusBarUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.gkzxhn.gkprison.utils.UIUtils;
import com.keda.sky.app.GKStateMannager;
import com.keda.sky.app.TruetouchGlobal;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author: Huang ZN
 * Date: 2016/12/28
 * Email:943852572@qq.com
 * Description:主页
 */
public class MainActivity extends BaseActivityNew implements MainContract.View,
        View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.navigationView) NavigationView navigationView;
    @BindView(R.id.iv_user_icon) ImageView iv_user_icon;
    @BindView(R.id.tv_menu_user_name) TextView tv_menu_user_name;
    @BindView(R.id.tool_bar) Toolbar tool_bar;
    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.rl_home_menu) RelativeLayout rl_home_menu;

    @BindView(R.id.main_content) FrameLayout main_content;
    @BindView(R.id.rg_bottom_guide) RadioGroup rg_bottom_guide;
    @BindView(R.id.rb_bottom_guide_home) RadioButton rb_bottom_guide_home;
    @BindView(R.id.rb_bottom_guide_visit) RadioButton rb_bottom_guide_visit;
    @BindView(R.id.rb_bottom_guide_canteen) RadioButton rb_bottom_guide_canteen;

    private List<BaseFragmentNew> fragments = new ArrayList<>();
    private FragmentManager manager;
    private FragmentTransaction transaction = null;
    private HomeFragment homeFragment = null;
    private RemoteMeetFragment remoteMeetFragment = null;
    private CanteenBaseFragment canteenBaseFragment = null;

    @Inject MainPresenter mPresenter;

    private ProgressDialog progressDialog;
    private AlertDialog reLoginDialog;
    private AlertDialog fastLoginDialog;
    private AlertDialog kickoutDialog;
    private AlertDialog logoutDialog;

    private boolean isRegisterUser;
    private SQLiteDatabase database;
    private long mExitTime;


    /**
     * 开启当前activity
     * @param mContext
     */
    public static void startActivity(Context mContext){
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public int setLayoutResId() {
        return R.layout.main_layout;
    }

    @Override
    protected void initUiAndListener() {
        mPresenter.attachView(this);
        ButterKnife.bind(this);
        setSupportActionBar(tool_bar);
        tv_title.setText(getString(R.string.main_page));
        rl_home_menu.setVisibility(View.VISIBLE);
        rl_home_menu.setOnClickListener(this);
        rg_bottom_guide.setOnCheckedChangeListener(this);
        setNavigationViewWidth();
        StatusBarUtil.setColorForDrawerLayout(this, drawerLayout, Color.parseColor("#EE6495ed"), 0);
        setNavigationItemClick();
        setBottomGuideIcon();
        isRegisterUser = (boolean) SPUtil.get(this, SPKeyConstants.IS_REGISTERED_USER, false);
        database = StringUtils.getSQLiteDB(this);
        mPresenter.checkStatus();
        tv_title.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!GKStateMannager.mRegisterGK){
//            showToast("GK重连中...");
//            GKStateMannager.instance().registerGK();
//        }
    }

    /**
     * 设置侧栏item点击事件
     */
    private void setNavigationItemClick() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()){
                    case R.id.user_info:
                        if (isRegisterUser) {
                            intent = new Intent(MainActivity.this, UserInfoActivity.class);
                            startActivity(intent);
                        } else {
                            showToast(getString(R.string.enable_logined));
                        }
                        break;
                    case R.id.remittance_record:
                        if (isRegisterUser) {
                            intent = new Intent(MainActivity.this, RemittanceRecordActivity.class);
                            startActivity(intent);
                        } else {
                            showToast(getString(R.string.enable_logined));
                        }
                        break;
                    case R.id.shopping_record:
                        if (isRegisterUser) {
                            intent = new Intent(MainActivity.this, ShoppingRecoderActivity.class);
                            startActivity(intent);
                        } else {
                            showToast(getString(R.string.enable_logined));
                        }
                        break;
                    case R.id.setting:
                        intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.logout:
                        if (isRegisterUser){
                            logoutDialog = MainUtils.showConfirmDialog(MainActivity.this);
                        }else {
                            LoginActivity.startActivityClearTask(MainActivity.this);
                            MainActivity.this.finish();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 设置navigationView宽度
     */
    private void setNavigationViewWidth() {
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 2;
        navigationView.setLayoutParams(params);
    }

    /**
     * 调整底部导航栏的图标大小
     */
    private void setBottomGuideIcon() {
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

    @Override
    protected void initInjector() {
        DaggerMainComponent.builder()
                .appComponent(getAppComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return false;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return false;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, TAG + " onDestroy");
        UIUtils.dismissProgressDialog(progressDialog);
        UIUtils.dismissAlertDialog(reLoginDialog, fastLoginDialog, kickoutDialog, logoutDialog);
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_home_menu:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)){
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.tv_title:// debug模式下
                if (BuildConfig.DEBUG) {
                    showToast("当前登录账号为：" + Config.mAccount + ",GK状态：" + GKStateMannager.mRegisterGK);
                }
                break;
        }
    }

    @Override
    public void showProgress(String msg) {
        if (progressDialog == null){
            progressDialog = UIUtils.showProgressDialog(this, msg);
        }else {
            if (!progressDialog.isShowing())
                progressDialog.show();
        }
    }

    @Override
    public void dismissProgress() {
        UIUtils.dismissProgressDialog(progressDialog);
    }

    @Override
    public void showToast(String msg) {
        ToastUtil.showShortToast(msg);
    }

    @Override
    public void reLoginNotGetUserInfo() {
        reLoginDialog = UIUtils.showReLoginDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reLogin();
            }
        });
    }

    /**
     * 重新登录
     */
    private void reLogin() {
        LoginActivity.startActivityClearTask(MainActivity.this);
        if (isRegisterUser) {
            SPUtil.clear(MainActivity.this);
            NIMClient.getService(AuthService.class).logout();
            TruetouchGlobal.logOff();
        }
    }

    @Override
    public void getUserInfoSuccess() {
        initFragment();
        if (isRegisterUser) {
            tv_menu_user_name.setText((String) SPUtil.get(this, SPKeyConstants.NAME, getString(R.string.user_name)));
            String ICON_URL = (String) SPUtil.get(this, SPKeyConstants.AVATAR, "");
            if(!TextUtils.isEmpty(ICON_URL)){
                Picasso.with(this).load(Constants.RESOURSE_HEAD + ICON_URL)
                        .error(R.drawable.default_icon).into(iv_user_icon);
                mPresenter.downloadAvatar(Constants.RESOURSE_HEAD + ICON_URL);
            }
            String times = getIntent().getStringExtra("times");
            if (!TextUtils.isEmpty(times)) {
                mPresenter.doWXPayController(times, database);
            }
        }
    }

    @Override
    public void fastLoginWithoutAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        View prison_choose = View.inflate(this, R.layout.prison_input, null);
        builder.setView(prison_choose);
        Button bt_ok = (Button) prison_choose.findViewById(R.id.bt_ok);
        final AutoCompleteTv actv_prison_input = (AutoCompleteTv) prison_choose.findViewById(R.id.actv_prison_input);
        fastLoginDialog = builder.create();
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = actv_prison_input.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    showToast(getString(R.string.input_prison_name));
                } else {
                    Map<String, Integer> prison_map = actv_prison_input.getDataList();
                    if (prison_map.containsKey(content)) {
                        int jail_id = prison_map.get(content);
                        SPUtil.put(MainActivity.this, SPKeyConstants.JAIL_ID, jail_id);
                        fastLoginDialog.dismiss();
                        addHomeFragment();
                    } else {
                        showToast(getString(R.string.not_open_prison));
                    }
                }
            }
        });
        fastLoginDialog.show();
    }

    @Override
    public void accountKickout() {
        kickoutDialog = UIUtils.showKickoutDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reLogin();//
            }
        });
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
        transaction.add(R.id.main_content, homeFragment);
        if(remoteMeetFragment == null) {
            remoteMeetFragment = new RemoteMeetFragment();
            fragments.add(remoteMeetFragment);
        }
        if(remoteMeetFragment.isAdded()){
            transaction.remove(remoteMeetFragment);
        }
        transaction.add(R.id.main_content, remoteMeetFragment);
        if(canteenBaseFragment == null) {
            canteenBaseFragment = new CanteenBaseFragment();
            fragments.add(canteenBaseFragment);
        }
        if(remoteMeetFragment.isAdded()){
            transaction.remove(remoteMeetFragment);
        }
        transaction.add(R.id.main_content, canteenBaseFragment);
        transaction.show(homeFragment).hide(remoteMeetFragment)
                .hide(canteenBaseFragment);
        remoteMeetFragment.setUserVisibleHint(false);
        canteenBaseFragment.setUserVisibleHint(false);
        homeFragment.setUserVisibleHint(true);
        transaction.commitAllowingStateLoss();
    }

    /**
     * 添加主Fragment
     */
    @SuppressLint("CommitTransaction")
    public void addHomeFragment() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        if(homeFragment != null && homeFragment.isAdded()){
            transaction.remove(homeFragment);
        }
        if(homeFragment == null){
            homeFragment = new HomeFragment();
        }
        transaction.add(R.id.main_content, homeFragment);
        transaction.commit();
        // 设置侧拉的相关界面
        iv_user_icon.setImageResource(R.drawable.default_icon);
        tv_menu_user_name.setText(R.string.user_name);
        navigationView.getMenu().getItem(4).setTitle(getString(R.string.login_text));
    }

    /**
     * 切换并设置相关ui
     * @param index
     * @param title
     * @param menuVisibility
     * @param messageVisibility
     */
    private void switchUI(int index, String title, int menuVisibility, int messageVisibility) {
        switchFragment(index); // 切换fragment
        tv_title.setText(title);// 设置标题
        rl_home_menu.setVisibility(menuVisibility); // 设置菜单图标
    }

    /**
     *  切换fragment
     * @param index 索引
     */
    @SuppressLint("CommitTransaction")
    private void switchFragment(int index) {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            if(index == i) {
                transaction.show(fragments.get(index));
                fragments.get(i).setUserVisibleHint(true);
            }else {
                transaction.hide(fragments.get(i));
                fragments.get(i).setUserVisibleHint(false);
            }
        }
        transaction.commit();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_bottom_guide_home: // 首页
                switchUI(0, getString(R.string.main_page), View.VISIBLE, View.VISIBLE);
                break;
            case R.id.rb_bottom_guide_visit: // 探监
                if (isRegisterUser) {
                    switchUI(1, getString(R.string.visit_prison), View.GONE, View.GONE);
                } else {
                    showToast(getString(R.string.enable_logined));
                }
                break;
            case R.id.rb_bottom_guide_canteen: // 电子商务
                if (isRegisterUser) {
                    switchUI(2, getString(R.string.canteen), View.GONE, View.GONE);
                } else {
                    showToast(getString(R.string.enable_logined));
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (progressDialog != null && progressDialog.isShowing())
            return;
        if (reLoginDialog != null && reLoginDialog.isShowing())
            return;
        if (kickoutDialog != null && kickoutDialog.isShowing())
            return;
        if (fastLoginDialog != null && fastLoginDialog.isShowing())
            return;
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
            return;
        }
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, R.string.exit_app, Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
