package com.xiao7.pump.Utils;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * 多媒体工具类
 */
public class MediaPlayerUtils {
    //多媒体播放器
    private static MediaPlayer mediaPlayer;

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    public static void setMediaPlayer(MediaPlayer mediaPlayer) {
        MediaPlayerUtils.mediaPlayer = mediaPlayer;
    }
    /**
     * 播放
     */
    public static void play(Context context, int resid){
        if(mediaPlayer==null){
            //初始化
            mediaPlayer = MediaPlayer.create(context,resid);
        }
        mediaPlayer.start();
        //循环播放
        mediaPlayer.setLooping(true);
    }
    /**
     * 重新播放（一般用于暂停后重新播放）
     */
    public static void reStart(){
        if(mediaPlayer!=null){
            mediaPlayer.start();
        }
    }
    /**
     * 暂停
     */
    public static void pause(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }
    /**
     * 停止
     */
    public static void stop(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
    /**
     * 重置
     */
    public static void reset(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }
    }
}
