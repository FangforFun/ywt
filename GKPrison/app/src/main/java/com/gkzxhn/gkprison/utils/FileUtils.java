package com.gkzxhn.gkprison.utils;

import android.content.Context;
import android.os.Environment;

import com.gkzxhn.gkprison.userport.bean.News;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Author: Huang ZN
 * Date: 2016/12/29
 * Email:943852572@qq.com
 * Description:
 */

public class FileUtils {

    /**
     * 获取缓存文件夹
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }


    /**
     * 缓存新闻
     * @param news
     * @param context
     * @return
     */
    public static boolean cacheNews(News news, Context context, String keyContent) {
        File cacheDir = FileUtils.getDiskCacheDir(context, "news");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(cacheDir, SystemUtil.
                    getVersionCode(context), 20, 10 * 1024 * 1024);
            String key = MD5Utils.ecoder(keyContent);
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(news);
                editor.commit();
                os.close();
                return true;
            }
            diskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 取缓存新闻
     * @param context
     * @return
     */
    public static News getCacheNews(Context context, String keyContent) {
        File cacheDir = FileUtils.getDiskCacheDir(context, "news");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        News news = null;
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(cacheDir, SystemUtil.
                    getVersionCode(context), 20, 10 * 1024 * 1024);
            String key = MD5Utils.ecoder(keyContent);
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null){
                InputStream inputStream = snapshot.getInputStream(0);
                ObjectInputStream oi = new ObjectInputStream(inputStream);
                news = (News) oi.readObject();
                oi.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return news;
    }
}
