package com.xiao7.pump.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.xiao7.pump.MainActivity;
import com.xiao7.pump.R;

/**
 * 通知栏工具类
 */
public class NotifyUtils {
    /**
     * 发送通知
     * @param context
     * @param channelId 消息通道ID ：String类型，每个包必须是唯一的，如果值太长，可能会被截断
     * @param channelName 消息通道名称
     * @param contentTitle 通知标题
     * @param contentText 通知内容
     * @param isVibrate 是否震动
     */
    public static void sendNotice(Context context,String channelId,String channelName,String contentTitle,String contentText, boolean isVibrate){
        //默认通道ID是default
        String cid ="default";
        //1.获取消息服务
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //2.如果是android8.0以上的系统，则新建一个消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //新的通道ID
            cid= (channelId == null ||"".equals(channelId)) ? "chat" : channelId;
            //通道名称
            channelName= (channelName == null ||"".equals(channelName)) ? "消息提醒" : channelName;
            /*
             通道优先级别：
             * IMPORTANCE_NONE 关闭通知
             * IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
             * IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
             * IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
             * IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
             */
            NotificationChannel channel = new NotificationChannel(cid, channelName, NotificationManager.IMPORTANCE_HIGH);
            //设置该通道的描述（可以不写）
            //channel.setDescription("重要消息，请不要关闭这个通知。");
            //是否绕过勿打扰模式
            channel.setBypassDnd(true);
            //是否允许呼吸灯闪烁
            channel.enableLights(true);
            //闪关灯的灯光颜色
            channel.setLightColor(Color.RED);
            //桌面launcher的消息角标
            channel.canShowBadge();
            //设置是否在锁定屏幕上显示此频道的通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            if (isVibrate) {
                //是否允许震动
                channel.enableVibration(true);
                //先震动1秒，然后停止0.5秒，再震动2秒则可设置数组为：new long[]{1000, 500, 2000}
                channel.setVibrationPattern(new long[]{1000, 500, 2000});
            } else {
                channel.enableVibration(false);
                channel.setVibrationPattern(new long[]{0});
            }
            //创建消息通道
            manager.createNotificationChannel(channel);
        }
        //3.实例化通知
        NotificationCompat.Builder nc = new NotificationCompat.Builder(context, cid);
        //通知默认的声音 震动 呼吸灯
        nc.setDefaults(NotificationCompat.DEFAULT_ALL);
        //通知标题
        nc.setContentTitle(contentTitle);
        //通知内容
        nc.setContentText(contentText);
        //设置通知的小图标
        nc.setSmallIcon(android.R.drawable.ic_popup_reminder);
        //设置通知的大图标
        nc.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        //设定通知显示的时间
        nc.setWhen(System.currentTimeMillis());
        //设置通知的优先级
        nc.setPriority(NotificationCompat.PRIORITY_MAX);
        //设置点击通知之后通知是否消失
        nc.setAutoCancel(true);
        //点击通知打开软件
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
        nc.setContentIntent(pendingIntent);
        //4.创建通知，得到build
        Notification notification = nc.build();
        //5.发送通知
        manager.notify(1, notification);
    }

}
