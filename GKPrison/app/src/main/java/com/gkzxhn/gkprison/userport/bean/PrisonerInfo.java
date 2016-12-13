package com.gkzxhn.gkprison.userport.bean;

/**
 * Author: Huang ZN
 * Date: 2016/12/13
 * Email:943852572@qq.com
 * Description:家属服务页面的囚犯信息bean
 */

public class PrisonerInfo {

    /**
     * code : 200
     * prisoner : {"created_at":"2016-01-09T02:01:28.000Z","crimes":"抢劫","gender":"m","id":20,"isvisit":1,"jail_id":11,"name":"张三20","prison_area":"第三监区","prison_term_ended_at":"2019-01-07","prison_term_started_at":"2009-01-07","prisoner_number":"4000020","updated_at":"2016-01-09T02:01:28.000Z"}
     */

    private int code;
    /**
     * created_at : 2016-01-09T02:01:28.000Z
     * crimes : 抢劫
     * gender : m
     * id : 20
     * isvisit : 1
     * jail_id : 11
     * name : 张三20
     * prison_area : 第三监区
     * prison_term_ended_at : 2019-01-07
     * prison_term_started_at : 2009-01-07
     * prisoner_number : 4000020
     * updated_at : 2016-01-09T02:01:28.000Z
     */

    private PrisonerBean prisoner;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public PrisonerBean getPrisoner() {
        return prisoner;
    }

    public void setPrisoner(PrisonerBean prisoner) {
        this.prisoner = prisoner;
    }

    public static class PrisonerBean {
        private String created_at;
        private String crimes;
        private String gender;
        private int id;
        private int isvisit;
        private int jail_id;
        private String name;
        private String prison_area;
        private String prison_term_ended_at;
        private String prison_term_started_at;
        private String prisoner_number;
        private String updated_at;

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getCrimes() {
            return crimes;
        }

        public void setCrimes(String crimes) {
            this.crimes = crimes;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getIsvisit() {
            return isvisit;
        }

        public void setIsvisit(int isvisit) {
            this.isvisit = isvisit;
        }

        public int getJail_id() {
            return jail_id;
        }

        public void setJail_id(int jail_id) {
            this.jail_id = jail_id;
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

        public String getPrison_term_ended_at() {
            return prison_term_ended_at;
        }

        public void setPrison_term_ended_at(String prison_term_ended_at) {
            this.prison_term_ended_at = prison_term_ended_at;
        }

        public String getPrison_term_started_at() {
            return prison_term_started_at;
        }

        public void setPrison_term_started_at(String prison_term_started_at) {
            this.prison_term_started_at = prison_term_started_at;
        }

        public String getPrisoner_number() {
            return prisoner_number;
        }

        public void setPrisoner_number(String prisoner_number) {
            this.prisoner_number = prisoner_number;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        @Override
        public String toString() {
            return "PrisonerBean{" +
                    "created_at='" + created_at + '\'' +
                    ", crimes='" + crimes + '\'' +
                    ", gender='" + gender + '\'' +
                    ", id=" + id +
                    ", isvisit=" + isvisit +
                    ", jail_id=" + jail_id +
                    ", name='" + name + '\'' +
                    ", prison_area='" + prison_area + '\'' +
                    ", prison_term_ended_at='" + prison_term_ended_at + '\'' +
                    ", prison_term_started_at='" + prison_term_started_at + '\'' +
                    ", prisoner_number='" + prisoner_number + '\'' +
                    ", updated_at='" + updated_at + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PrisonerInfo{" +
                "code=" + code +
                ", prisoner=" + prisoner.toString() +
                '}';
    }
}
