package com.gkzxhn.gkprison.userport.bean;

import java.util.List;

/**
 * Created by admin on 2015/12/29.
 */
public class Order {
    private int jail_id;
    private List<Items> items;
    private String token;
    private String out_trade_no;

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public int getJail_id() {
        return jail_id;
    }

    public void setJail_id(int jail_id) {
        this.jail_id = jail_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }
}
