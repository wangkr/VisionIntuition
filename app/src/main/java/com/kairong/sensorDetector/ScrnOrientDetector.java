package com.kairong.sensorDetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Created by Kairong on 2015/6/12.
 * mail:wangkrhust@gmail.com
 */
/**
 *1. Accelrator的x,y,z轴的正负向变化：
 *手机屏幕向上水平放置时： (x,y,z) = (0, 0, -9.81)
 *当手机顶部抬起时： y减小，且为负值
 *当手机底部抬起时： y增加，且为正值
 *当手机右侧抬起时： x减小，且为负值
 *当手机左侧抬起时： x增加，且为正值
 2. Accelrator的z轴的变化：
 *手机屏幕向上水平放置时，z= -9.81
 *手机屏幕竖直放置时，z=  0
 *手机屏幕向下水平放置时，z=  9.81
 3. 系统默认屏幕横竖切换
 *当y变为-5时， 手机画面切换为竖向
 *当x变为-5时， 手机画面切换为横向
 */
/**
 * 用于检测手机屏幕旋转方向
 */
public class ScrnOrientDetector implements SensorEventListener {

    /**
     * 检测的时间间隔
     */
    private int UPDATE_INTERVAL;
    /**
     * 上一次检测的屏幕角度
     */
    int mLastSrcnOrientation;
    /**
     * 上一次检测检测的时间
     */
    private long mLastUpdateTime;
    /**
     * 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。
     */
    Context mContext;
    SensorManager mSensorManager;
    ArrayList<OnSrcnListener> mListeners;
    public int SrcnOrientation;

    /**
     * 旋转检测阈值，决定了对旋转的敏感程度，越小越敏感。
     */
    private int orientChangedThreshold;
    public ScrnOrientDetector(Context context){
        mContext = context;
        mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mListeners = new ArrayList<OnSrcnListener>();
        SrcnOrientation = 0;
        orientChangedThreshold = 2;
        UPDATE_INTERVAL = 100;
    }

    /**
     * 屏幕旋转监听器设置
     */
    public void settings(int oritChgThre,int upDateTime)
    {
        this.orientChangedThreshold = oritChgThre;
        this.mLastUpdateTime = upDateTime;
    }
    /**
     * 当摇晃事件发生时，接收通知
     */
    public interface OnSrcnListener {
        /**
         * 当手机摇晃时被调用
         */
        void onSrcnRoate(int Orientation);
    }
    /**
     * 注册OnShakeListener，当摇晃时接收通知
     *
     * @param listener
     */
    public void registerOnShakeListener(OnSrcnListener listener) {
        if (mListeners.contains(listener))
            return;
        mListeners.add(listener);
    }
    /**
     * 移除已经注册的OnShakeListener
     *
     * @param listener
     */
    public void unregisterOnShakeListener(OnSrcnListener listener) {
        mListeners.remove(listener);
    }
    /**
     * 启动摇晃检测
     */
    public void start() {
        if (mSensorManager == null) {
            throw new UnsupportedOperationException();
        }
        Sensor sensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null) {
            throw new UnsupportedOperationException();
        }
        boolean success = mSensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_GAME);
        if (!success) {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * 停止屏幕旋转方向检测
     */
    public void stop() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - mLastUpdateTime;
        if (diffTime < UPDATE_INTERVAL)
            return;
        mLastUpdateTime = currentTime;
        mLastSrcnOrientation = SrcnOrientation;
        float x = -event.values[0];
        float y = -event.values[1];
        float z = -event.values[2];

        float magnitude = x * x + y * y;
        // 如果相对于y值的幅度小,不要相信角度
        if (magnitude * 4 >= z * z) {
            float OneEightyOverPi = 57.29577957855f;
            float angle = (float) Math.atan2(-y, x) * OneEightyOverPi;
            SrcnOrientation = 90 - (int) Math.round(angle);
            // 归为0 - 359范围内
            while (SrcnOrientation >= 360) {
                SrcnOrientation -= 360;
            }
            while (SrcnOrientation < 0) {
                SrcnOrientation += 360;
            }
            if (Math.abs(SrcnOrientation - mLastSrcnOrientation) > orientChangedThreshold) { // 当加速度的差值大于指定的阈值，认为这是一个摇晃
                this.notifyListeners();
            }
        }
    }
    /**
     * 当摇晃事件发生时，通知所有的listener
     */
    private void notifyListeners() {
        for (OnSrcnListener listener : mListeners) {
            listener.onSrcnRoate(SrcnOrientation);
        }
    }
}
