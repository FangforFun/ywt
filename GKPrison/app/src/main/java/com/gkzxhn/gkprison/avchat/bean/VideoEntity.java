package com.gkzxhn.gkprison.avchat.bean;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by huangzhengneng on 2016/5/26.
 * 视频实体类  用于发送至服务器
 */
public class VideoEntity implements KvmSerializable{

    private byte[] fileContent;
//    private String type;
//    private String fileName;

    public VideoEntity(byte[] fileContent){
//        this.fileName = fileName;
//        this.fileContent = fileContent;
        this.fileContent = fileContent;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }

    @Override
    public Object getProperty(int i) {
//        switch (i){
//            case 0:
//                return fileName;
//            case 1:
//                return type;
//            case 2:
                return fileContent;
//            default:
//                return null;
//        }
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void setProperty(int i, Object o) {
//        switch (i){
//            case 0:
//                fileName = (String) o;
//                break;
//            case 1:
//                type = (String) o;
//                break;
//            case 2:
                fileContent = (byte[]) o;
//                break;
//        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
//        switch (i){
//            case 0:
//                propertyInfo.type = fileName.getClass();
//                propertyInfo.name = "fileName";
//                break;
//            case 1:
//                propertyInfo.type = type.getClass();
//                propertyInfo.name = "type";
//                break;
//            case 2:
                propertyInfo.type = fileContent.getClass();
                propertyInfo.name = "fileContent";
//                break;
//            default:
//                break;
//        }
    }
}
