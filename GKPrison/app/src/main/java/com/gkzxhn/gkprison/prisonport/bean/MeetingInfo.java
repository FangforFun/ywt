package com.gkzxhn.gkprison.prisonport.bean;

/**
 * Created by zhengneng on 2016/1/8.
 */
public class MeetingInfo {

    String name;
    String phone;
    String prisoner_number;
    String relationship;
    String uuid;
    String reply_date;
    String prison_area;
    String access_token;
    String prisoner_name;
    String image_url;

    public String getPrisoner_name() {
        return prisoner_name;
    }

    public void setPrisoner_name(String prisoner_name) {
        this.prisoner_name = prisoner_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrisoner_number() {
        return prisoner_number;
    }

    public void setPrisoner_number(String prisoner_number) {
        this.prisoner_number = prisoner_number;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReply_date() {
        return reply_date;
    }

    public void setReply_date(String reply_date) {
        this.reply_date = reply_date;
    }

    public String getPrison_area() {
        return prison_area;
    }

    public void setPrison_area(String prison_area) {
        this.prison_area = prison_area;
    }
}
