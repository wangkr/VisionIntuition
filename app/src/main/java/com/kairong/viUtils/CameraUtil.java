package com.kairong.viUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Kairong on 2015/6/12.
 * mail:wangkrhust@gmail.com
 */
public final class CameraUtil {
    // 摄像头个数
    private int cameraCount = 0;
    // 前置摄像头相机支持的预览尺寸列表
    private static List<Map<String,Object>> supportedPreviewSizesFront = null;
    // 前置相机支持的图片尺寸列表
    private static List<Map<String,Object>> supportedPictureSizesFront = null;
    // 后置摄像头相机支持的预览尺寸列表
    private static List<Map<String,Object>> supportedPreviewSizesBack = null;
    // 后置相机支持的图片尺寸列表
    private static List<Map<String,Object>> supportedPictureSizesBack = null;

    // 摄像头位置
    // 前置摄像头
    public static final int CAMERA_FRONT = 0;
    // 后置摄像头
    public static final int CAMERA_BACK = 1;

    private static final String KEY_VALUE = "value";
    private static final String KEY_RATIO = "ratio";

    private static CameraUtil cameraUtil = null;

    private CameraUtil(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        // 对分辨率进行排序
        Comparator comp = new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                Size sl = (Size)lhs;
                Size sr = (Size)rhs;
                long resoll = ((Size) lhs).height*((Size) lhs).width;
                long resolr = ((Size) rhs).height*((Size) rhs).width;
                if(resoll>resolr)
                    return 1;
                else if(resoll==resolr)
                    return 0;
                else if(resoll<resolr)
                    return -1;
                return 0;
            }
        };
        for(int i = 0;i < cameraCount;i++){
            Camera camera = Camera.open(i);
            Camera.getCameraInfo(i,cameraInfo);
            List<Size> prewSizes = null,picSizes = null;
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                supportedPreviewSizesFront = new ArrayList<Map<String,Object>>();
                supportedPictureSizesFront = new ArrayList<Map<String,Object>>();
                Camera.Parameters parameters = camera.getParameters();
                prewSizes = parameters.getSupportedPreviewSizes();
                picSizes = parameters.getSupportedPictureSizes();
                Collections.sort(picSizes,comp);
                Collections.sort(prewSizes,comp);
                Map<String,Object> map = null;
                for(int j = 0;j < picSizes.size();j++){
                    map = new HashMap<String, Object>();
                    map.put(KEY_VALUE,picSizes.get(j));
                    map.put(KEY_RATIO,getWHratioString(picSizes.get(j).width,picSizes.get(j).height));
                    supportedPictureSizesFront.add(map);
                }
                for(int j1 = 0;j1 < prewSizes.size();j1++){
                    map = new HashMap<String, Object>();
                    map.put(KEY_VALUE,prewSizes.get(j1));
                    map.put(KEY_RATIO,getWHratioString(prewSizes.get(j1).width,prewSizes.get(j1).height));
                    supportedPreviewSizesFront.add(map);
                }
            }
            else if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                supportedPreviewSizesBack = new ArrayList<Map<String,Object>>();
                supportedPictureSizesBack = new ArrayList<Map<String,Object>>();
                Camera.Parameters parameters = camera.getParameters();
                prewSizes = parameters.getSupportedPreviewSizes();
                picSizes = parameters.getSupportedPictureSizes();
                Collections.sort(picSizes,comp);
                Collections.sort(prewSizes,comp);
                Map<String,Object> map = null;
                for(int j = 0;j < picSizes.size();j++){
                    map = new HashMap<String, Object>();
                    map.put(KEY_VALUE,picSizes.get(j));
                    map.put(KEY_RATIO,getWHratioString(picSizes.get(j).width,picSizes.get(j).height));
                    supportedPictureSizesBack.add(map);
                }
                for(int j1 = 0;j1 < prewSizes.size();j1++){
                    map = new HashMap<String, Object>();
                    map.put(KEY_VALUE,prewSizes.get(j1));
                    map.put(KEY_RATIO,getWHratioString(prewSizes.get(j1).width,prewSizes.get(j1).height));
                    supportedPreviewSizesBack.add(map);
                }
            }
            camera.release();
        }
    }
    public static CameraUtil getCameraUtil(){
        if(cameraUtil == null){
            cameraUtil = new CameraUtil();
        }
        return cameraUtil;
    }

    /**
     * 获取对应摄像头的最大预览图片分辨率
     * @param cameraPosition:摄像头位置
     * @return
     */
    public Size getMaxPreviewSize(int cameraPosition){
        if(CAMERA_FRONT == cameraPosition){ return (Size)supportedPreviewSizesFront.get(0).get(KEY_VALUE);}
        if(CAMERA_BACK == cameraPosition){ return (Size)supportedPreviewSizesBack.get(0).get(KEY_VALUE);}
        return null;
    }

    /**
     * 获取对应摄像头的最小预览图片分辨率
     * @param cameraPosition:摄像头位置
     * @return
     */
    public Size getMinPreviewSize(int cameraPosition){
        if(CAMERA_FRONT == cameraPosition){ return (Size)supportedPreviewSizesFront.get(supportedPreviewSizesFront.size()-1).get(KEY_VALUE);}
        if(CAMERA_BACK == cameraPosition){ return (Size)supportedPreviewSizesBack.get(supportedPreviewSizesBack.size()-1).get(KEY_VALUE);}
        return null;
    }

    /**
     * 获取对应摄像头的最大图片分辨率
     * @param cameraPosition:摄像头位置
     * @return
     */
    public Size getMaxPictureSize(int cameraPosition){
        if(CAMERA_FRONT == cameraPosition){ return (Size)supportedPictureSizesFront.get(0).get(KEY_VALUE);}
        if(CAMERA_BACK == cameraPosition){ return (Size)supportedPictureSizesBack.get(0).get(KEY_VALUE);}
        return null;
    }

    /**
     * 获取对应摄像头的最小图片分辨率
     * @param cameraPosition:摄像头位置
     * @return
     */
    public Size getMinPictureSize(int cameraPosition){
        if(CAMERA_FRONT == cameraPosition){ return (Size)supportedPictureSizesFront.get(supportedPictureSizesFront.size()-1).get(KEY_VALUE);}
        if(CAMERA_BACK == cameraPosition){ return (Size)supportedPictureSizesBack.get(supportedPictureSizesBack.size()-1).get(KEY_VALUE);}
        return null;
    }

    /**
     * 根据图片宽高比例来查找对应摄像头的预览图片分辨率
     * @param cameraPosition:相机位置
     * @param ratio:图片宽高比例{@code null}
     * @return
     */
    public Size getPreviewSizeByRatio(int cameraPosition,String ratio){
        int idx = 0;
        Map<String,Object> map = null;
        if(CAMERA_FRONT == cameraPosition){
            int size = supportedPreviewSizesFront.size();
            for(int i = size - 1;i >= 0;i--){
                map = supportedPreviewSizesFront.get(i);
                if(((String)map.get(KEY_RATIO)).contains(ratio))return (Size)map.get(KEY_VALUE);
            }
        }
        if(CAMERA_BACK == cameraPosition){
            int size = supportedPreviewSizesBack.size();
            for(int i = size - 1;i >= 0;i--){
                map = supportedPreviewSizesBack.get(i);
                if(((String)map.get(KEY_RATIO)).contains(ratio))return (Size)map.get(KEY_VALUE);
            }
        }
        return null;
    }

    /**
     * 根据图片的宽高比来查找对应摄像头的存储图片分辨率:默认从高到低查询
     * @param cameraPosition:相机位置
     * @param ratio:图片宽高比例{@code null}
     * @return
     */
    public Size getPictureSizeByRatio(int cameraPosition,String ratio){
        int idx = 0;
        Map<String,Object> map = null;
        if(CAMERA_FRONT == cameraPosition){
            int size = supportedPictureSizesFront.size();
            for(int i = size - 1;i >=0 ;i--){
                map = supportedPictureSizesFront.get(i);
                if(((String)map.get(KEY_RATIO)).contains(ratio))return (Size)map.get(KEY_VALUE);
            }
        }
        if(CAMERA_BACK == cameraPosition){
            int size = supportedPictureSizesBack.size();
            for(int i = size - 1;i >=0 ;i--){
                map = supportedPictureSizesBack.get(i);
                if(((String)map.get(KEY_RATIO)).contains(ratio))return (Size)map.get(KEY_VALUE);
            }
        }
        return null;
    }

    /**
     * 从系统相册获取图片，并返回图片路径
     * @param activity：目标Activity
     * @param data：相册数据
     * @return
     */
    public static String getImageFromSysGallery(Activity activity,Intent data){
        Uri uri = data.getData();
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
        // 获得图片索引值
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        // 将光标移至开头
        cursor.moveToFirst();
        // 最后根据索引值获取图片路径
        String filepath = cursor.getString(index);
        return filepath;
    }
    /**
     * 返回图片分辨率的比例
     */
    public static String getWHratioString(int width,int height){
        int gcd = gcd(width,height);
        int w = width/gcd;int h = height/gcd;
        return w+":"+h;
    }
    public static int gcd(int a,int b){
        int min = a;
        int max = b;
        if (a > b) {
            min = b;
            max = a;
        }
        if (min == 0)
            return max;
        else
            return gcd(min, max - min);
    }
}
