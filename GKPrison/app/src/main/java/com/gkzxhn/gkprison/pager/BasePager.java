package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * Created by hzn on 2015/11/16.
 * BasePager 任意页面的基类
 */
public abstract class BasePager implements View.OnClickListener{

    public Context context;
    public View view;

    public BasePager(Context context){
        this.context = context;
        view = initView();
    }

    /**
     * 构建UI
     * @return
     */
    public abstract View initView();

    /**
     * 填充数据
     */
    public abstract void initData();

    /**
     * 返回View效果
     * @return
     */
    public View getView(){
        return view;
    }

    /**
     * 弹出toast 显示时长short
     * @param pMsg
     */
    protected void showToastMsgShort(String pMsg) {
        Toast.makeText(context, pMsg, Toast.LENGTH_SHORT).show();
    }
    /**
     * 弹出toase 显示时长long
     * @param pMsg
     */
    protected void showToastMsgLong(String pMsg) {
        Toast.makeText(context, pMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        //子类实现
    }
}
