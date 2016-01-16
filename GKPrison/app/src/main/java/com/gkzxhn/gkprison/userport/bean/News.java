package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by admin on 2016/1/7.
 */
public class News {


    /**
     * id : 1
     * title : 【刑法执行】昭通市食药监局到昭通监狱开展警示教育
     * contents : 为深入践行“三严三实”和“忠诚干净担当”专题教育实践活动，加强系统党风廉政建设，提高干部拒腐防变能力<br><b><p></p><i></i></b><br><br><br><br><br><br><br><br>
     * isFocus : true
     * jail_id : 1
     * created_at : 2015-12-23T02:03:57.000Z
     * updated_at : 2016-01-16T09:03:23.000Z
     * image_file_name : QQ截图20160116170130.png
     * image_content_type : image/png
     * image_file_size : 37975
     * image_updated_at : 2016-01-16T09:03:23.000Z
     * image_url : /system/news/images/000/000/001/medium/QQ%E6%88%AA%E5%9B%BE20160116170130.png?1452935003
     * type_id : 1
     */

    private int id;
    private String title;
    private String contents;
    private boolean isFocus;
    private int jail_id;
    private String created_at;
    private String updated_at;
    private String image_file_name;
    private String image_content_type;
    private int image_file_size;
    private String image_updated_at;
    private String image_url;
    private int type_id;

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setIsFocus(boolean isFocus) {
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

    public void setImage_file_name(String image_file_name) {
        this.image_file_name = image_file_name;
    }

    public void setImage_content_type(String image_content_type) {
        this.image_content_type = image_content_type;
    }

    public void setImage_file_size(int image_file_size) {
        this.image_file_size = image_file_size;
    }

    public void setImage_updated_at(String image_updated_at) {
        this.image_updated_at = image_updated_at;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
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

    public boolean isIsFocus() {
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

    public String getImage_file_name() {
        return image_file_name;
    }

    public String getImage_content_type() {
        return image_content_type;
    }

    public int getImage_file_size() {
        return image_file_size;
    }

    public String getImage_updated_at() {
        return image_updated_at;
    }

    public String getImage_url() {
        return image_url;
    }

    public int getType_id() {
        return type_id;
    }
}
