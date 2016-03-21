package com.gkzxhn.gkprison.userport.bean;

import java.util.List;

/**
 * 订单全部信息
 * Created by admin on 2015/12/29.
 */
public class Order {
    private int jail_id;
    private List<line_items_attributes> line_items_attributes;
    private String created_at;
    private int amount;
    private int family_id;

    public int getFamily_id() {
        return family_id;
    }

    public void setFamily_id(int family_id) {
        this.family_id = family_id;
    }


    public float getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public List<com.gkzxhn.gkprison.userport.bean.line_items_attributes> getLine_items_attributes() {
        return line_items_attributes;
    }

    public void setLine_items_attributes(List<com.gkzxhn.gkprison.userport.bean.line_items_attributes> line_items_attributes) {
        this.line_items_attributes = line_items_attributes;
    }

    public int getJail_id() {
        return jail_id;
    }

    public void setJail_id(int jail_id) {
        this.jail_id = jail_id;
    }
}
