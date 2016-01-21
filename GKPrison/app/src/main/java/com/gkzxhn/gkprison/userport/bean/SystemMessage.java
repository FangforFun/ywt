package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by zhengneng on 2016/1/19.
 */
public class SystemMessage {

    String apply_date;
    String name;
    String result;
    boolean is_read;
    String meeting_date;
    int type_id;
    String reason;
    String msg_receive_time;
    String user_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMsg_receive_time() {
        return msg_receive_time;
    }

    public void setMsg_receive_time(String msg_receive_time) {
        this.msg_receive_time = msg_receive_time;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean is_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public String getMeeting_date() {
        return meeting_date;
    }

    public void setMeeting_date(String meeting_date) {
        this.meeting_date = meeting_date;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public String getApply_date() {
        return apply_date;
    }

    public void setApply_date(String apply_date) {
        this.apply_date = apply_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "SystemMessage{" +
                "apply_date='" + apply_date + '\'' +
                ", name='" + name + '\'' +
                ", result='" + result + '\'' +
                ", is_read=" + is_read +
                ", meeting_date='" + meeting_date + '\'' +
                ", type_id=" + type_id +
                ", reason='" + reason + '\'' +
                ", msg_receive_time='" + msg_receive_time + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
