package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by zhangjia on 2016/1/7.
 */
public class Letter {

    private LetterBean message;

    public class LetterBean {
        /**
         * theme : 主题
         * content : 信件内容
         */

        private String theme;
        private String contents;
        private  int jail_id;
        private  int family_id;

        public int getFamily_id() {
            return family_id;
        }

        public void setFamily_id(int family_id) {
            this.family_id = family_id;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public int getJail_id() {
            return jail_id;
        }

        public void setJail_id(int jail_id) {
            this.jail_id = jail_id;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }



        public String getTheme() {
            return theme;
        }

        @Override
        public String toString() {
            return "LetterBean{" +
                    "theme='" + theme + '\'' +
                    ", contents='" + contents + '\'' +
                    ", jail_id=" + jail_id +
                    ", family_id=" + family_id +
                    '}';
        }
    }
}
