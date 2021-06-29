package com.xiao7.pump;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.xiao7.pump.Utils.DialogUtils;
import com.xiao7.pump.Utils.MediaPlayerUtils;
import com.xiao7.pump.Utils.NotifyUtils;
import com.xiao7.pump.Utils.StatusBarUtils;
import com.xiao7.pump.Utils.TimeUtils;
import com.xiao7.pump.Utils.ToolUtils;
import com.xiao7.pump.service.RingService;
import com.xiao7.pump.service.inter.RingInterface;

import net.zy13.library.OmgPermission;
import net.zy13.library.Permission;
import net.zy13.library.PermissionFail;
import net.zy13.library.PermissionSuccess;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static AlarmManager alarmManager;
    private PendingIntent pi;
    private static Button buttonRing;
    private RadioGroup radioGroup;
    private static TextView txtCountDown;
    private int time = 20;
    private static int secondTime = 20 * 60;
    //服务相关
    private Intent intent;
    public static RingInterface ringInterface;
    private MyConnection conn;

    //声明一个操作常量字符串
    public static final String ACTION_SERVICE_NEED = "action.ServiceNeed";
    //声明一个内部广播实例
    public ServiceNeedBroadcastReceiver broadcastReceiver;

    //存储请求码
    private final int REQUEST_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这是为了应用程序安装完后直接打开，按home键退出后，再次打开程序出现的BUG
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            //结束你的activity
            return;
        }
        // 隐藏标题栏，在加载布局之前设置(兼容Android2.3.3版本)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        /**
         * 沉浸式(透明)状态栏
         * 说明：需要在setContentView之后才可以调用
         */
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtils.setRootViewFitsSystemWindows(this, false);
        //设置状态栏透明
        StatusBarUtils.setTranslucentStatus(this);
        //设置状态使用深色文字图标风格
        if (!StatusBarUtils.setStatusBarDarkTheme(this, true)) {
            //设置一个半透明（半透明+白=灰）颜色的状态栏
            StatusBarUtils.setStatusBarColor(this, 0x55000000);
        }
        /**
         * 添加标志位，允许锁屏状态下显示消息，四个标志位分别是：
         *  1.锁屏状态下显示
         *  2.解锁
         *  3.保持屏幕长亮（可选）
         *  4.打开屏幕
         * 当Activity启动的时候，它会解锁并亮屏显示
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); //打开屏幕
        //请求权限
        //requestPermission();
        initService();
        initView();
        initEvent();
        //得到闹钟管理器
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    }

    private void requestPermission() {
/**
 * 请求权限
 * request()方法的参数可以有也可以没有，有且不为空，就会回调PermissionCallback的响应的回调方法，没有或为空，则回调响应的注解方法。
 */
        OmgPermission.with(MainActivity.this)
                ////添加请求码
                .addRequestCode(REQUEST_STORAGE)
                //单独申请一个权限
                //.permissions(Manifest.permission.CAMERA)
                //同时申请多个权限
                .permissions(Permission.STORAGE)
                .request();
    }
    /**
     * 回调注解方法
     * 当request()没有参数的时候，就会在当前类里面寻找相应的注解方法
     */
    @PermissionSuccess(requestCode = REQUEST_STORAGE)
    public void permissionSuccess() {
        Toast.makeText(MainActivity.this, "成功授予读写权限" , Toast.LENGTH_SHORT).show();
    }
    @PermissionFail(requestCode = REQUEST_STORAGE)
    public void permissionFail() {
        Toast.makeText(MainActivity.this, "授予读写权限失败" , Toast.LENGTH_SHORT).show();
    }
    /**
     * 申请权限的系统回调方法
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        OmgPermission.onRequestPermissionsResult(MainActivity.this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        String name = buttonRing.getText().toString();
        if (name.equals("停止抽水")) {
            TimeUtils.startCountdown(false, secondTime, txtCountDown);
        }
    }

    @Override
    protected void onDestroy() {
        //关掉铃声
        MediaPlayerUtils.stop();
        // 解绑服务与活动
        unbindService(conn);
        ringInterface = null;
        stopService(intent);
        super.onDestroy();
    }

    private void initView() {
        buttonRing = findViewById(R.id.btnRing);
        radioGroup = findViewById(R.id.radioGroupTime);
        txtCountDown = findViewById(R.id.txtCountDown);
        selectRadioButton(radioGroup);
    }

    private void initService() {
        /**
         * RingService服务
         */
        intent = new Intent(MainActivity.this, RingService.class);
        // 1.启动服务
        startService(intent);
        // 2.创建活动与服务的连接
        conn = new MyConnection();
        // 3.绑定服务与活动，参数：意图-----连接----绑定时自动创建(也可以写0)
        bindService(intent, conn, BIND_AUTO_CREATE);
        /**
         * 注册广播实例（在初始化的时候）
         */
        IntentFilter filter = new IntentFilter();
        //给意图过虑器增加一个Action（用来区分广播来源，相当于是广播的身份证）
        filter.addAction(ACTION_SERVICE_NEED);
        broadcastReceiver = new ServiceNeedBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    private void initEvent() {
        buttonRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = buttonRing.getText().toString();
                if (name.equals("开始抽水")) {
                    buttonRing.setText("停止抽水");
                    //设置按钮背景颜色（使用GradientDrawable防止radius圆角消失)
                    GradientDrawable gradientDrawable = (GradientDrawable)buttonRing.getBackground();
                    gradientDrawable.setColor(Color.parseColor("#FF493C"));
                    secondTime = time * 60;
                    //倒计时
                    TimeUtils.startCountdown(true, secondTime, txtCountDown);
                    //获得当前系统的小时、分钟
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minue = calendar.get(Calendar.MINUTE);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, (minue + time));
                    //使用服务去调用广播通知
                    if (ringInterface != null) {
                        ringInterface.ringOn(MainActivity.this, alarmManager, calendar);
                    }
                } else {
                    stopPump();
                }
            }
        });
         //监听单选按钮点击事件
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectRadioButton(group);
            }
        });
    }

    /**
     * 点击停止抽水
     */
    public static void stopPump() {
        String name = buttonRing.getText().toString();
        if (name.equals("停止抽水")) {
            buttonRing.setText("开始抽水");
            txtCountDown.setText("00:00:00");
            //设置按钮背景颜色（使用GradientDrawable防止radius圆角消失)
            GradientDrawable gradientDrawable = (GradientDrawable)buttonRing.getBackground();
            gradientDrawable.setColor(Color.parseColor("#38AF20"));
            //结束倒计时
            TimeUtils.stopCountdown();
            //关掉铃声和通知
            if (ringInterface != null) {
                ringInterface.ringOff(alarmManager);
            }
            //关闭震动
            ToolUtils.closeVibrate();
        }
    }

    /**
     * 定义广播接收器，用于执行Service服务的需求（内部类）
     */
    private class ServiceNeedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*------------这里是要在Activity活动里执行的代码----------*/
            //手机震动
            ToolUtils.playVibrate(MainActivity.this, true);
            //播放系统默认闹钟铃声
            ToolUtils.defaultAlarmMediaPlayer(MainActivity.this);
            //发送通知
            String contentTitle= getString(R.string.noticeTitle);
            String contentText= getString(R.string.noticeContent);
            NotifyUtils.sendNotice(MainActivity.this,"pumpchat","消息提醒",contentTitle,contentText,true);
            //弹窗提示
            DialogUtils.showAlterDialog(MainActivity.this, MainActivity.this.getString(R.string.ringTips),true, new DialogUtils.ResultCallBack() {
                @Override
                public void callback(boolean flag) {
                    //点击确定
                    if (flag){
                        stopPump();
                    }
                }
            });
            /*------------这里是要在Activity活动里执行的代码----------*/
        }
    }
    private void selectRadioButton(RadioGroup radioGroup) {
        //通过radioGroup.getCheckedRadioButtonId()来得到选中的RadioButton的ID，从而得到RadioButton进而获取选中值
        RadioButton rb = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
        time = Integer.parseInt(rb.getTag().toString());
        //Toast.makeText(MainActivity.this, "时间为："+time, Toast.LENGTH_LONG).show();
    }

    //开启单次闹钟
    public void oneAlarm(View view) {
        //获得当前系统的小时、分钟
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);//得到小时
        final int minue = calendar.get(Calendar.MINUTE);//得到分钟

        //弹出时间对话框
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                //Toast.makeText(MainActivity.this, "minue=" + minue, Toast.LENGTH_LONG).show();
                calendar.set(Calendar.MINUTE, time);
                //使用服务去调用广播通知
                if (ringInterface != null) {
                    ringInterface.ringOn(MainActivity.this, alarmManager, calendar);
                }
            }
        }, hour, minue, true);
        tpd.show();//显示窗口
    }

    //开启周期闹钟
    public void periodAlarm(View view) {
        //获得当前系统的小时跟分
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); //得到小时
        final int minue = calendar.get(Calendar.MINUTE);//得到分钟
        //Toast.makeText(MainActivity.this, hour + "小时，" + minue + "分钟", Toast.LENGTH_LONG).show();
        //弹出时间对话框
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, time);
                //使用服务去调用广播通知
                if (ringInterface != null) {
                    ringInterface.ringOn(MainActivity.this, alarmManager, calendar);
                }
            }
        }, hour, minue, true);
        tpd.show();//显示窗口
    }

    //关闭闹钟
    public void closeAlarm(View view) {
        alarmManager.cancel(pi);
        //关掉铃声
        MediaPlayerUtils.stop();
    }

    /**
     * 自定义活动与服务的连接器
     *
     * @author admin
     */
    class MyConnection implements ServiceConnection {
        // 连接启用时调用
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ringInterface = (RingInterface) iBinder;
        }

        // 连接关闭时调用
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    /**
     * 格式化时间
     * @param second
     * @return
     */
    public String formatLongToTimeStr(int second) {
        int hour = 0;
        int minute = 0;
        if (second > 60) {
            minute = second / 60;   //取整
            second = second % 60;   //取余
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        String strtime = hour+"时"+ minute + "分" + second + "秒";
        return strtime;
    }
}
