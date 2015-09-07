package com.kairong.vision_recognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.kairong.viUIControls.viCamera.CamTargetList;
import com.kairong.viUtils.CameraUtil;
import com.kairong.viUtils.DisplayUtil;

import java.util.List;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 初始化应用
                viApplication.initViApp(getApplication());
                // 获取设备屏幕信息
                initDeviceInfo();
                // 初始化应用首选项
                initPreference();

                // 初始化CamTargetList
                CamTargetList.addTargetClass(FaceMatActivity.class,"FaceMatActivity");
                CamTargetList.addTargetClass(AutoRecgActivity.class,"AutoRecgActivity");
                CamTargetList.addTargetClass(AgeTestActivity.class,"AgeTestActivity");
                CamTargetList.addTargetClass(TextCovtActivity.class,"TextCovtActivity");

                // 初始BimapUtil信息
                if(viApplication.getViApp().getScreenHeight()>viApplication.getViApp().getScreenWidth()){
                    viApplication.getViApp().setIMAGE_LOAD_IN_MEM_MAX_WIDTH(viApplication.getViApp().getScreenHeight());
                }else{
                    viApplication.getViApp().setIMAGE_LOAD_IN_MEM_MAX_WIDTH(viApplication.getViApp().getScreenWidth());
                }
                viApplication.getViApp().setIMAGE_LOAD_IN_MEM_MAX_SIZE(viApplication.getViApp().getScreenHeight() * viApplication.getViApp().getScreenWidth());
                Intent newint = new Intent();
                newint.setClass(WelcomeActivity.this, MainActivity.class);
                try {
                    Thread.sleep(1200);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    startActivity(newint);
                    WelcomeActivity.this.finish();
                }
            }
        }).start();
    }

    private void initDeviceInfo(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        viApplication.getViApp().setScreenWidth(dm.widthPixels);   // 屏幕宽
        viApplication.getViApp().setScreenHeight(dm.heightPixels); // 屏幕高

        if(!viApplication.getViApp().initDirPath()){
            Toast.makeText(WelcomeActivity.this,"存储不可用,应用退出!",Toast.LENGTH_LONG).show();
        }
        DisplayUtil.scaledDensity = dm.scaledDensity;
        DisplayUtil.scale = dm.density;
    }
    private void initPreference(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("IfFirstTime",true)){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("IfFirstTime",false);
            // 初始化图片分辨率
            int w = viApplication.getViApp().getScreenWidth(),h = viApplication.getViApp().getScreenHeight();
            List<Camera.Size> list = CameraUtil.getCameraUtil().getAllPictureSizeOfRatio(Camera.CameraInfo.CAMERA_FACING_BACK, CameraUtil.getWHratioString(h, w));
            String picQualKey = getResources().getString(R.string.pic_quality_key);
            Camera.Size defSize = list.get(Math.round(list.size() / 2));
            editor.putString(picQualKey,defSize.width+"x"+defSize.height);
            // 初始化默认摄像头
            String defCamKey = getResources().getString(R.string.def_cam_key);
            editor.putString(defCamKey,"0");
            editor.apply();
        }
    }
}
