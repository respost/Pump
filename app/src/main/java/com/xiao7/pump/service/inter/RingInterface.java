package com.xiao7.pump.service.inter;

import android.app.AlarmManager;
import android.content.Context;

import java.util.Calendar;

/**
 * 服务接口类
 */
public interface RingInterface {
    void ringOn(Context context, AlarmManager alarmManager, Calendar calendar);
    void ringOff(AlarmManager alarmManager);
}
