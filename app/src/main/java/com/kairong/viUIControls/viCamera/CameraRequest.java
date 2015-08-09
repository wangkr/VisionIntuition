package com.kairong.viUIControls.viCamera;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/8/3.
 * mail:wangkrhust@gmail.com
 */
public class CameraRequest implements Serializable{
    private int cropType;               // 裁剪类型
    private int photoMode;              // 拍照模式
    private boolean backOrigin;      // 是否跳转到源Activity class
    private boolean goDestClass;        // 是否跳转到目的Activity class
    private Class originClass;          // 源Activity class
    private Class destClass;            // 目的Activity class
    private String crop_wh_ratio;       // 裁剪宽高比
    private String[] crop_wh_options;   // 裁剪比例选项（适用于多比例裁剪模式）

    private String TAG = "CameraRequest";

    /**
     * 空构造函数
     */
    public CameraRequest(){
        this.cropType = CropPhotoType.CROP_FREE_RATIO;
        this.photoMode = TakePhotoMode.AUTO_MODE;
        this.originClass = null;
        this.destClass = null;
        this.crop_wh_ratio = null;
        this.crop_wh_options = null;
        this.backOrigin = false;
        this.goDestClass = false;
    }
    /**
     * 相机请求构造函数
     * @param cropType:裁剪类型
     * @param photoMode:拍照模式
     * @param crop_wh_ratio:裁剪宽高比
     * @param crop_wh_options:裁剪比例选项
     * @param originClass:源Activity class
     * @param destClass:目的Activity class
     */
    public CameraRequest(int cropType, int photoMode, String crop_wh_ratio, Class originClass, Class destClass, String[] crop_wh_options){
        this.cropType = cropType;
        this.photoMode = photoMode;
        this.originClass = originClass;
        this.destClass = destClass;
        this.crop_wh_ratio = crop_wh_ratio;
        this.crop_wh_options = crop_wh_options;
        this.backOrigin = originClass!=null;
        this.goDestClass = destClass!=null;
    }
    public boolean ifCrop(){
        return cropType>=0;
    }
    public int getPhotoMode(){
        return photoMode;
    }

    /**
     * 获取默认的裁剪比例值：多比例模式下第1个比例为默认比例
     * @return
     */
    public float getDefaultCropWHratioValue(){
        if(cropType==CropPhotoType.CROP_FIXED_RATIO){
            return getWHratioFromString(crop_wh_ratio);
        }else if(cropType==CropPhotoType.CROP_MULTI_RATIO){
            return getWHratioFromString(crop_wh_options[0]);
        }
        return 1;
    }

    /**
     * 获取默认的裁剪比例字符串：多比例模式下第1个比例为默认比例
     * @return
     */
    public String getDefaultCropWHratioString(){
        if(cropType==CropPhotoType.CROP_FIXED_RATIO){
            return crop_wh_ratio;
        }else if(cropType==CropPhotoType.CROP_MULTI_RATIO){
            return crop_wh_options[0];
        }
        return "";
    }

    /**
     * 获取裁剪宽高比选项字符串
     * @return
     */
    public String[] getCrop_wh_options(){
        if(cropType == CropPhotoType.CROP_MULTI_RATIO)
            return crop_wh_options;
        return null;
    }

    /**
     * 获取裁剪类型
     * @return
     */
    public int getCropType(){
        return cropType;
    }

    /**
     * 是否返回源Activity Class
     * @return
     */
    public boolean backOriginClass(){
        return backOrigin;
    }

    /**
     * 是否转到目标Activity Class
     * @return
     */
    public boolean goDestClass(){
        return goDestClass;
    }
    public Class getOriginClass(){
        return originClass;
    }
    public Class getDestClass(){
        return destClass;
    }
    public static float getWHratioFromString(String crop_wh_ratio){
        float ratio = -1;
        if(crop_wh_ratio!=null&&crop_wh_ratio.contains(":")){
            String[] splitStr = crop_wh_ratio.split(":");
            int w = Integer.parseInt(splitStr[0]),h = Integer.parseInt(splitStr[1]);
            try {
                ratio = w*1f/h;
            }catch (Exception e){
                Log.e("CameraRequest", "the h of crop ratio \"w:h\"cannot be zero");
                e.printStackTrace();
                return -1;
            }

        }
        return ratio;
    }
}
