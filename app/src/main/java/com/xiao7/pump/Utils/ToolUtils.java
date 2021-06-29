package com.xiao7.pump.Utils;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;

/**
 * 通用工具类
 */
public class ToolUtils {
    private static Vibrator vibrator;
    private static PowerManager.WakeLock wakeLock;

    /**
     * 唤醒手机屏幕并解锁
     *
     * @param context
     */
    @SuppressLint("InvalidWakeLockTag")
    public static void acquire(Context context) {
        try {
            //获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象
            wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            //点亮屏幕30秒
            wakeLock.acquire(30 * 1000);
            //灭屏（释放锁）
            if (null != wakeLock) {
                wakeLock.release();
            }
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            //这里参数”unLock”作为调试时LogCat中的Tag
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
            //解锁
            kl.disableKeyguard();
        } catch (Exception ex) {
        }
    }

    /**
     * 手机震动
     *
     * @param context
     * @param isRepeat 是否重复震动
     */
    public static void playVibrate(Context context, boolean isRepeat) {

        /*
         * 设置震动，用一个long的数组来表示震动状态（以毫秒为单位）
         * 如果要设置先震动1秒，然后停止0.5秒，再震动2秒则可设置数组为：long[]{1000, 500, 2000}。
         * 别忘了在AndroidManifest配置文件中申请震动的权限
         */
        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            long[] patern = new long[]{1000, 500, 2000};
            AudioAttributes audioAttributes = null;
            /**
             * 适配android7.0以上版本的震动
             * 说明：如果发现5.0或6.0版本在app退到后台之后也无法震动，那么只需要改下方的Build.VERSION_CODES.N版本号即可
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM) //key
                        .build();
                vibrator.vibrate(patern, isRepeat ? 1 : -1, audioAttributes);
            }else {
                vibrator.vibrate(patern, isRepeat ? 1 : -1);
            }
        } catch (Exception ex) {
        }
    }

    /**
     * 关闭震动
     */
    public static void closeVibrate() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    /**
     * 播放系统默认提示音
     *
     * @return MediaPlayer对象
     * @throws Exception
     */
    public static void defaultMediaPlayer(Context mContext) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        } catch (Exception ex) {
        }
    }

    /**
     * 播放系统默认来电铃声
     *
     * @return MediaPlayer对象
     * @throws Exception
     */
    public static void defaultCallMediaPlayer(Context mContext) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        } catch (Exception ex) {
        }
    }

    /**
     * 播放系统默认闹钟铃声
     *
     * @return MediaPlayer对象
     * @throws Exception
     */
    public static void defaultAlarmMediaPlayer(Context mContext) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        } catch (Exception ex) {
        }
    }
}
