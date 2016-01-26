package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by admin on 2016/1/21.
 */
public class Remittance {
    private String times;
    private String price;
    private int cart_id;

    public int getCart_id() {
        return cart_id;
    }

    public void setCart_id(int cart_id) {
        this.cart_id = cart_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }
}
