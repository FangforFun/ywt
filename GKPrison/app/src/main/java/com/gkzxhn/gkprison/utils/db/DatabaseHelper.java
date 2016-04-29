package com.gkzxhn.gkprison.utils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gkzxhn.gkprison.utils.Log;

/**
 * Created by huangzhengneng on 2016/4/29.
 * database helper
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String TAG = "DatabaseHelper";
    private static final int VERSION = 1;

    public DatabaseHelper(Context context,String name){
        this(context, name, VERSION);
    }

    public DatabaseHelper(Context context,String name,int version){
        this(context, name, null, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate --> create a table");
        db.execSQL("create table Cart(id integer primary key increment, time varchar2(60), out_trade_no varchar2(60)," +
                " isfinish Boolean(30), total_money varchar(60), count integer(60), remittance Boolean(30), " +
                "payment_type varchar(255))");
        db.execSQL("create table ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
