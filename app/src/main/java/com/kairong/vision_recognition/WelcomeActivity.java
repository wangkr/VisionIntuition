package com.kairong.vision_recognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.kairong.viUIControls.viCamera.CamTargetList;
import com.kairong.viUtils.CameraUtil;
import com.kairong.viUtils.DisplayUtil;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viApplication.initViApp(getApplication());
        // 获取设备屏幕信息
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        viApplication.getViApp().setScreenWidth(dm.widthPixels);   // 屏幕宽
        viApplication.getViApp().setScreenHeight(dm.heightPixels); // 屏幕高

        if(!viApplication.getViApp().initDirPath()){
            Toast.makeText(WelcomeActivity.this,"存储不可用,应用退出!",Toast.LENGTH_LONG).show();
        }

        DisplayUtil.scaledDensity = dm.scaledDensity;
        DisplayUtil.scale = dm.density;

//        FaceMatActivity.hListViewImageHeight = (int)(r.getDimension(R.dimen.thumnail_default_height));
//        FaceMatActivity.hListViewImageWidth = (int)(r.getDimension(R.dimen.thumnail_default_width));

        // 初始化CamTargetList
        CamTargetList.addTargetClass(FaceMatActivity.class,"FaceMatActivity");
        CamTargetList.addTargetClass(AutoRecgActivity.class,"AutoRecgActivity");
        CamTargetList.addTargetClass(AgeTestActivity.class,"AgeTestActivity");
        CamTargetList.addTargetClass(TextCovtActivity.class,"TextCovtActivity");

        // 初试BimapUtil信息
        if(viApplication.getViApp().getScreenHeight()>viApplication.getViApp().getScreenWidth()){
            viApplication.getViApp().setIMAGE_LOAD_IN_MEM_MAX_WIDTH(viApplication.getViApp().getScreenHeight());
        }else{
            viApplication.getViApp().setIMAGE_LOAD_IN_MEM_MAX_WIDTH(viApplication.getViApp().getScreenWidth());
        }
        viApplication.getViApp().setIMAGE_LOAD_IN_MEM_MAX_SIZE(viApplication.getViApp().getScreenHeight() * viApplication.getViApp().getScreenWidth());
        // 设置应用的图片宽高比
        Intent newint = new Intent();
        newint.setClass(this, MainActivity.class);
        startActivity(newint);
        this.finish();
    }
}
