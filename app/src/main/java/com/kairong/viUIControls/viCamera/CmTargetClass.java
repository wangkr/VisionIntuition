package com.kairong.viUIControls.viCamera;

/**
 * Created by Kairong on 2015/6/13.
 * mail:wangkrhust@gmail.com
 */

/**
 * 相机返回源Activity class类
 */
public class CmTargetClass {
    /*类引用*/
    private Class aClass;
    /*类标签*/
    private String clsTag;
    /**
     * 构造函数
     */
    public CmTargetClass(){
        aClass = null;
        clsTag = "";
    }
    /**
     * 构造函数
     * @param cls
     * @param tag
     */
    public CmTargetClass(Class cls,String tag){
        this.aClass = cls;
        this.clsTag = tag;
    }

    /**
     * 设置类变量函数
     * @param cls：类引用
     * @param tag：类标签
     */
    public void set(Class cls,String tag){
        this.aClass = cls;
        this.clsTag = tag;
    }

    /**
     * 设置标签
     * @param tag：类标签
     */
    public void setTag(String tag){
        this.clsTag = tag;
    }

    /**
     * 设置类引用
     * @param cls：类引用
     */
    public void setaClass(Class cls){
        this.aClass = cls;
    }

    /**
     * 获取标签
     * @return
     */
    public String getTag(){
        return clsTag;
    }

    /**
     * 返回类引用
     * @return
     */
    public Class getaClass(){
        return aClass;
    }
}
