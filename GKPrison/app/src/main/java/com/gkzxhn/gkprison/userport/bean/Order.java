package com.gkzxhn.gkprison.userport.bean;

import java.util.List;

/**
 * 订单全部信息
 * Created by admin on 2015/12/29.
 */
public class Order {
    private int jail_id;
    private List<line_items_attributes> items;
    private String out_trade_no;
    private String created_at;
    private float amount;
    private String ip;
    private int family_id;

    public int getFamily_id() {
        return family_id;
    }

    public void setFamily_id(int family_id) {
        this.family_id = family_id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public List<line_items_attributes> getItems() {
        return items;
    }

    public void setItems(List<line_items_attributes> items) {
        this.items = items;
    }

    public int getJail_id() {
        return jail_id;
    }

    public void setJail_id(int jail_id) {
        this.jail_id = jail_id;
    }


    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }
}
