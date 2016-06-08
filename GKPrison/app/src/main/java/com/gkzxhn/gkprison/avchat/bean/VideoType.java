package com.gkzxhn.gkprison.avchat.bean;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by Administrator on 2016/5/26.
 */
public class VideoType implements KvmSerializable {

    String type;

    public VideoType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Object getProperty(int i) {
        return type;
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void setProperty(int i, Object o) {
        type = (String) o;
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.type = type.getClass();
        propertyInfo.name = "type";
    }
}
