package com.xiao7.pump.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import com.xiao7.pump.R;

/**
 * 弹出框工具类
 */
public class DialogUtils {
    //定义一个AlertDialog，防止同时弹出多个AlertDialog
    private static AlertDialog alterDialog = null;
    //回调接口
    public interface ResultCallBack {
        public void callback(boolean flag);
    }

    /**
     * 简单对话框
     * @param context 上下文
     * @param msg 信息
     * @param noneCancel 是否为取消键，true为没有，false为有
     */
    public static void showDialog(Context context,String msg,boolean noneCancel) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(msg);
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if(!noneCancel) {
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        if(alterDialog==null){
            alterDialog = builder.create();
        }
        alterDialog.show();
    }

    /**
     * 带回调函数的对话框
     * @param context 上下文
     * @param msg 信息
     * @param noneCancel 是否为取消键，true为没有，false为有
     * @param resultCallBack 回调
     */
    public static void showAlterDialog(Context context,String msg,boolean noneCancel,final ResultCallBack resultCallBack) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(msg);
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (resultCallBack!= null) {
                    resultCallBack.callback(true);
                }
            }
        });
        if(!noneCancel) {
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (resultCallBack != null) {
                        resultCallBack.callback(false);
                    }
                }
            });
        }
        builder.setCancelable(false); //dialog弹出后，点击屏幕或物理返回键，dialog都不消失，只能点击确定或取消按键
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //屏蔽搜索键
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                } else {
                    //默认返回 false
                    return false;
                }
            }
        });
        if(alterDialog==null){
            alterDialog = builder.create();
        }
        alterDialog.show();
    }
}
