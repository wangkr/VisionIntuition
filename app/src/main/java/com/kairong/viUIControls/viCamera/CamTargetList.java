package com.kairong.viUIControls.viCamera;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2015/6/13.
 * mail:wangkrhust@gmail.com
 */
public class CamTargetList {
    private static List<CmTargetClass> cmTargetClassList = null;

    /**
     * 添加目标class
     * @param cls
     * @param tag
     */
    public static void addTargetClass(Class cls,String tag)
    {
        if(cmTargetClassList == null){
            cmTargetClassList = new ArrayList<>();
        }
        CmTargetClass cmTargetClass = new CmTargetClass(cls,tag);
        cmTargetClassList.add(cmTargetClass);
    }

    /**
     * 获取class标签
     * @param cls：类引用
     * @return
     */
    public static String getTargetTag(Class cls){
        String restr = null;
        for(int i = 0;i < cmTargetClassList.size();i++){
            CmTargetClass cmTargetClass = cmTargetClassList.get(i);
            if(cmTargetClass.getaClass() == cls){
                restr = cmTargetClass.getTag();
                break;
            }
        }
        return restr;
    }

    /**
     * 返回目标class引用
     * @param tag：类标签
     * @return
     */
    public static Class getTargetClass(String tag)
    {
        Class recls = null;
        for(int i = 0;i < cmTargetClassList.size();i++){
            CmTargetClass cmTargetClass = cmTargetClassList.get(i);
            if(cmTargetClass.getTag().equals(tag)){
                recls = cmTargetClass.getaClass();
                break;
            }
        }
        return recls;
    }
}
