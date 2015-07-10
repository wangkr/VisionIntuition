package com.kairong.vision_recognition;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.kairong.viCamera.CamTargetList;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.DisplayUtil;
import com.kairong.myPathButton.myAnimations;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 */
public class WelcomeActivity extends Activity {
    private viApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (viApplication)getApplication();
        // 获取设备屏幕信息
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        app.setScreenWidth(dm.widthPixels);   // 屏幕宽
        app.setScreenHeight(dm.heightPixels); // 屏幕高


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
        if(app.getScreenHeight()>app.getScreenWidth()){
            app.setIMAGE_LOAD_IN_MEM_MAX_WIDTH(app.getScreenHeight());
        }else{
            app.setIMAGE_LOAD_IN_MEM_MAX_WIDTH(app.getScreenWidth());
        }
        app.setIMAGE_LOAD_IN_MEM_MAX_SIZE(app.getScreenHeight()*app.getScreenWidth());

        Intent newint = new Intent();
        newint.setClass(this, MainActivity.class);
        startActivity(newint);
        this.finish();
    }
}
