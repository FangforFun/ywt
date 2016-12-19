package com.gkzxhn.gkprison.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * add by hzn 2015.12.03
 * Fragment基类
 */
public abstract class BaseFragment extends Fragment {

    public Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    //view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //填充数据
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 子类必须实现  即布局
     * @return
     */
    protected abstract View initView();

    /**
     * 子类要想显示动态数据必须实现
     */
    protected abstract void initData();

    /**
     * 弹出toast 显示时长short
     * @param pMsg
     */
    protected void showToastMsgShort(String pMsg) {
        Toast.makeText(getActivity(), pMsg, Toast.LENGTH_SHORT).show();
    }
    /**
     * 弹出toase 显示时长long
     * @param pMsg
     */
    protected void showToastMsgLong(String pMsg) {
        Toast.makeText(getActivity(), pMsg, Toast.LENGTH_LONG).show();
    }
}
