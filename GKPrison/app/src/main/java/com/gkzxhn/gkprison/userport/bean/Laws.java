package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by zhangjia on 2016/1/6.
 */
public class Laws {

    /**
     * id : 1
     * title : 中华人民共和国监狱法
     * contents : <p></p>第一章　总则<br>第一条　为了正确执行刑罚，惩罚和改造罪犯，预防和减少犯罪，根据宪法，制定本法。<br>第二条　监狱是国家的刑罚执行机关。<br>依照刑法和刑事诉讼法的规定，被判处死刑缓期二年执行、无期徒刑、有期徒刑的罪犯，在监狱内执行刑罚。<br>第三条　监狱对罪犯实行惩罚和改造相结合、教育和劳动相结合的原则，将罪犯改造成为守法公民。<br><br>
     * jail_id : 1
     * created_at : 2015-12-25T06:48:59.000Z
     * updated_at : 2016-01-06T02:59:43.000Z
     */

    private int id;
    private String title;
    private String contents;
    private int jail_id;
    private String created_at;
    private String updated_at;

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setJail_id(int jail_id) {
        this.jail_id = jail_id;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public int getJail_id() {
        return jail_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
