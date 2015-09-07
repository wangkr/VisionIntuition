package com.kairong.viUIControls.viCamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.kairong.viUIControls.viPreferences.viSeekBarPreference;
import com.kairong.viUtils.CameraUtil;
import com.kairong.vision_recognition.R;
import com.kairong.vision_recognition.viApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kairong on 2015/8/24.
 * mail:wangkrhust@gmail.com
 */
public class CameraHelper {
    private Context mContext;
    private Camera camera;
    private SurfaceHolder holder;
    private Camera.Parameters photoParameters;
    private Handler handler;
    private CameraUtil cameraUtil;
    private String tmpPhotoPath;
    private int camera_position;
    private int save_photo_state;
    private boolean isPreviewing;
    private boolean focuseState;

    private int orientation;
    private int barrier_height;

    /*照片保存状态*/
    public final static int SAVING_PHOTO = 3323;
    public final static int SAVED_PHOTO = 3324;
    public final static int SAVED_ERROR = 3325;
    /*显示对焦状态*/
    public final static int MSG_FOCUSING = 3234;
    public final static int MSG_FOCUSED = 3235;
    public final static int MSG_FOCUS_FAILED = 3236;

    public CameraHelper(Context mContext,SurfaceHolder holder) {
        this.mContext = mContext;
        this.holder = holder;
        this.isPreviewing = false;
        this.focuseState = false;
        this.camera_position = Camera.CameraInfo.CAMERA_FACING_BACK;
        cameraUtil = CameraUtil.getCameraUtil();
    }
    public void open(){
        // 默认打开后置摄像头
        if(camera!=null){
            camera.release();
            camera = null;
        }

        camera = Camera.open();

        try {
            // 设置摄像头参数
            setParameters(Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
            camera.setDisplayOrientation(90);
            camera.startPreview();// 开始预览
            isPreviewing = true;
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void open(int position){
        if(camera!=null){
            camera.release();
            camera = null;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        if(cameraCount <= 1){
            Toast.makeText(mContext,"系统只检测到一个摄像头",Toast.LENGTH_LONG).show();
            return;
        }
        for(int i = 0; i < cameraCount;i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (position == cameraInfo.facing) {
                camera = Camera.open(i);
                break;
            }
        }

        try {
            // 设置摄像头参数
            setParameters(position);
            camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
            camera.setDisplayOrientation(90);
            camera.startPreview();// 开始预览
            isPreviewing = true;
            camera_position = position;
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 设置摄像头参数
     * @param camera_position 摄像头位置
     */
    private void setParameters(int camera_position){
        photoParameters = camera.getParameters();

        if(camera_position == Camera.CameraInfo.CAMERA_FACING_FRONT){
            photoParameters.set("rotation",90);
        }

        int screenW = viApplication.getViApp().getScreenWidth();
        int screenH = viApplication.getViApp().getScreenHeight();
        // 获取屏幕宽高比
        String srcnRatio = CameraUtil.getWHratioString(screenH, screenW);


        Camera.Size preSize = cameraUtil.getMaxPreviewSizeOfRatio(camera_position, srcnRatio);
        try {
            photoParameters.setPreviewSize(preSize.width, preSize.height);
            if(camera_position == Camera.CameraInfo.CAMERA_FACING_BACK){
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String defPicSize = sharedPreferences.getString(mContext.getResources().getString(R.string.pic_quality_key), screenH + "x" + screenW);
                String[] picSize = defPicSize.split("x");
                int picW,picH;
                picW = Integer.parseInt(picSize[0]);picH = Integer.parseInt(picSize[1]);
                photoParameters.setPictureSize(picW, picH);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            String msg = "camera parameters setting error!";
            Toast.makeText(mContext, msg ,Toast.LENGTH_SHORT).show();
            Log.e("CameraHelper", msg);
            photoParameters.setPreviewSize(screenH,screenW);
            photoParameters.setPictureSize(screenH, screenW);
        }

        camera.setParameters(photoParameters);
    }
    
    public void stop(){
        // 置预览回调为空，再关闭预览
        if(camera!=null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            isPreviewing = false;
        }
    }

    /**
     *切换前置或者后置摄像头
     */
    public void switchCamera(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        if(cameraCount <= 1){
            Toast.makeText(mContext,"系统只检测到一个摄像头",Toast.LENGTH_LONG).show();
            return;
        }
        for(int i = 0; i < cameraCount;i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(camera_position == Camera.CameraInfo.CAMERA_FACING_BACK) {
                //现在是后置，变更为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置  CAMERA_FACING_BACK后置
                    camera_position = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    camera.stopPreview();   //停掉原来摄像头的预览
                    camera.release();       //释放资源
                    camera = null;          //取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头

                    // 设置摄像头参数
                    setParameters(Camera.CameraInfo.CAMERA_FACING_FRONT);

                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.setDisplayOrientation(90);
                    camera.startPreview();//开始预览
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头

                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // 设置摄像头参数
                    setParameters(Camera.CameraInfo.CAMERA_FACING_BACK);

                    camera.setDisplayOrientation(90);
                    camera.startPreview();//开始预览
                    camera_position = Camera.CameraInfo.CAMERA_FACING_BACK;
                    break;
                }
            }

        }
    }
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if(success){
                handler.sendEmptyMessage(MSG_FOCUSED);
            } else {
                handler.sendEmptyMessage(MSG_FOCUS_FAILED);
            }
            focuseState = success;
        }
    };

    public synchronized void autoFocus(Handler handler){
        this.handler = handler;
        focuseState = false;
        handler.sendEmptyMessage(MSG_FOCUSING);
        camera.autoFocus(autoFocusCallback);
    }

    public void restartPreview(){
        camera.setDisplayOrientation(90);
        camera.startPreview();
        isPreviewing = true;
        camera.autoFocus(autoFocusCallback);
    }

    // 创建jpeg图片回调数据对象,对图片进行旋转和初步裁剪
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            isPreviewing = false;
            Bitmap saved_photo = BitmapFactory.decodeByteArray(data, 0, data.length);
            save_photo_state = SAVING_PHOTO;
            savePicture(saved_photo,orientation,barrier_height);
        }
    };

    public void takePhoto(final int orientation,final int barrier_height){
        this.orientation = orientation;
        this.barrier_height = barrier_height;
        camera.takePicture(null,null,jpeg);
    }
    private void savePicture(Bitmap saved_photo, int orientation,int barrier_height) {
        /*存储的图片宽高*/
        int storeImageWidth = 0, storeImageHeight = 0;
        /*保存的图片和预览图片的比例*/
        float ratio = (float)photoParameters.getPictureSize().width/photoParameters.getPreviewSize().width;
        // 确定最终图片的宽高
        if (orientation == viCameraActivity.ORIENTATION_LAND || orientation == viCameraActivity.ORIENTATION_REV_LAND) {
            storeImageWidth = saved_photo.getWidth() - 2 * Math.round(mContext.getResources().getDimensionPixelSize(R.dimen.layout_bar_height)*ratio);
            storeImageHeight = saved_photo.getHeight();
        } else {
            storeImageWidth = saved_photo.getWidth() - Math.round(mContext.getResources().getDimensionPixelSize(R.dimen.layout_bar_height)*ratio + barrier_height*ratio);
            storeImageHeight = saved_photo.getHeight();
        }
        // 对图片进行旋转和裁剪处理
        {
            // 后置摄像头对照片进行顺时针旋转90度，前置摄像头则逆时针转90度
            Matrix matRotate = new Matrix();
            if (camera_position == 0) {
                matRotate.setRotate(-orientation);
            } else {
                matRotate.setRotate(orientation);
            }
            int h = 0, w = Math.round(mContext.getResources().getDimensionPixelSize(R.dimen.layout_bar_height) * ratio);

            try {
                // 先图片进行初步
//                saved_photo = Bitmap.createBitmap(saved_photo, w, h, storeImageWidth, storeImageHeight, null, true);
                // 进行剪切旋转
                saved_photo = Bitmap.createBitmap(saved_photo, w, h, storeImageWidth, storeImageHeight, matRotate, true);
            } catch (OutOfMemoryError e){
                e.printStackTrace();
                if(saved_photo!=null&&!saved_photo.isRecycled()){
                    saved_photo.recycle();
                    System.gc();
                }
                save_photo_state = SAVED_ERROR;
                return;
            }
        }

        File file = new File(viApplication.getViApp().getTempFileDir() + "//" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                    ".tmp");
        tmpPhotoPath = file.getPath();

        /*保存成临时文件*/
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            saved_photo.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();    // 刷新此缓冲区的输出流
            bos.close();    // 关闭此输出流并释放与此流有关的所有系统资源
            save_photo_state = SAVED_PHOTO;
        } catch (IOException | NullPointerException e){
            e.printStackTrace();
            save_photo_state = SAVED_ERROR;
        } finally {
            // 释放内存
            if(saved_photo!=null&&!saved_photo.isRecycled()){
                saved_photo.recycle();
                System.gc();
            }
        }
    }
    public String getTmpPhotoPath(){
        return tmpPhotoPath;
    }
    public boolean isPreviewing(){
        return isPreviewing;
    }
    public int getCamera_position(){
        return camera_position;
    }
    public boolean isFocused(){
        return focuseState;
    }
    public int getSave_photo_state(){
        return save_photo_state;
    }
}
