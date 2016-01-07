package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by admin on 2016/1/7.
 */
public class Letter {

    /**
     * theme : 主题
     * content : 信件内容
     */

    private String theme;
    private String contents;
    private  int jail_id;

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public int getJail_id() {
        return jail_id;
    }

    public void setJail_id(int jail_id) {
        this.jail_id = jail_id;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }



    public String getTheme() {
        return theme;
    }


}
