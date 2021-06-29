package com.xiao7.pump;

import android.app.Application;
import android.content.Context;

/**
 * 全局上下文
 */
public class BaseApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    /**
     * 获取全局上下文*/
    public static Context getContext() {
        return context;
    }

}
