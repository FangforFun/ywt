package com.gkzxhn.gkprison.pager;

import android.content.Context;
import android.view.View;

/**
 * Created by hzn on 2015/11/16.
 * BasePager 任意页面的基类
 */
public abstract class BasePager {

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

}
