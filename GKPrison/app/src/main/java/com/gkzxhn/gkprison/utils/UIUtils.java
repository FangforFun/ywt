package com.gkzxhn.gkprison.utils;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import com.gkzxhn.gkprison.R;

/**
 * Author: Huang ZN
 * Date: 2016/12/26
 * Email:943852572@qq.com
 * Description:
 */

public class UIUtils {

    private static String defaultMsg = "请稍候...";

    /**
     *
     * @param context
     */
    public static ProgressDialog showProgressDialog(Context context){
        return showProgressDialog(context, defaultMsg);
    }

    /**
     *
     * @param context
     * @param msg
     */
    public static ProgressDialog showProgressDialog(Context context, String msg){
        if (context instanceof Application)
            throw new IllegalArgumentException("not support context");
        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            throw new IllegalStateException("must show dialog in main thread");
        }
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(msg);
        dialog.show();
        return dialog;
    }

    /**
     * dismiss
     * @param dialog
     */
    public static void dismissProgressDialog(ProgressDialog dialog){
        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            throw new IllegalStateException("this controller must in main thread");
        }
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    /**
     * show对话框  cancelable为默认true
     * @param context
     * @param msg
     * @param okListener
     * @param cancelListener
     * @return
     */
    public static AlertDialog showAlertDialog(Context context, String msg, DialogInterface.OnClickListener
            okListener, DialogInterface.OnClickListener cancelListener){
        return showAlertDialog(context, msg, okListener, cancelListener, true);
    }

    /**
     * show对话框  可传入是否cancelable
     * @param context
     * @param msg
     * @param okListener
     * @param cancelListener
     * @param cancelable
     * @return
     */
    public static AlertDialog showAlertDialog(Context context, String msg, DialogInterface.OnClickListener
            okListener, DialogInterface.OnClickListener cancelListener, boolean cancelable){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", okListener)
                .setCancelable(cancelable);
        if (cancelListener != null) {
            builder.setNegativeButton("取消", cancelListener);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    /**
     * 显示列表对话框
     * @param context
     * @param titleMsg
     * @param items
     * @param listener
     * @return
     */
    public static AlertDialog showListDialog(Context context, String titleMsg,
           String[] items, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(titleMsg)
                .setItems(items, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    /**
     * 协议对话框
     */
    public static AlertDialog showSoftProtocolDialog(Context context) {
        if (context instanceof Application)
            throw new IllegalArgumentException("not support context");
        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            throw new IllegalStateException("must show dialog in main thread");
        }
        AlertDialog.Builder agreement_builder = new AlertDialog.Builder(context);
        View agreement_view = View.inflate(context, R.layout.software_agreement_dialog, null);
        Button btn_ok = (Button) agreement_view.findViewById(R.id.btn_ok);
        final AlertDialog agreement_dialog = agreement_builder.create();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agreement_dialog.dismiss();
            }
        });
        agreement_dialog.setCancelable(true);
        agreement_builder.setView(agreement_view);
        agreement_builder.show();
        return agreement_dialog;
    }

    /**
     * 显示确认提交注册的提示对话框
     */
    public static AlertDialog showConfirmDialog(Context context, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("即将提交注册，注册信息将会严格审核，注册信息一旦通过，将不可修改，确定提交？");
        builder.setPositiveButton("确定", okListener).setNegativeButton("再确认一下", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog register_remind_dialog = builder.create();
        register_remind_dialog.show();
        return register_remind_dialog;
    }

}
