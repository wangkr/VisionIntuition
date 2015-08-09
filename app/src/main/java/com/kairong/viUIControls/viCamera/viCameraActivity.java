package com.kairong.viUIControls.viCamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.sensorDetector.ScrnOrientDetector;
import com.kairong.viUIControls.circleProgress.CircleProgress;
import com.kairong.viUIControls.viCropImage.CropImageActivity;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.CameraUtil;
import com.kairong.viUtils.viSize;
import com.kairong.vision_recognition.R;
import com.kairong.sensorDetector.ShakeDetector;
import com.kairong.vision_recognition.viApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/5/27.
 * mail:wangkrhust@gmail.com
 */
public class viCameraActivity extends Activity implements SurfaceHolder.Callback {
    private SurfaceView surface = null;
    private SurfaceHolder holder = null;
    private Camera camera = null;                       // 声明相机
    private int cameraPosition = 1;                     // 摄像头位置：0代表前置摄像头，1代表后置摄像头
    private int scrnOrient = 90;                        // 当前屏幕朝向
    private int picTakenScrnOrient = 0;                 // 拍照时的手机屏幕朝向
    private int current_mode = -1;                      // 当前拍照模式
    private volatile int save_photo_state = -1;         // 照片保存状态

    private ImageView focus_view = null;                // 显示对焦光标
    private ImageView preview_photo_view = null;        // 显示拍照后的预览图片
    private ImageView btn_shutter = null;               // 快门按钮
    private ImageView btn_gallery = null;               // 相册方式获取
    private ImageView btn_camera_change = null;         // 切换相机
    private ImageView previewing_barrier = null;        // 预览照片遮幅
    private ImageView switch_mode_hint = null;          // 切换拍照模式提示
    private ProgressDialog progressDialog = null;
    // 各个布局的引用
    private RelativeLayout take_photo_rl = null;
    private RelativeLayout preview_photo_rl  = null;

    private ListView take_photo_mode_listView = null;
    private volatile String tmpPhotoPath = null;
    private Parameters photoParameters = null;
    private CameraUtil cameraUtil = null;
    private Size preSize = null;
    private Size picSize = null;
    // 自动对焦监测线程
    // 晃动检测器—用于检测摄像头剧烈晃动，从而启动自动聚焦
    private ShakeDetector shakeDetector = null;
    // 屏幕方向检测器，用于监测屏幕的旋转
    private ScrnOrientDetector scrnOrientDetector = null;
    // 相机请求
    private CameraRequest cameraRequest = null;
    // 头像模式拍照比例
    private static final float HUMAN_MODE_WH_RATIO = 1.0f;

    private boolean isPreviewing = false;
    private boolean isFocused = false;
    private boolean ifStopCPThread = false;
    private boolean modevlist_visible = false;
    private boolean isBarrierShown = false;

    public static int Focus_State_Read = 111;
    public static int Focus_State_Write = 222;
    /*拍照遮幅高度*/
    private int barrier_height = 0;
    // 控件信号量
    /*显示对焦状态*/
    private final static int MSG_FOCUSING = 3234;
    private final static int MSG_FOCUSED = 3235;
    private final static int MSG_FOCUS_FAILED = 3236;
    /*显示进程状态*/
    private final static int STOP_PROGRESS = 3238;
    private static final int REQUEST_CODE_PICK_IMAGE = 3037;
    /*照片保存状态*/
    private final static int SAVING_PHOTO = 3323;
    private final static int SAVED_PHOTO = 3324;
    private final static int SAVED_ERROR = 3325;
    /*手机屏幕的旋转方向*/
    /*水平方向*/
    public static final int ORIENTATION_LAND = 0;
    /*竖直方向*/
    public static final int ORIENTATION_PORTAIT = 90;
    /*反方向水平方向*/
    public static final int ORIENTATION_REV_LAND = 180;
    /*反方向竖直方向*/
    public static final int ORIENTATION_REV_PORTRAIT = 270;
    /*屏幕发生旋转信号*/
    public static final int ORIENTATION_ROTATED = 1234;

    private final String TAG = "viCameraActivity";
    private final static String[] vlist_text_mode = new String[]{"自动模式","风景模式","头像模式"};
    private final static int[] vlist_image_mode = new int[]{
            R.drawable.auto_mode_icn,
            R.drawable.landscape_mode_icn,
            R.drawable.human_mode_icn
    };

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.take_photo_activity);

        // 获取源Activity的相机请求
        cameraRequest = (CameraRequest)getIntent().getSerializableExtra("cameraRequest");
        cameraUtil = CameraUtil.getCameraUtil();
        setScreen();//拍照过程屏幕一直处于高亮

        // 获取相机模式
        current_mode = cameraRequest.getPhotoMode();
        // 初始化视图
        initView();
        setTakePhotoLayout();
    }
    // 初始化视图资源
    private void initView(){
        // 设置控件资源ID
        Button btn_preview_photo_ok = (Button)findViewById(R.id.btn_preview_photo_ok);
        Button btn_preview_photo_cancell = (Button)findViewById(R.id.btn_preview_photo_cancell);
        TextView mode_text = (TextView)findViewById(R.id.take_photo_mode_text);
        LinearLayout take_photo_mode_ll = (LinearLayout)findViewById(R.id.take_photo_mode_btn_layout);
        // 全局控件初始化
        btn_gallery = (ImageView)findViewById(R.id.btn_take_photo_gallery);
        btn_camera_change = (ImageView)findViewById(R.id.btn_camera_change);
        btn_shutter = (ImageView)findViewById(R.id.btn_take_photo);
        focus_view = (ImageView)findViewById(R.id.focus);
        preview_photo_view = (ImageView)findViewById(R.id.take_photo_preview);
        previewing_barrier = (ImageView)findViewById(R.id.take_photo_barrier);
        switch_mode_hint = (ImageView)findViewById(R.id.switch_mode_hint_view);
        surface = (SurfaceView)findViewById(R.id.surfaceview);
        holder = surface.getHolder();   //获得句柄
        holder.addCallback(this);       //添加回调
        // surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        take_photo_rl = (RelativeLayout)findViewById(R.id.take_photo_layout);
        preview_photo_rl = (RelativeLayout)findViewById(R.id.preview_photo_layout);
        take_photo_mode_listView = (ListView)findViewById(R.id.take_photo_mode_list);

        // 设置控件监听
        btn_shutter.setOnClickListener(gl_listener);
        btn_camera_change.setOnClickListener(gl_listener);
        btn_gallery.setOnClickListener(gl_listener);
        btn_preview_photo_cancell.setOnClickListener(gl_listener);
        btn_preview_photo_ok.setOnClickListener(gl_listener);
        take_photo_mode_ll.setOnClickListener(gl_listener);

        // 只有自动模式下才有显示让用户选择的菜单的必要
        if(cameraRequest.getPhotoMode()==TakePhotoMode.AUTO_MODE) {
            mode_text.setText(vlist_text_mode[0]);
            mode_text.invalidate();
            SimpleAdapter mode_adapter = new SimpleAdapter(this, getData(vlist_image_mode, vlist_text_mode), R.layout.crop_wh_ratio_vlist,
                    new String[]{"vlist_image", "vlist_text"}, new int[]{R.id.vlist_image, R.id.vlist_text});
            take_photo_mode_listView.setAdapter(mode_adapter);
            take_photo_mode_listView.setOnItemClickListener(onItemClickListener1);
        } else {
            take_photo_mode_ll.setFocusable(false);
            // 设置文字内容和颜色
            mode_text.setText(vlist_text_mode[cameraRequest.getPhotoMode()-TakePhotoMode.AUTO_MODE]);
            mode_text.setTextColor(Color.rgb(128,128,128));
            mode_text.invalidate();
        }
        // 初始化遮幅高度
        barrier_height = viApplication.getViApp().getScreenHeight() - getResources().getDimensionPixelSize(R.dimen.layout_bar_height)-
                Math.round(viApplication.getViApp().getScreenWidth()*1.0f/HUMAN_MODE_WH_RATIO);
        previewing_barrier.setScaleY(barrier_height * 2);
        previewing_barrier.setVisibility(View.INVISIBLE);

        // 设置“点击聚焦”和拍照模式菜单弹回
        surface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Rect surfaceRect = new Rect();
                view.getDrawingRect(surfaceRect);
                int photo_bar_height = (int) (getResources().getDimension(R.dimen.layout_bar_height));
                surfaceRect.top += photo_bar_height;
                surfaceRect.bottom -= photo_bar_height;
                float m_X = motionEvent.getX(0);
                float m_Y = motionEvent.getY(0);
                if (isPreviewing && cameraPosition == 1 && surfaceRect.contains((int) m_X, (int) m_Y)) {
                    handler.sendEmptyMessage(MSG_FOCUSING);
                    camera.autoFocus(autoFocusCallback);
                }
                // 拍照模式菜单弹回
                if (modevlist_visible) {
                    mode_list_show();
                }
                return false;
            }
        });

    }
    /**
     * 为了防止系统gc时发生内存泄露
     * 自定义的一个Handler静态类
     */
    static class viCmHandler extends Handler{
        WeakReference<viCameraActivity> mActivity;
        viCmHandler(viCameraActivity activity){
            mActivity = new WeakReference<viCameraActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            viCameraActivity theActivity = mActivity.get();
            switch (msg.what) {
                case MSG_FOCUSING:
                    if (theActivity.cameraPosition == 1&&theActivity.isPreviewing)
                        theActivity.focus_view.setBackgroundResource(R.drawable.focus_focusing);
                    break;
                case MSG_FOCUSED:
                    if (theActivity.cameraPosition == 1&&theActivity.isPreviewing)
                        theActivity.focus_view.setBackgroundResource(R.drawable.focus_focused);
                    break;
                case MSG_FOCUS_FAILED:
                    if (theActivity.cameraPosition == 1&&theActivity.isPreviewing)
                        theActivity.focus_view.setBackgroundResource(R.drawable.focus_focus_failed);
                    break;
                case ORIENTATION_ROTATED:
                    theActivity.Btns_Rotate();
                    break;
                case STOP_PROGRESS:
                    if(theActivity.progressDialog!=null) {
                        theActivity.progressDialog.dismiss();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
    private viCmHandler handler = new viCmHandler(this);
    // 设置一个晃动监听器--发生剧烈晃动就重新聚焦
    ShakeDetector.OnShakeListener onShakeListener = new ShakeDetector.OnShakeListener() {
        @Override
        public void onShake() {
            focuseState(Focus_State_Write, false);
            if(cameraPosition == 1&&isPreviewing){
                if(camera!=null){
                    handler.sendEmptyMessage(MSG_FOCUSING);
                    camera.autoFocus(autoFocusCallback);
                }
            }
        }
    };
    // 设置一个屏幕旋转监听器--发生屏幕旋转就重新检测
    ScrnOrientDetector.OnSrcnListener onSrcnListener = new ScrnOrientDetector.OnSrcnListener() {
        @Override
        public void onSrcnRoate(int Orientation) {
            int last_scrnOrient = scrnOrient;
            if (Orientation>45&&Orientation<135) {
                scrnOrient = ORIENTATION_REV_LAND;
            }else if (Orientation>135&&Orientation<225){
                scrnOrient = ORIENTATION_REV_PORTRAIT;
            }else if (Orientation>225&&Orientation<315){
                scrnOrient = ORIENTATION_LAND;
            }else if ((Orientation>315&&Orientation<360)||(Orientation>=0&&Orientation<45)){
                scrnOrient = ORIENTATION_PORTAIT;
            }
            // 屏幕发生旋转的同时，按钮控件也发生对应的旋转
            if(last_scrnOrient!=scrnOrient) {
                handler.sendEmptyMessage(ORIENTATION_ROTATED);
                Btns_Rotate();
            }
            // 显示拍照布局和切换横竖屏提示
            setTakePhotoLayout();
        }
    };

    // 拍照模式监听器
    AdapterView.OnItemClickListener onItemClickListener1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mode_list_show();
            ((TextView)findViewById(R.id.take_photo_mode_text)).setText(vlist_text_mode[position]);
            current_mode = TakePhotoMode.AUTO_MODE+position;

        }
    };
    // 全局控件点击事件监听
    OnClickListener gl_listener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.btn_take_photo_gallery:
                    // 打开相册
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");//相片类型
                    startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                    break;
                case R.id.btn_camera_change:
                    // 切换前后摄像头
                   switchCamera();
                    break;
                case R.id.btn_take_photo:
                    // 拍照
                    takePhoto();
                    break;
                case R.id.btn_preview_photo_ok:
                    // 存储照片
                    storePhoto();
                    break;
                case R.id.btn_preview_photo_cancell:
                    // 丢弃照片
                    discardPhoto();
                    break;
                case R.id.take_photo_mode_btn_layout:
                    mode_list_show();
                    break;
            }
        }
    };

    /**
     * 得到裁剪宽高比选项菜单内容
     * @param vlist_image:图片资源id
     * @param vlist_text:描述字符串
     * @return
     */
    private List<Map<String,Object>> getData(int[] vlist_image,String[] vlist_text){
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

        Map<String,Object> map = null;
        for(int i = 0;i < vlist_image.length;i++){
            map = new HashMap<String,Object>();
            map.put("vlist_image",vlist_image[i]);
            map.put("vlist_text",vlist_text[i]);
            list.add(map);
        }

        return list;

    }
    AutoFocusCallback autoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            Message msg = Message.obtain();
            if (success) {
                msg.what = MSG_FOCUSED;
            } else {
                msg.what = MSG_FOCUS_FAILED;
            }
            handler.sendMessage(msg);
            focuseState(Focus_State_Write, success);
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(camera == null){
            // 默认打开后置摄像头
            camera = Camera.open();
            try {
                // 设置摄像头和照片参数
                String srcnRatio = CameraUtil.getWHratioString(viApplication.getViApp().getScreenWidth(),
                        viApplication.getViApp().getScreenHeight());// 获取屏幕宽高比
                // 设置摄像头参数
                setCameraParameter(CameraUtil.CAMERA_BACK);

                camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
                camera.setDisplayOrientation(90);
                camera.startPreview();// 开始预览

                isPreviewing = true;

                shakeDetector = new ShakeDetector(this);
                scrnOrientDetector = new ScrnOrientDetector(this);

                shakeDetector.registerOnShakeListener(onShakeListener);
                scrnOrientDetector.registerOnShakeListener(onSrcnListener);
                // 启动监听
                shakeDetector.start();
                scrnOrientDetector.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 置预览回调为空，再关闭预览
        if(camera!=null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            isPreviewing = false;
        }
        // 最后关掉震动检测器和自动对焦
        shakeDetector.stop();
        scrnOrientDetector.stop();
        surface = null;
    }

    /**
     * 根据屏幕方向对按钮进行旋转
     */
    private void Btns_Rotate(){
        // 布局默认为竖屏布局方式
        btn_gallery.setRotation(90 - scrnOrient);
        btn_camera_change.setRotation(90 - scrnOrient);
    }
    /**
     *切换前置或者后置摄像头
     */
    private void switchCamera(){
        int cameraCount = 0;
        CameraInfo cameraInfo = new CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        if(cameraCount <= 1){
            Toast.makeText(viCameraActivity.this,"系统只检测到一个摄像头",Toast.LENGTH_LONG).show();
            return;
        }
        for(int i = 0; i < cameraCount;i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(cameraPosition == 1) {
                //现在是后置，变更为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    cameraPosition = 0;
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();   //释放资源
                    camera = null;      //取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头

                    // 设置摄像头参数
                    setCameraParameter(CameraUtil.CAMERA_FRONT);

                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.setDisplayOrientation(90);
                    camera.startPreview();//开始预览
                    focuseState(Focus_State_Write,true);
                    focus_view.setVisibility(View.INVISIBLE);
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
                    setCameraParameter(CameraUtil.CAMERA_BACK);

                    camera.setDisplayOrientation(90);
                    camera.startPreview();//开始预览
                    cameraPosition = 1;
                    focuseState(Focus_State_Write,false);
                    camera.autoFocus(autoFocusCallback);
                    focus_view.setVisibility(View.VISIBLE);
                    break;
                }
            }

        }
    }

    private void setCameraParameter(int cameraPosition){
        photoParameters = camera.getParameters();

        if(cameraPosition == CameraUtil.CAMERA_FRONT){
            photoParameters.set("rotation",90);
        }

        // 获取屏幕宽高比
        String srcnRatio = CameraUtil.getWHratioString(viApplication.getViApp().getScreenHeight(),viApplication.getViApp().getScreenWidth());
        Size preSize = cameraUtil.getPreviewSizeByRatio(cameraPosition, srcnRatio);
        Size picSize = cameraUtil.getPictureSizeByRatio(cameraPosition, srcnRatio);
        try {
            photoParameters.setPreviewSize(preSize.width, preSize.height);
            photoParameters.setPictureSize(picSize.width, picSize.height);
        }catch (NullPointerException e){
            e.printStackTrace();
            String msg = "Camera Parameters setting error!";
            Toast.makeText(this, msg ,Toast.LENGTH_SHORT).show();
            Log.e(TAG, msg);
            photoParameters.setPreviewSize(viApplication.getViApp().getScreenWidth(), viApplication.getViApp().getScreenHeight());
            photoParameters.setPictureSize(viApplication.getViApp().getScreenWidth(), viApplication.getViApp().getScreenHeight());
        }

        camera.setParameters(photoParameters);
    }

    /**
     * 拍照
     */
    private void takePhoto(){
        if(cameraPosition == 0||focuseState(Focus_State_Read,false)) {
            picTakenScrnOrient = scrnOrient;
            save_photo_state = SAVING_PHOTO;
            progressDialog = ProgressDialog.show(this,null,"正在处理...",true,false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    camera.takePicture(null,null,jpeg);
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!ifStopCPThread){
                        if(save_photo_state == SAVED_PHOTO){
                            // 保存图片完成后进行图片处理
                            if(cameraRequest.ifCrop()){
                                // 转到裁剪Activity
                                Intent intent = new Intent(getApplicationContext(), CropImageActivity.class);
                                BitmapIntent bitmapIntent = new BitmapIntent(tmpPhotoPath);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("cameraRequest", cameraRequest);
                                bundle.putSerializable("bitmapIntent", bitmapIntent);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            } else {
                                take_photo_rl.setVisibility(View.INVISIBLE);
                                viSize load_size = BitmapUtil.getImageReq(tmpPhotoPath, viApplication.getViApp().getScreenWidth(),
                                        viApplication.getViApp().getScreenHeight() - 2 * getResources().getDimensionPixelSize(R.dimen.layout_bar_height));
                                Bitmap preview_photo = BitmapUtil.decodeSampledBitmapFromFile(tmpPhotoPath, load_size);
                                preview_photo_view.setImageBitmap(preview_photo);
                                preview_photo_rl.setVisibility(View.VISIBLE);
                            }
                            break;
                        } else if(save_photo_state == SAVED_ERROR){//保存发生错误
                            // 先停止等待动画
                            handler.sendEmptyMessage(STOP_PROGRESS);
                        }
                    }
                }
            }).start();

        }
    }

    /**
     * 显示/隐藏拍照模式菜单
     */
    private void mode_list_show(){
        if(!modevlist_visible){
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,-1.0f,
                    Animation.RELATIVE_TO_SELF,0f);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(300);
            take_photo_mode_listView.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    take_photo_mode_listView.clearAnimation();
                    take_photo_mode_listView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            modevlist_visible = true;
        }else {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,-1f);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(300);
            take_photo_mode_listView.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    take_photo_mode_listView.clearAnimation();
                    take_photo_mode_listView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            modevlist_visible = false;
        }
    }

    /**
     * 调整拍照布局，并提示横竖屏切换
     */
    private void setTakePhotoLayout(){
        switch (current_mode){
            case TakePhotoMode.AUTO_MODE:
                // 隐藏切换提示布局
                (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.INVISIBLE);
                if (scrnOrient == ORIENTATION_LAND || scrnOrient == ORIENTATION_REV_LAND) {
                    setBarrierAnimation(false);
                } else {
                    setBarrierAnimation(true);
                }
                break;
            case TakePhotoMode.LANDSCAPE_MODE:
                if(scrnOrient!=ORIENTATION_LAND&&scrnOrient!=ORIENTATION_REV_LAND){
                    switch_mode_hint.setImageResource(R.drawable.switch_human2landscape_icn);
                    switch_mode_hint.setRotation(90 - scrnOrient);
                    // 显示切换提示
                    (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.VISIBLE);
                    setBarrierAnimation(false);
                    isPreviewing = false;
                    btn_shutter.setImageResource(R.drawable.photo_take_button_disable);
                    btn_shutter.setClickable(false);
                } else {
                    (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.GONE);
                    setBarrierAnimation(false);
                    isPreviewing = true;
                    btn_shutter.setImageResource(R.drawable.btn_take_photo);
                    btn_shutter.setClickable(true);
                }
                break;
            case TakePhotoMode.FACE_MODE:
                if(scrnOrient!=ORIENTATION_PORTAIT&&scrnOrient!=ORIENTATION_REV_PORTRAIT){
                    switch_mode_hint.setImageResource(R.drawable.switch_landscape2human_icn);
                    switch_mode_hint.setRotation(90 - scrnOrient);
                    // 显示切换提示
                    (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.VISIBLE);
                    isPreviewing = false;
                    setBarrierAnimation(true);
                    btn_shutter.setImageResource(R.drawable.photo_take_button_disable);
                    btn_shutter.setClickable(false);
                } else {
                    (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.GONE);
                    setBarrierAnimation(true);
                    isPreviewing = true;
                    btn_shutter.setImageResource(R.drawable.btn_take_photo);
                    btn_shutter.setClickable(true);
                }
                break;
        }
    }

    /**
     * 拍照遮幅动画设置
     * @param ifBarrier:是否被遮
     */
    private void setBarrierAnimation(boolean ifBarrier){
        if(ifBarrier&&!isBarrierShown) {
            previewing_barrier.setVisibility(View.VISIBLE);
            isBarrierShown = true;
        } else if(!ifBarrier&&isBarrierShown){
            previewing_barrier.setVisibility(View.GONE);
            isBarrierShown = false;
        }
    }
    // 创建jpeg图片回调数据对象,对图片进行旋转和初步裁剪
    PictureCallback jpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            isPreviewing = false;
            Bitmap saved_photo = BitmapFactory.decodeByteArray(data, 0, data.length);
            // 是否裁剪图片
            if (cameraRequest.ifCrop()) {
                /*存储的图片尺寸*/
                int storeImageWidth = 0, storeImageHeight = 0;
                /*保存的图片和预览图片的比例*/
                float ratio = (float)photoParameters.getPictureSize().width/photoParameters.getPreviewSize().width;
                // 确定最终图片的宽高
                if (picTakenScrnOrient == ORIENTATION_LAND || picTakenScrnOrient == ORIENTATION_REV_LAND) {
                    storeImageWidth = saved_photo.getWidth() - 2 * Math.round(getResources().getDimensionPixelSize(R.dimen.layout_bar_height)*ratio);
                    storeImageHeight = saved_photo.getHeight();
                } else {
                    storeImageWidth = saved_photo.getWidth() - Math.round(getResources().getDimensionPixelSize(R.dimen.layout_bar_height)*ratio + barrier_height*ratio);
                    storeImageHeight = saved_photo.getHeight();
                }
                // 对图片进行旋转处理
                {
                    // 后置摄像头对照片进行顺时针旋转90度，前置摄像头则逆时针转90度
                    Matrix matRotate = new Matrix();
                    if (cameraPosition == 0) {
                        matRotate.setRotate(-picTakenScrnOrient);
                    } else {
                        matRotate.setRotate(picTakenScrnOrient);
                    }
                    // 再对图片进行初步剪切
                    int h = 0, w = Math.round(getResources().getDimensionPixelSize(R.dimen.layout_bar_height) * ratio);
                    try{
                        saved_photo = Bitmap.createBitmap(saved_photo, w, h, storeImageWidth, storeImageHeight, null, true);
                        // 再进行旋转
                        saved_photo = Bitmap.createBitmap(saved_photo, 0, 0, saved_photo.getWidth(), saved_photo.getHeight(), matRotate, true);
                    } catch (OutOfMemoryError e){
                        e.printStackTrace();
                        System.gc();
                        save_photo_state = SAVED_ERROR;
                        return;
                    }

                }
                try {
                    File file = new File(viApplication.getViApp().getTempFileDir() + "//" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                            ".tmp");
                    tmpPhotoPath = file.getPath();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    saved_photo.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    bos.flush();    // 刷新此缓冲区的输出流
                    bos.close();    // 关闭此输出流并释放与此流有关的所有系统资源
                    save_photo_state = SAVED_PHOTO;
                    saved_photo.recycle();
                    System.gc();
                }catch (IOException e){
                    e.printStackTrace();
                    save_photo_state = SAVED_ERROR;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    save_photo_state = SAVED_ERROR;
                }
            }
        }
    };

    /**
     * 存储照片
     */
    private void storePhoto(){
        String filepath = viApplication.getViApp().getSavedPhotoDir() + "//" +  new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
        File srcfile = new File(tmpPhotoPath);
        File newfile = new File(filepath);
        if(!srcfile.exists()||!srcfile.isFile()){
            Toast.makeText(this,"保存文件错误",Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if(srcfile.renameTo(newfile)){
            Toast.makeText(this,"文件保存成功:\n"+filepath,Toast.LENGTH_LONG).show();
            srcfile.delete();
        }
        // 初始化BitmapIntent
        BitmapIntent bitmapIntent = new BitmapIntent(tmpPhotoPath);
        Bundle bundle = new Bundle();
        bundle.putSerializable("bitmapIntent",bitmapIntent);
        // 跳转到其他的Activity
        if(cameraRequest.backOriginClass()){
            Intent newint = new Intent(viCameraActivity.this, cameraRequest.getOriginClass());
            newint.putExtras(bundle);
            setResult(RESULT_OK, newint);
        }else if(cameraRequest.goDestClass()){
            Intent newint = new Intent(viCameraActivity.this, cameraRequest.getDestClass());
            newint.putExtras(bundle);
            startActivity(newint);
        }
        System.gc();
        this.finish();
    }

    /**
     * 放弃存储照片
     */
    private void discardPhoto(){
        // 显示take_photo_bar_layout
        take_photo_rl.setVisibility(View.VISIBLE);
        preview_photo_rl.setVisibility(View.INVISIBLE);
        // 只有后置摄像头才能设置聚焦
        if(cameraPosition == 1) {
            focus_view.setVisibility(View.VISIBLE);
        }
        camera.setDisplayOrientation(90);
        camera.startPreview();
        isPreviewing = true;
        camera.autoFocus(autoFocusCallback);
    }

    private void setScreen(){
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.screenBrightness = 255;
        this.getWindow().setAttributes(lp);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private synchronized boolean focuseState(int ReadOrWrite,boolean value){
        synchronized (this){
            if(ReadOrWrite == Focus_State_Read)
                return isFocused;
            else if(ReadOrWrite == Focus_State_Write)
                isFocused = value;
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            Toast.makeText(getApplicationContext(),"没有选择任何图片!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(requestCode==REQUEST_CODE_PICK_IMAGE) {
            Uri uri = data.getData();
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(uri, proj, null, null, null);
            // 将光标移至开头
            cursor.moveToFirst();
            // 获得图片索引值
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            // 最后根据索引值获取图片路径
            String filepath = cursor.getString(index);
            // 判断图像大小是否超过最大值，超过则不加载
            if (BitmapUtil.getImageSizeBeforeLoad(filepath) > BitmapUtil.IMAGE_MAX_LOAD_SIZE) {
                Toast.makeText(this, "图片尺寸过大!", Toast.LENGTH_SHORT).show();
                return;
            }
            tmpPhotoPath = filepath;
            // 初始化BitmapIntent
            int orientation = BitmapUtil.readPictureDegree(tmpPhotoPath);
            BitmapIntent bitmapIntent = new BitmapIntent(orientation,tmpPhotoPath,BitmapIntent.FROM_CAMERA);
            Bundle bundle = new Bundle();
            bundle.putSerializable("bitmapIntent",bitmapIntent);
            if (cameraRequest.ifCrop()) {
                Intent intent = new Intent(viCameraActivity.this, CropImageActivity.class);
                bundle.putSerializable("cameraRequest", cameraRequest);
                intent.putExtras(bundle);
                startActivity(intent);
                this.finish();
            }else {
                // 跳转到其他的Activity
                if(cameraRequest.backOriginClass()){
                    Intent newint = new Intent(viCameraActivity.this, cameraRequest.getOriginClass());
                    newint.putExtras(bundle);
                    setResult(RESULT_OK, newint);
                }else if(cameraRequest.goDestClass()){
                    Intent newint = new Intent(viCameraActivity.this, cameraRequest.getDestClass());
                    newint.putExtras(bundle);
                    startActivity(newint);
                }
                this.finish();
            }
        }
    }

    @Override
    protected void onPause() {
        // 取消注册监听器
        if(shakeDetector!=null) {
            shakeDetector.stop();
        }
        if(scrnOrientDetector!=null) {
            scrnOrientDetector.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(shakeDetector!=null) {
            shakeDetector.registerOnShakeListener(onShakeListener);
            shakeDetector.start();
        }
        if(scrnOrientDetector!=null) {
            scrnOrientDetector.registerOnShakeListener(onSrcnListener);
            scrnOrientDetector.start();
        }
        setScreen();
        super.onResume();
    }


    //无意中按返回键时要释放内存
    @Override
    public void onBackPressed() {
        if(save_photo_state == SAVING_PHOTO){
            ifStopCPThread = true;
            save_photo_state = -1;
            return;
        }
        this.finish();
        System.gc();

    }

    @Override
    public void finish() {
        // 停止等待动画
        handler.sendEmptyMessage(STOP_PROGRESS);
        super.finish();
    }
}
