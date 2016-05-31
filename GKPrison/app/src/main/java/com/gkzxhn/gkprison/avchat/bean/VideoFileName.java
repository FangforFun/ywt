package com.gkzxhn.gkprison.avchat.bean;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by huangzhengneng on 2016/5/26.
 */
public class VideoFileName implements KvmSerializable {

    private String fileName;

    public VideoFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Object getProperty(int i) {
        return fileName;
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void setProperty(int i, Object o) {
        fileName = (String) o;
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.type = fileName.getClass();
        propertyInfo.name = "fileName";
    }
}
