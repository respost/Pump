package com.xiao7.pump.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
/**
 * 权限管理工具
 * 快速解决权系统功能权限申请问题
 * 一个个权限申请也太麻烦，这个批量申请能满足你的需求。
 * 程序在android6.0版本以上（大于23）时，权限需要申请，在配置表中设置的权限当拒绝后，导至功能失常，或闪退现象
 * 调用checkPermission  申请权限回时调用 onRequestPermissionsResult
 */
public class PermissionUtils {

    static String[] permissions;
    /**
     *  检测权限是否己申请，通过在启动应用程序时检测，当需要时，会自动弹出提示请求权限
     * @param context 请求的 Activity
     * @param pms 关键的权限，也就是必须要的权限 当为null 时，申请的全部权限都是必需的
     * @return 需要请求返回 false  ，返回true时 全部权限都拿到
     */
    static  public boolean checkPermission(Activity context,String[] pms){
        int permissionCode=0;
        int decode=0;
        try {
            // APP 权限
            permissions = (pms!=null)?pms:context.getPackageManager().getPackageInfo(context.getPackageName(),PackageManager.GET_PERMISSIONS).requestedPermissions;
            for(int i=0;i<permissions.length;i++){
                System.out.println("PermissionUtil: " +permissions[i] );
                //判断用户是否给这些权限授权
                if(ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionCode+=1;
                    //判断是否拒绝过
                    decode += ActivityCompat.shouldShowRequestPermissionRationale(context, permissions[i]) ? 1 : 0;
                }
            }
        }catch (Exception e){
            System.out.println("PermissionUtil checkPermission error :"+e.getMessage());
        }
        if(permissionCode>0) {
            if(decode>0)
                //提示请求权限
                requestTip(context);
            else
                requestPermissions(context,permissions);
            return false;
        }
        return true;
    }

    /**
     * 请求获取权限 当权限需要申请时调用
     * @param context 请求的 Activity
     * @param perms 需要请求的权限
     */
    static  public  void requestPermissions(Activity context,String[] perms){
        ActivityCompat.requestPermissions(context,  perms,  1);
    }

    /**
     * 引导用户到设置权限
     * @param context 请求的 Activity
     */
    static public void toSetting(Context context){
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }

    /**
     *  提示请求权限 当拒绝了授权后，为提升用户体验，可以以弹窗的方式引导用户到设置中去进行设置
     * @param context  请求的 Activity
     */
    static  public void  requestTip(final Activity context){
        new AlertDialog.Builder(context)
                .setMessage("为了更好的体验，需要在设置里开启权限！")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toSetting(context);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * 请求权限回调检测 如有拒绝 将直接引导到系统设置要求打开
     * @param context  请求的 Activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    static public void onRequestPermissionsResult(Activity context,int requestCode,   String[] permissions,  int[] grantResults) {
        if(requestCode!=1)return;
        int code=0;
        if (grantResults.length <1)return;
        //用户是否拒绝了权限
        for (int i=0;i<grantResults.length;i++) {
            code += (grantResults[i] != PackageManager.PERMISSION_GRANTED) ? 1 : 0;
        }
        if(code==0){
            return;
        }
        requestTip(context);
    }

    /**
     *  设置中打开权限管理界面
     *  跟据不同厂商进行快捷目录界面
     * @param context
     */
    public static void gotoPermissionManager(Context context) {
        String brand = Build.BRAND;//手机厂商
        if (TextUtils.equals(brand.toLowerCase(), "redmi")
                || TextUtils.equals(brand.toLowerCase(), "xiaomi")) {
            gotoMiuiPermissionManager(context);//小米
            return;
        }
        if (TextUtils.equals(brand.toLowerCase(), "meizu")) {
            gotoMeizuPermissionManager(context);
            return;
        }
        if (TextUtils.equals(brand.toLowerCase(), "huawei")
                || TextUtils.equals(brand.toLowerCase(), "honor")) {
            gotoHuaweiPermissionManager(context);
            return;
        }
        toSetting(context);
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private static void gotoMeizuPermissionManager(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", context.getPackageName());
            context.startActivity(intent);
        } catch (Exception e) {
            toSetting(context);
        }
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private static void gotoMiuiPermissionManager(Context context) {
        try { // MIUI 8
            Intent localIntent = new Intent();
            localIntent.setAction("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(localIntent);
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(localIntent);
            } catch (Exception e1) { // 否则跳转到应用详情
                toSetting(context);
            }
        }
    }

    /**
     * 华为的权限管理页面
     */
    private static void gotoHuaweiPermissionManager(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            toSetting(context);
        }

    }

}
