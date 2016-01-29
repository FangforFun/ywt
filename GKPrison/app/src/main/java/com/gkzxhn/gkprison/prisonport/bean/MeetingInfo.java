package com.gkzxhn.gkprison.prisonport.bean;

/**
 * Created by zhengneng on 2016/1/8.
 */
public class MeetingInfo {

    String name;
    String prison_area;
    String meeting_started;
    String meeting_finished;
    String prisoner_number;
    int family_id;
    int id;

    public String getPrisoner_number() {
        return prisoner_number;
    }

    public void setPrisoner_number(String prisoner_number) {
        this.prisoner_number = prisoner_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrison_area() {
        return prison_area;
    }

    public void setPrison_area(String prison_area) {
        this.prison_area = prison_area;
    }

    public String getMeeting_started() {
        return meeting_started;
    }

    public void setMeeting_started(String meeting_started) {
        this.meeting_started = meeting_started;
    }

    public String getMeeting_finished() {
        return meeting_finished;
    }

    public void setMeeting_finished(String meeting_finished) {
        this.meeting_finished = meeting_finished;
    }

    public int getFamily_id() {
        return family_id;
    }

    public void setFamily_id(int family_id) {
        this.family_id = family_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
