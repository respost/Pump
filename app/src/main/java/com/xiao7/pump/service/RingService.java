package com.xiao7.pump.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.xiao7.pump.MainActivity;
import com.xiao7.pump.service.inter.RingInterface;

import java.util.Calendar;

public class RingService extends Service {
    /**
     * 通过bindService()绑定到服务的客户端
     */
    @Override
    public IBinder onBind(Intent intent) {

        return new MyBinder();
    }

    /**
     * 自定义的绑定器
     */
    class MyBinder extends Binder implements RingInterface {

        @Override
        public void ringOn(Context context, AlarmManager alarmManager, Calendar calendar) {
            startService(context, alarmManager, calendar);
        }

        @Override
        public void ringOff(AlarmManager alarmManager) {
            cancelService(alarmManager);
        }
    }

    private PendingIntent pi;

    /**
     * 打开服务
     *
     * @param context
     * @param alarmManager
     * @param calendar
     */
    private void startService(Context context, AlarmManager alarmManager, Calendar calendar) {
        /**
         * 在Service服务类中发送广播消息给Activity活动界面
         */
        Intent intent = new Intent();
        //设置意图过虑器Action（用来区分广播来源，相当于是广播的身份证）
        intent.setAction(MainActivity.ACTION_SERVICE_NEED);
        //添加NEW_TASK标志位（必须加这个，否则不能在锁屏下实现消息提醒）
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //PendingIntent是待确定的意图（等待的意图）
        pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        //版本大于Android 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
        //版本大于Android 4.4
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //单次闹钟：
            //alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            //重复闹钟：
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5 * 1000,pi);
        } else {
            /*重复闹钟*/
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5 * 1000, pi);
        }
    }

    /**
     * 取消服务
     *
     * @param alarmManager
     */
    private void cancelService(AlarmManager alarmManager) {
        alarmManager.cancel(pi);
    }

}
