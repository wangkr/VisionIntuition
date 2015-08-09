package com.kairong.viUIControls.viCamera;

import java.io.Serializable;

/**
 * Created by Kairong on 2015/8/8.
 * mail:wangkrhust@gmail.com
 */
// Activity间传递Bitmap的对象类
public class BitmapIntent implements Serializable {
    private int orientation;
    private String bitmappath;
    private int source;

    // Bitmap源
    public static final int FROM_CAMERA = 0;
    public static final int FROM_GALLERY = 1;

    public BitmapIntent(){
        this.orientation = 0;
        this.bitmappath = null;
        this.source = FROM_CAMERA;
    }
    public BitmapIntent(int orientation,String bitmappath,int source){
        this.orientation = orientation;
        this.bitmappath = bitmappath;
        this.source = source;
    }
    public BitmapIntent(String bitmappath){
        this.bitmappath = bitmappath;
        this.orientation = 0;
        this.source = FROM_CAMERA;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setBitmappath(String bitmappath) {
        this.bitmappath = bitmappath;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getSource() {
        return source;
    }

    public int getOrientation(){
        return orientation;
    }
    public String getBitmappath(){
        return bitmappath;
    }
}
