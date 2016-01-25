package com.gkzxhn.gkprison.prisonport.bean;

import java.util.List;

/**
 * Created by zhengneng on 2016/1/8.
 */
public class MeetingInfo {

    List<Info> applies;


    public class Info {
        int apply_id;
        int family_id;
        String prisoner_name;
        String prisoner_number;
        String prisoner_district;
        String meeting_started;
        String meeting_finished;

        public int getApply_id() {
            return apply_id;
        }

        public void setApply_id(int apply_id) {
            this.apply_id = apply_id;
        }

        public int getFamily_id() {
            return family_id;
        }

        public void setFamily_id(int family_id) {
            this.family_id = family_id;
        }

        public String getPrisoner_name() {
            return prisoner_name;
        }

        public void setPrisoner_name(String prisoner_name) {
            this.prisoner_name = prisoner_name;
        }

        public String getPrisoner_number() {
            return prisoner_number;
        }

        public void setPrisoner_number(String prisoner_number) {
            this.prisoner_number = prisoner_number;
        }

        public String getMeeting_started() {
            return meeting_started;
        }

        public void setMeeting_started(String meeting_started) {
            this.meeting_started = meeting_started;
        }

        public String getPrisoner_district() {
            return prisoner_district;
        }

        public void setPrisoner_district(String prisoner_district) {
            this.prisoner_district = prisoner_district;
        }

        public String getMeeting_finished() {
            return meeting_finished;
        }

        public void setMeeting_finished(String meeting_finished) {
            this.meeting_finished = meeting_finished;
        }
    }

    public List<Info> getApplies() {
        return applies;
    }

    public void setApplies(List<Info> applies) {
        this.applies = applies;
    }
}
