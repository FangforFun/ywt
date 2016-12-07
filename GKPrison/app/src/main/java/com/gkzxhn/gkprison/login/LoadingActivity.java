package com.gkzxhn.gkprison.login;

import android.view.View;
import android.widget.AdapterView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.keda.sky.app.PcAppStackManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * created by huangzhengneng on 2015/12/15
 * 登录页面
 */
public class LoadingActivity extends BaseActivity {

    public PersonLoginFragment personLoginFragment;
    public PrisonLoginFragment prisonLoginFragment;
    public static boolean isPerson = true;

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        return View.inflate(this,R.layout.activity_loading,null);
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }

    @Override
    protected void initData() {
        setTitle("登录");
        List<String> dataset = new LinkedList<>(Arrays.asList("个人用户", "监狱用户"));
        ns_login_type.attachDataSource(dataset);
        setRegistVisiblity(View.VISIBLE);
        ns_login_type.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ns_login_type.setText("个人用户");
                        LoadingActivity.this.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_load, personLoginFragment).commit();
                        isPerson = true;
                        break;
                    case 1:
                        ns_login_type.setText("监狱用户");
                        prisonLoginFragment = new PrisonLoginFragment();
                        LoadingActivity.this.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_load, prisonLoginFragment).commit();
                        isPerson = false;
                        break;
                }
            }
        });
        personLoginFragment = new PersonLoginFragment();
        this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoginFragment).commit();
        SPUtil.put(this, "is_first", false);
    }
}
