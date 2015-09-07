package com.kairong.viUIControls.viCamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.sensorDetector.ScrnOrientDetector;
import com.kairong.viUIControls.viCropImage.CropImageActivity;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.CameraUtil;
import com.kairong.viUtils.viSize;
import com.kairong.vision_recognition.R;
import com.kairong.sensorDetector.ShakeDetector;
import com.kairong.vision_recognition.viApplication;

import java.io.File;
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
public class viCameraActivity extends Activity implements SurfaceHolder.Callback,View.OnClickListener {
    private volatile CameraHelper cameraHelper;
    private SurfaceView surface;
    private SurfaceHolder holder;
    private int scrnOrient = 90;                        // 当前屏幕朝向
    private int picTakenScrnOrient = 0;                 // 拍照时的手机屏幕朝向
    private int current_mode = -1;                      // 当前拍照模式

    private ImageView focus_view;                // 显示对焦光标
    private ImageView btn_shutter;               // 快门按钮
    private ImageView btn_gallery;               // 相册方式获取
    private ImageView btn_camera_change;         // 切换相机
    private ImageView previewing_barrier;        // 预览照片遮幅
    private ImageView switch_mode_hint;          // 切换拍照模式提示
    private ProgressDialog progressDialog;
    // 各个布局的引用
    private RelativeLayout take_photo_rl;
    private RelativeLayout preview_photo_rl ;

    private ListView take_photo_mode_listView;
    /*晃动检测器—用于检测摄像头剧烈晃动，从而启动自动聚焦*/
    private ShakeDetector shakeDetector;
    /*屏幕方向检测器，用于监测屏幕的旋转*/
    private ScrnOrientDetector scrnOrientDetector;
    /*相机请求*/
    private CameraRequest cameraRequest;
    /*头像模式拍照比例*/
    private static final float HUMAN_MODE_WH_RATIO = 1.0f;
    /*停止保存照片线程*/
    private boolean ifStopCPThread = false;
    /*拍照模式列表是否可见*/
    private boolean modevlist_visible = false;
    /*拍照遮幅可见标志*/
    private boolean isBarrierShown = false;

    /*拍照遮幅高度*/
    private int barrier_height = 0;
    /*显示进程状态*/
    private final static int STOP_PROGRESS = 3238;
    private static final int REQUEST_CODE_PICK_IMAGE = 3037;
    //手机屏幕的旋转方向
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

    // 线程共享变量
    public static ShareData save_photo_state = new ShareData();

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
        initCameraHelper();
        setScreen();//拍照过程屏幕一直处于高亮
        // 获取源Activity的相机请求
        cameraRequest = (CameraRequest)getIntent().getSerializableExtra("cameraRequest");
        // 获取相机模式
        current_mode = cameraRequest.getPhotoMode();
        // 初始化视图
        initView();
        setTakePhotoLayout();
    }
    // 初始化cameraHelper
    private void initCameraHelper(){
        surface = (SurfaceView)findViewById(R.id.surfaceview);
        holder = surface.getHolder();   //获得句柄
        holder.addCallback(this);       //添加回调
        // surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraHelper = new CameraHelper(this,holder);
    }
    // 初始化视图资源
    private void initView(){
        // 设置控件资源ID
        TextView mode_text = (TextView)findViewById(R.id.take_photo_mode_text);
        LinearLayout take_photo_mode_ll = (LinearLayout)findViewById(R.id.take_photo_mode_btn_layout);
        // 全局控件初始化
        btn_gallery = (ImageView)findViewById(R.id.btn_take_photo_gallery);
        btn_camera_change = (ImageView)findViewById(R.id.btn_camera_change);
        btn_shutter = (ImageView)findViewById(R.id.btn_take_photo);
        focus_view = (ImageView)findViewById(R.id.focus);
        previewing_barrier = (ImageView)findViewById(R.id.take_photo_barrier);
        switch_mode_hint = (ImageView)findViewById(R.id.switch_mode_hint_view);


        take_photo_rl = (RelativeLayout)findViewById(R.id.take_photo_layout);
        preview_photo_rl = (RelativeLayout)findViewById(R.id.preview_photo_layout);
        take_photo_mode_listView = (ListView)findViewById(R.id.take_photo_mode_list);

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
                if (cameraHelper.isPreviewing() && cameraHelper.getCamera_position()==Camera.CameraInfo.CAMERA_FACING_BACK && surfaceRect.contains((int) m_X, (int) m_Y)) {
                    cameraHelper.autoFocus(handler);
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
                case CameraHelper.MSG_FOCUSING:
                    if (theActivity.cameraHelper.getCamera_position() == Camera.CameraInfo.CAMERA_FACING_BACK&&theActivity.cameraHelper.isPreviewing())
                        theActivity.focus_view.setBackgroundResource(R.drawable.focus_focusing);
                    break;
                case CameraHelper.MSG_FOCUSED:
                    if (theActivity.cameraHelper.getCamera_position() == Camera.CameraInfo.CAMERA_FACING_BACK&&theActivity.cameraHelper.isPreviewing())
                        theActivity.focus_view.setBackgroundResource(R.drawable.focus_focused);
                    break;
                case CameraHelper.MSG_FOCUS_FAILED:
                    if (theActivity.cameraHelper.getCamera_position() == Camera.CameraInfo.CAMERA_FACING_BACK&&theActivity.cameraHelper.isPreviewing())
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
            if(cameraHelper.getCamera_position() == Camera.CameraInfo.CAMERA_FACING_BACK&&cameraHelper.isPreviewing()){
                cameraHelper.autoFocus(handler);

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
            setTakePhotoLayout();
        }
    };
    // 全局控件点击事件监听
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btn_take_photo_gallery:
                // 打开相册
                openGallery();
                break;
            case R.id.btn_camera_change:
                // 切换前后摄像头
                cameraHelper.switchCamera();
                if(cameraHelper.getCamera_position()==Camera.CameraInfo.CAMERA_FACING_BACK){
                    focus_view.setVisibility(View.VISIBLE);
                    cameraHelper.autoFocus(handler);
                }else{
                    focus_view.setVisibility(View.GONE);
                }
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
                // 显示拍照模式
                mode_list_show();
                break;
        }
    }

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

    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defCam = sharedPreferences.getString(getResources().getString(R.string.def_cam_key),"1");
        if(defCam.equals("0")){
            cameraHelper.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }else if (defCam.equals("1")){
            cameraHelper.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            focus_view.setVisibility(View.GONE);
        }else {
            cameraHelper.open();
        }

        cameraHelper.autoFocus(handler);
        shakeDetector = new ShakeDetector(this);
        scrnOrientDetector = new ScrnOrientDetector(this);

        shakeDetector.registerOnShakeListener(onShakeListener);
        scrnOrientDetector.registerOnShakeListener(onSrcnListener);
        // 启动监听
        shakeDetector.start();
        scrnOrientDetector.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 置预览回调为空，再关闭预览
        cameraHelper.stop();
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
    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }
    /**
     * 拍照
     */
    private void takePhoto(){
        if(cameraHelper.getCamera_position() == Camera.CameraInfo.CAMERA_FACING_FRONT || cameraHelper.isFocused()) {
            picTakenScrnOrient = scrnOrient;
            progressDialog = ProgressDialog.show(this,null,"正在处理...",true,false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cameraHelper.takePhoto(picTakenScrnOrient, barrier_height);
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!ifStopCPThread){
                        if(cameraHelper.getSave_photo_state() == CameraHelper.SAVED_PHOTO){
                            handler.sendEmptyMessage(STOP_PROGRESS);
                            // 保存图片完成后进行图片处理
                            if(cameraRequest.ifCrop()){
                                // 转到裁剪Activity
                                Intent intent = new Intent(getApplicationContext(), CropImageActivity.class);
                                BitmapIntent bitmapIntent = new BitmapIntent(cameraHelper.getTmpPhotoPath());
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("cameraRequest", cameraRequest);
                                bundle.putSerializable("bitmapIntent", bitmapIntent);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            } else {
                                take_photo_rl.setVisibility(View.INVISIBLE);
                                viSize load_size = BitmapUtil.getImageReq(cameraHelper.getTmpPhotoPath(), viApplication.getViApp().getScreenWidth(),
                                        viApplication.getViApp().getScreenHeight() - 2*getResources().getDimensionPixelSize(R.dimen.layout_bar_height));
                                Bitmap preview_photo = BitmapUtil.decodeSampledBitmapFromFile(cameraHelper.getTmpPhotoPath(), load_size);
                                ((ImageView)preview_photo_rl.findViewById(R.id.take_photo_preview)).setImageBitmap(preview_photo);
                                preview_photo_rl.setVisibility(View.VISIBLE);
                            }
                            break;
                        } else if(cameraHelper.getSave_photo_state() == CameraHelper.SAVED_ERROR){//保存发生错误
                            // 先停止等待动画
                            handler.sendEmptyMessage(STOP_PROGRESS);
                            cameraHelper.restartPreview();
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
//                    isPreviewing = false;
                    btn_shutter.setImageResource(R.drawable.photo_take_button_disable);
                    btn_shutter.setClickable(false);
                } else {
                    (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.GONE);
                    setBarrierAnimation(false);
//                    isPreviewing = true;
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
//                    isPreviewing = false;
                    setBarrierAnimation(true);
                    btn_shutter.setImageResource(R.drawable.photo_take_button_disable);
                    btn_shutter.setClickable(false);
                } else {
                    (findViewById(R.id.switch_mode_hint_layout)).setVisibility(View.GONE);
                    setBarrierAnimation(true);
//                    isPreviewing = true;
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

    /**
     * 存储照片
     */
    private void storePhoto(){
        String filepath = viApplication.getViApp().getSavedPhotoDir() + "//" +  new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
        File srcfile = new File(cameraHelper.getTmpPhotoPath());
        File newfile = new File(filepath);
        if(!srcfile.exists()||!srcfile.isFile()){
            Toast.makeText(this,"保存文件错误",Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if(srcfile.renameTo(newfile)&&srcfile.delete()){
            Toast.makeText(this,"文件保存成功:\n"+filepath,Toast.LENGTH_SHORT).show();
        }
        // 初始化BitmapIntent
        BitmapIntent bitmapIntent = new BitmapIntent(cameraHelper.getTmpPhotoPath());
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
        if(cameraHelper.getCamera_position() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            focus_view.setVisibility(View.VISIBLE);
        }
        cameraHelper.restartPreview();
    }

    private void setScreen(){
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.screenBrightness = 1;
        this.getWindow().setAttributes(lp);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

//    private synchronized boolean focuseState(int ReadOrWrite,boolean value){
//        synchronized (this){
//            if(ReadOrWrite == Focus_State_Read)
//                return cameraHelper.isFocused();
//            else if(ReadOrWrite == Focus_State_Write)
//                isFocused = value;
//        }
//        return false;
//    }
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
            // 初始化BitmapIntent
            int orientation = BitmapUtil.readPictureDegree(filepath);
            BitmapIntent bitmapIntent = new BitmapIntent(orientation,filepath,BitmapIntent.FROM_CAMERA);
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
        if(cameraHelper.getSave_photo_state() == CameraHelper.SAVING_PHOTO){
            ifStopCPThread = true;
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
class ShareData{
    private int value;
    public synchronized int get(){
        return value;
    }
    public synchronized void set(int value){
        this.value = value;
    }
}
