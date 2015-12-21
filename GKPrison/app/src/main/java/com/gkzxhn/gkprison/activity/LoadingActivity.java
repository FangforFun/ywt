package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.widget.AdapterView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.fragment.PersonLoadingFragment;
import com.gkzxhn.gkprison.fragment.PrisonLoadingFragment;

/**
 * 登录页面
 */
public class LoadingActivity extends BaseActivity {

    private PersonLoadingFragment personLoadingFragment;
    private PrisonLoadingFragment prisonLoadingFragment;

    @Override
    protected View initView() {
        View view = View.inflate(this,R.layout.activity_loading,null);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("登录");
        setRegistVisiblity(View.VISIBLE);
        personLoadingFragment = new PersonLoadingFragment();
        this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoadingFragment).commit();
        rl_regist.setOnClickListener(this);
        spinner_user_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        LoadingActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, personLoadingFragment).commit();
                        tv_user_type.setText("个人用户");
                        break;
                    case 1:
                        prisonLoadingFragment = new PrisonLoadingFragment();
                        LoadingActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fl_load, prisonLoadingFragment).commit();
                        tv_user_type.setText("监狱用户");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.rl_regist:
                spinner_user_type.performClick();
                break;
        }
    }
}
