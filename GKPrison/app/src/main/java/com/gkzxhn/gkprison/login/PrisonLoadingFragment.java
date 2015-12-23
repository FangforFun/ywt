package com.gkzxhn.gkprison.login;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * 监狱用户登录界面
 */
public class PrisonLoadingFragment extends BaseFragment {

    private Button btn_login;

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_prison_loading, null);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        return view;
    }

    @Override
    protected void initData() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DateMeetingListActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }
}
