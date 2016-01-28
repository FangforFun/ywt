package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by Administrator on 2016/1/27.
 * 版本信息bean
 */
public class VersionInfo {

    String created_at;
    String updated_at;
    int id;
    String version_name;
    int version_code;
    String contents;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", id=" + id +
                ", version_name='" + version_name + '\'' +
                ", version_code=" + version_code +
                ", contents='" + contents + '\'' +
                '}';
    }
}
