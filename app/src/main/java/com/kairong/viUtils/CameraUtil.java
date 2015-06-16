package com.kairong.viUtils;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kairong on 2015/6/12.
 * mail:wangkrhust@gmail.com
 */
public final class CameraUtil {
    private static List<Size> supportedPreviewSizes = null;
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
}
