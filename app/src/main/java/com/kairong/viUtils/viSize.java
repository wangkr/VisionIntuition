package com.kairong.viUtils;

/**
 * Created by Kairong on 2015/6/14.
 * mail:wangkrhust@gmail.com
 */
public class viSize {
    private int Width;
    private int Height;
    public viSize(int width,int height){
        this.Width = width;
        this.Height = height;
    }
    public void setWidth(int width){
        this.Width = width;
    }
    public void setHeight(int height){
        this.Height = height;
    }
    public int getWidth(){
        return this.Width;
    }
    public int getHeight(){
        return this.Height;
    }
}
