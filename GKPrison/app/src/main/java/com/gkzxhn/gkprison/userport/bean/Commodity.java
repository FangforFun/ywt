package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by admin on 2015/12/15.
 */
public class Commodity {

    private String commodityclass;
    private String price;
    private int id;
    private int num;
    private String avatar_url;
    private String description;
    private String title;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getCommodityclass() {
        return commodityclass;
    }

    public void setCommodityclass(String commodityclass) {
        this.commodityclass = commodityclass;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
