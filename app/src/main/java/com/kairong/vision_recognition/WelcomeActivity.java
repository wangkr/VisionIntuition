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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取设备屏幕信息
        DisplayUtil.scale = getResources().getDisplayMetrics().density;                 // 像素密度
        DisplayUtil.scaledDensity = getResources().getDisplayMetrics().scaledDensity;   // 伸缩密度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        DisplayUtil.screenWidth = dm.widthPixels;   // 屏幕宽
        DisplayUtil.screenHeight = dm.heightPixels; // 屏幕高

        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        DisplayUtil.stateBarHeight = rect.top;      // 状态栏高

        Resources r = getResources();
        DisplayUtil.activity_horizontal_margin = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.activity_horizontal_margin));
        DisplayUtil.activity_vertical_margin = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.activity_vertical_margin));
        DisplayUtil.main_2_btn_size = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.main_2_btn_size));
        DisplayUtil.main_1_btn_size = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.main_1_btn_size));

//        FaceMatActivity.hListViewImageHeight = (int)(r.getDimension(R.dimen.thumnail_default_height));
//        FaceMatActivity.hListViewImageWidth = (int)(r.getDimension(R.dimen.thumnail_default_width));

        // 初始化CamTargetList
        CamTargetList.addTargetClass(FaceMatActivity.class,"FaceMatActivity");
        CamTargetList.addTargetClass(AutoRecgActivity.class,"AutoRecgActivity");
        CamTargetList.addTargetClass(AgeTestActivity.class,"AgeTestActivity");
        CamTargetList.addTargetClass(TextCovtActivity.class,"TextCovtActivity");

        // 初试BimapUtil信息
        if(DisplayUtil.screenHeight>DisplayUtil.screenWidth){
            BitmapUtil.IMAGE_LOAD_IN_MEM_MAX_WIDTH = DisplayUtil.screenHeight;
        }else{
            BitmapUtil.IMAGE_LOAD_IN_MEM_MAX_WIDTH = DisplayUtil.screenWidth;
        }
        BitmapUtil.IMAGE_LOAD_IN_MEM_MAX_SIZE = DisplayUtil.screenHeight*DisplayUtil.screenWidth;

        Intent newint = new Intent();
        newint.setClass(this, MainActivity.class);
        startActivity(newint);
        this.finish();
    }
}
