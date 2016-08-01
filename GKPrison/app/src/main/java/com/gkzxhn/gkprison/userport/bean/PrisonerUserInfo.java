package com.gkzxhn.gkprison.userport.bean;

import java.util.List;

/**
 * author:huangzhengneng
 * email:943852572@qq.com
 * date: 2016/8/1.
 * function:
 */

public class PrisonerUserInfo {

    /**
     * prisoner_number : 4000022
     * name : 张三22
     * gender : m
     * jail_id : 11
     * prison_term_started_at : 2009-01-07
     * prison_term_ended_at : 2019-01-07
     * prison_area : 第三监区
     * access_token : bdd997d3e0de28d87c0ea1fdef744dd7
     */

    private List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private String prisoner_number;
        private String name;
        private String gender;
        private int jail_id;
        private String prison_term_started_at;
        private String prison_term_ended_at;
        private String prison_area;
        private String access_token;

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

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public int getJail_id() {
            return jail_id;
        }

        public void setJail_id(int jail_id) {
            this.jail_id = jail_id;
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

        public String getPrison_area() {
            return prison_area;
        }

        public void setPrison_area(String prison_area) {
            this.prison_area = prison_area;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        @Override
        public String toString() {
            return "ResultBean{" +
                    "prisoner_number='" + prisoner_number + '\'' +
                    ", name='" + name + '\'' +
                    ", gender='" + gender + '\'' +
                    ", jail_id=" + jail_id +
                    ", prison_term_started_at='" + prison_term_started_at + '\'' +
                    ", prison_term_ended_at='" + prison_term_ended_at + '\'' +
                    ", prison_area='" + prison_area + '\'' +
                    ", access_token='" + access_token + '\'' +
                    '}';
        }
    }
}
