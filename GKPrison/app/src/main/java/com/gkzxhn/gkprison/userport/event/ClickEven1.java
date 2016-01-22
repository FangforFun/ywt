package com.gkzxhn.gkprison.userport.event;

import java.util.List;

/**
 * Created by admin on 2016/1/19.
 */
public class ClickEven1 {
    private List<Integer> envntlist;

    public ClickEven1(List<Integer> envntlist) {
        this.envntlist = envntlist;
    }
    public List<Integer> getList(){
        return envntlist;
    }
}
