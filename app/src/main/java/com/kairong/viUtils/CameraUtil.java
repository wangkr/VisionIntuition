package com.kairong.viUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kairong on 2015/6/12.
 * mail:wangkrhust@gmail.com
 */
public final class CameraUtil {
    // 相机支持的预览尺寸列表
    private static List<Size> supportedPreviewSizes = null;
    // 相机支持的图片尺寸列表
    private static List<Size> supportedPictureSizes = null;

    public static void init()
    {
        Camera camera = Camera.open();
        Camera.Parameters parameters = null;
        if(camera!=null){
            parameters = camera.getParameters();
            supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            supportedPictureSizes = parameters.getSupportedPictureSizes();
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
            Collections.sort(supportedPictureSizes,comp);
            Collections.sort(supportedPreviewSizes,comp);
        }
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
}
