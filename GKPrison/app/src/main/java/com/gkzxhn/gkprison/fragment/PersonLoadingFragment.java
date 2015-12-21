package com.gkzxhn.gkprison.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.activity.MainActivity;
import com.gkzxhn.gkprison.activity.RegisterActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PersonLoadingFragment extends BaseFragment {

    private Button bt_register;
    private Button btn_login;

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_person_loading, null);
        bt_register = (Button) view.findViewById(R.id.bt_register);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        return view;
    }

    @Override
    protected void initData() {
        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }
}
