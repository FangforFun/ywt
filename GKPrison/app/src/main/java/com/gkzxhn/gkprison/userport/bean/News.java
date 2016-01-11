package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by admin on 2016/1/7.
 */
public class News {

    /**
     * id : 2
     * title : 【刑法执行】昭通市食药监局到昭通监狱开展警示教育
     * contents : <b><i></i><p></p></b><p>为深入践行“三严三实”和“忠诚干净担当”专题教育实践活动，加强系统党风廉政建设，提高干部拒腐防变能力</p><b><p></p><i></i></b><br><br>
     * isFocus : null
     * jail_id : 1
     * created_at : 2015-12-23T02:03:57.000Z
     * updated_at : 2016-01-07T08:22:54.000Z
     * image_file_name : null
     * image_content_type : null
     * image_file_size : null
     * image_updated_at : null
     */

    private int id;
    private String title;
    private String contents;
    private Object isFocus;
    private int jail_id;
    private String created_at;
    private String updated_at;
    private Object image_file_name;
    private Object image_content_type;
    private Object image_file_size;
    private Object image_updated_at;
    private String avatar_url;

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setIsFocus(Object isFocus) {
        this.isFocus = isFocus;
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

    public void setImage_file_name(Object image_file_name) {
        this.image_file_name = image_file_name;
    }

    public void setImage_content_type(Object image_content_type) {
        this.image_content_type = image_content_type;
    }

    public void setImage_file_size(Object image_file_size) {
        this.image_file_size = image_file_size;
    }

    public void setImage_updated_at(Object image_updated_at) {
        this.image_updated_at = image_updated_at;
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

    public Object getIsFocus() {
        return isFocus;
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

    public Object getImage_file_name() {
        return image_file_name;
    }

    public Object getImage_content_type() {
        return image_content_type;
    }

    public Object getImage_file_size() {
        return image_file_size;
    }

    public Object getImage_updated_at() {
        return image_updated_at;
    }
}
