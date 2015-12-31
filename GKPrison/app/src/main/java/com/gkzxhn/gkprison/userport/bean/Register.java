package com.gkzxhn.gkprison.userport.bean;

import java.util.List;

/**
 * 所有注册信息放入此对象，方便转成json;
 * Created by admin on 2015/12/31.
 */
public class Register {
    private String name;
    private String uuid;
    private String phone;
    private String relationship;
    private String prisoner_number;
    private String prison;
    private String jail_id;
    private String type_id;
    private String code;
    private List<Uuid_images_attributes> uuid_images_attributes;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPrison() {
        return prison;
    }

    public void setPrison(String prison) {
        this.prison = prison;
    }

    public String getJail_id() {
        return jail_id;
    }

    public void setJail_id(String jail_id) {
        this.jail_id = jail_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPrisoner_number() {
        return prisoner_number;
    }

    public void setPrisoner_number(String prisoner_number) {
        this.prisoner_number = prisoner_number;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<Uuid_images_attributes> getUuid_images_attributes() {
        return uuid_images_attributes;
    }

    public void setUuid_images_attributes(List<Uuid_images_attributes> uuid_images_attributes) {
        this.uuid_images_attributes = uuid_images_attributes;
    }
}
