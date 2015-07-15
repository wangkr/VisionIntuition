package com.kairong.vision_recognition;

import android.app.Application;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.kairong.viUtils.DisplayUtil;

/**
 * Created by wangkr on 15-7-10 at USTC
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class viApplication extends Application{
    private static final String APP_NAME="Vision Intuition";
    /*屏幕宽*/
    private int screenWidth;
    /*屏幕高*/
    private int screenHeight;
    /*Activity水平边缘宽度*/
    private int activity_horizontal_margin;
    /*Activity垂直边缘宽度*/
    private int activity_vertical_margin;
    /*Activity二级边缘宽度*/
    private int secondary_margin;
    /*Activity三级边缘宽度*/
    private int tertiary_margin;
    /*main activity 1号按钮尺寸*/
    private int main_2_btn_size;
    /*main activity 2号按钮尺寸*/
    private int main_1_btn_size;
    /*加载到内存图片的最大宽度*/
    private int IMAGE_LOAD_IN_MEM_MAX_WIDTH;
    /*加载到内存图片的最大大小*/
    private int IMAGE_LOAD_IN_MEM_MAX_SIZE;

    private float crop_photo_wh_ratio;
    /*裁剪图片的常见宽高比*/
    private float CROP_PHOTO_16W9H_RATIO = 1.7778f;
    private float CROP_PHOTO_9W16H_RATIO = 0.5625f;
    private float CROP_PHOTO_3W2H_RATIO = 1.5000f;
    private float CROP_PHOTO_2W3H_RATIO = 0.6667f;
    private float CROP_PHOTO_4W3H_RATIO = 1.3333f;
    private float CROP_PHOTO_3W4H_RATIO = 0.7500f;
    @Override
    public void onCreate() {
        super.onCreate();
        Resources r = getResources();
        activity_horizontal_margin = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.activity_horizontal_margin));
        activity_vertical_margin = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.activity_vertical_margin));
        secondary_margin = (int)(r.getDimension(R.dimen.activity_secondary_margin));
        tertiary_margin = (int)(r.getDimension(R.dimen.activity_tertiary_margin));
        main_2_btn_size = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.main_2_btn_size));
        main_1_btn_size = (int)(r.getDimension(com.kairong.vision_recognition.R.dimen.main_1_btn_size));
    }

    public String getAppName(){
        return APP_NAME;
    }
    
    public void setScreenWidth(int screenWidth){
        this.screenWidth = screenWidth;
    }
    public void setScreenHeight(int screenHeight){
        this.screenHeight = screenHeight;
    }
    public void setActivity_horizontal_margin(int activity_horizontal_margin){
        this.activity_horizontal_margin = activity_horizontal_margin;
    }
    public void setActivity_vertical_margin(int activity_vertical_margin){
        this.activity_vertical_margin = activity_vertical_margin;
    }
    public void setSecondary_margin(int secondary_margin){
        this.secondary_margin = secondary_margin;
    }
    public void setTertiary_margin(int tertiary_margin){
        this.tertiary_margin = tertiary_margin;
    }
    public void setMain_1_btn_size(int main_1_btn_size){
        this.main_1_btn_size = main_1_btn_size;
    }
    public void setMain_2_btn_size(int main_2_btn_size){
        this.main_2_btn_size = main_2_btn_size;
    }
    public void setIMAGE_LOAD_IN_MEM_MAX_WIDTH(int image_load_in_mem_max_width){
        this.IMAGE_LOAD_IN_MEM_MAX_WIDTH = image_load_in_mem_max_width;
    }
    public void setIMAGE_LOAD_IN_MEM_MAX_SIZE(int image_load_in_mem_max_size){
        this.IMAGE_LOAD_IN_MEM_MAX_SIZE = image_load_in_mem_max_size;
    }


    public int getScreenWidth(){
        return this.screenWidth;
    }
    public int getScreenHeight(){
        return this.screenHeight;
    }
    public int getActivity_horizontal_margin(){
        return this.activity_horizontal_margin;
    }
    public int getActivity_vertical_margin(){
        return this.activity_vertical_margin;
    }
    public int getSecondary_margin(){
        return this.secondary_margin;
    }
    public int getTertiary_margin(){
        return this.tertiary_margin;
    }
    public int getMain_2_btn_size(){
        return this.main_2_btn_size;
    }
    public int getMain_1_btn_size(){
        return this.main_1_btn_size;
    }
    public int getIMAGE_LOAD_IN_MEM_MAX_WIDTH(){
        return this.IMAGE_LOAD_IN_MEM_MAX_WIDTH;
    }
    public int getIMAGE_LOAD_IN_MEM_MAX_SIZE(){
        return this.IMAGE_LOAD_IN_MEM_MAX_SIZE;
    }
    public float getCROP_PHOTO_WH_RATIO(String tag){
        if(tag.contains("16:9")) {crop_photo_wh_ratio = CROP_PHOTO_16W9H_RATIO;}
        else if(tag.contains("9:16")) {crop_photo_wh_ratio = CROP_PHOTO_9W16H_RATIO;}
        else if(tag.contains("3:2"))  {crop_photo_wh_ratio = CROP_PHOTO_3W2H_RATIO;}
        else if(tag.contains("2:3"))  {crop_photo_wh_ratio = CROP_PHOTO_2W3H_RATIO;}
        else if(tag.contains("4:3"))  {crop_photo_wh_ratio = CROP_PHOTO_4W3H_RATIO;}
        else if(tag.contains("3:4"))  {crop_photo_wh_ratio = CROP_PHOTO_3W4H_RATIO;}
        return this.crop_photo_wh_ratio;
    }

    public static viApplication viApp = null;

}
