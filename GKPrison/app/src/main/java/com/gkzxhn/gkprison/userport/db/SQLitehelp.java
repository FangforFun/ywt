package com.gkzxhn.gkprison.userport.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhangjia on 16/5/10.
 */
public class SQLitehelp extends SQLiteOpenHelper {
    public SQLitehelp(Context context){
        super(context, "chaoshi.db", null, 1);

    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Cart(id INTEGER AUTO_INCREMENT,time VARCHAR2(60),out_trade_no VARCHAR2(60),isfinish BOOLEAN(30),total_money VARCHAR2(60),count INTEGER(60),remittance BOOLEAN(30),payment_type varchar(255))");
        db.execSQL("CREATE TABLE line_items(id INTEGER AUTO_INCREMENT,Items_id INTEGER(60),cart_id INTEGER,qty INTEGER(60),total_money VARCHAR2(60),position INTEGER(60),price varchar[255],title varchar(255))");
        db.execSQL("create table sysmsg(apply_date VARCHAR2,name VARCHAR2,result VARCHAR2,is_read VARCHAR2,meeting_date VARCHAR2, type_id INTEGER,reason VARCHAR2,receive_time VARCHAR2,user_id VARCHAR2)");

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
