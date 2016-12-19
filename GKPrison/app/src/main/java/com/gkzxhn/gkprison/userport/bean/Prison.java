package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by zhangjia on 16/2/25.
 */
public class Prison {
    private String prisoner_number;
    private String gender;
    private String crimes;
    private String prison_term_started_at;
    private String prison_term_ended_at;

    public String getPrisoner_number() {
        return prisoner_number;
    }

    public void setPrisoner_number(String prisoner_number) {
        this.prisoner_number = prisoner_number;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCrimes() {
        return crimes;
    }

    public void setCrimes(String crimes) {
        this.crimes = crimes;
    }

    public String getPrison_term_started_at() {
        return prison_term_started_at;
    }

    public void setPrison_term_started_at(String prison_term_started_at) {
        this.prison_term_started_at = prison_term_started_at;
    }

    public String getPrison_term_ended_at() {
        return prison_term_ended_at;
    }

    public void setPrison_term_ended_at(String prison_term_ended_at) {
        this.prison_term_ended_at = prison_term_ended_at;
    }
}
