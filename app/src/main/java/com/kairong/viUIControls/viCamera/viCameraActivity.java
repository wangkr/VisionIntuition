package com.kairong.viUIControls.viCamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.sensorDetector.ScrnOrientDetector;
import com.kairong.viUIControls.viCropImage.CropImageView;
import com.kairong.viUtils.BitmapUtil;
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
    // 布局按钮
    private ImageView btn_gallery = null;
    private ImageView btn_camera_change = null;
    private ImageView btn_shutter = null;
    private Button btn_take_photo_ok = null;
    private Button btn_take_photo_cancell = null;
    private Button btn_crop_cancell = null;
    private Button btn_crop_done = null;
    private SurfaceView surface = null;
    private Camera camera = null;                       // 声明相机
    private int cameraPosition = 1;                     // 0代表前置摄像头，1代表后置摄像头
    private int srcnOrient = 0;
    private CropImageView cropImageView = null;         // 裁剪图片View
    private SurfaceHolder holder = null;
    private ImageView focus_View = null;                // 显示对焦光标
    private ImageView imageView_photo_preview = null;   // 显示拍照后的预览图片
    private TextView  crop_wh_ratio_text = null;        // 显示当前裁剪的宽高比
    // 各个相对布局的引用
    private RelativeLayout take_photo_bar_rl = null;
    private RelativeLayout preview_photo_rl  = null;
    private RelativeLayout crop_photo_rl = null;
    private RelativeLayout  crop_wh_ratio_btn_rl= null;
    private ListView crop_wh_ratio_drop_down_listView = null;
    private Bitmap saved_photo = null;
    private Parameters previewParameters = null;
    // viCameraActivity结束时的跳转Activity
    private Class originActivity = null;
    private Class destActivity = null;
    private Thread myAFthread = null;                   // 自动对焦监测线程
    private File storeFileDir = null;
    // 晃动检测器—用于检测摄像头剧烈晃动，从而启动自动聚焦
    private ShakeDetector shakeDetector = null;
    // 屏幕方向检测器，用于监测屏幕的旋转
    private ScrnOrientDetector scrnOrientDetector = null;
    private boolean isPreviewing = false;
    private boolean isFocused = false;
    private boolean ifStopAFthread = false;
    private boolean ifCrop = false;
    private boolean cropvlist_visible = false;

    /*存储的图片尺寸*/
    private int storeImageWidth = 0;
    private int storeImageHeight = 0;
    /*裁剪宽高比*/
    private float crop_wh_ratio;

    private final static int MSG_FOCUSED = 3235;
    private final static int MSG_FOCUS_FAILED = 3236;
    private static final int REQUEST_CODE_PICK_IMAGE = 3037;
    /*手机屏幕的旋转方向*/
    /*竖直方向*/
    public static final int ORIENTATION_PORTAIT = 90;
    /*水平方向*/
    public static final int ORIENTATION_LAND = 0;
    /*反方向水平方向*/
    public static final int ORIENTATION_REV_LAND = 180;
    /*反方向竖直方向*/
    public static final int ORIENTATION_REV_PORTRAIT = -90;

    private final String TAG = "viCameraActivity";
    private final static String[] vlist_text = new String[]{"9:16","16:9","3:4","4:3","2:3","3:2"};

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.take_photo_activity);
        // 获取跳转Activity class 引用
        String originTag = getIntent().getStringExtra("OriginTag");
        String destTag  =getIntent().getStringExtra("DestTag");
        originActivity = CamTargetList.getTargetClass(originTag);
        destActivity = CamTargetList.getTargetClass(destTag);
        // 是否进行裁剪
        ifCrop = getIntent().getBooleanExtra("ifCrop",false);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        storeImageWidth = ((viApplication)getApplication()).getScreenHeight() - (int)( 2*getResources().getDimension(R.dimen.photo_preview_bar_height));
        storeImageHeight = ((viApplication)getApplication()).getScreenWidth();

        // 初始化控件资源ID
        initView();
        // 初始化控件监听
        initListener();

        // 照片固定的比例进行裁剪,默认16:9
        crop_wh_ratio = viApplication.viApp.getCROP_PHOTO_WH_RATIO("16:9");

        storeFileDir = new File(Environment.getExternalStorageDirectory(), "VisionIntuition");
        if (!storeFileDir.exists()) {
            storeFileDir.mkdir();
        }

    }

    // 初始化控件资源ID
    private void initView(){
        btn_gallery = (ImageView)findViewById(R.id.btn_take_photo_gallery);
        btn_camera_change = (ImageView)findViewById(R.id.btn_camera_change);
        btn_shutter = (ImageView)findViewById(R.id.btn_take_photo);
        btn_take_photo_ok = (Button)findViewById(R.id.btn_take_photo_ok);
        btn_take_photo_cancell = (Button)findViewById(R.id.btn_take_photo_cancell);
        btn_crop_cancell = (Button)findViewById(R.id.btn_crop_cancell);
        btn_crop_done = (Button)findViewById(R.id.btn_crop_done);
        crop_wh_ratio_text = (TextView)findViewById(R.id.crop_wh_ratio_text);
        surface = (SurfaceView)findViewById(R.id.surfaceview);
        focus_View = (ImageView)findViewById(R.id.focus);
        holder = surface.getHolder();//获得句柄
        holder.addCallback(this);//添加回调
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前
        take_photo_bar_rl = (RelativeLayout)findViewById(R.id.take_photo_bar_layout);
        preview_photo_rl = (RelativeLayout)findViewById(R.id.preview_photo_layout);
        crop_photo_rl = (RelativeLayout)findViewById(R.id.crop_photo_layout);
        crop_wh_ratio_btn_rl = (RelativeLayout)findViewById(R.id.crop_wh_ratio_btn_layout);
        cropImageView = (CropImageView)findViewById(R.id.crop_image_view);
        crop_wh_ratio_drop_down_listView = (ListView)findViewById(R.id.crop_wh_ratio_drop_down_list);

        SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.crop_wh_ratio_vlist,new String[] {"vlist_image","vlist_text"},new int[] {R.id.vlist_image,R.id.vlist_text});
        crop_wh_ratio_drop_down_listView.setAdapter(adapter);
        crop_wh_ratio_drop_down_listView.setOnItemClickListener(onItemClickListener);
    }
    // 控件监听初始化
    private void initListener(){
        // 设置控件监听
        btn_shutter.setOnClickListener(gl_listener);
        btn_camera_change.setOnClickListener(gl_listener);
        btn_gallery.setOnClickListener(gl_listener);
        btn_take_photo_cancell.setOnClickListener(gl_listener);
        btn_take_photo_ok.setOnClickListener(gl_listener);
        btn_crop_cancell.setOnClickListener(gl_listener);
        btn_crop_done.setOnClickListener(gl_listener);
        crop_wh_ratio_btn_rl.setOnClickListener(gl_listener);

        surface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Rect surfaceRect = new Rect();
                view.getDrawingRect(surfaceRect);
                int photo_bar_height = (int) (getResources().getDimension(R.dimen.photo_preview_bar_height));
                surfaceRect.top += photo_bar_height;
                surfaceRect.bottom -= photo_bar_height;
                float m_X = motionEvent.getX(0);
                float m_Y = motionEvent.getY(0);
                if (isPreviewing && cameraPosition == 1 && surfaceRect.contains((int) m_X, (int) m_Y)) {
                    isFocused = false;
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
            if (theActivity.cameraPosition == 1) {
                switch (msg.what) {
                    case MSG_FOCUSED:
                        theActivity.focus_View.setBackgroundResource(R.drawable.focus_focused);
                        break;
                    case MSG_FOCUS_FAILED:
                        theActivity.focus_View.setBackgroundResource(R.drawable.focus_focus_failed);
                        break;
                }
                super.handleMessage(msg);
            }
        }
    }
    private viCmHandler handler = new viCmHandler(this);
    // 设置一个晃动监听器--发生剧烈晃动就重新聚焦
    ShakeDetector.OnShakeListener onShakeListener = new ShakeDetector.OnShakeListener() {
        @Override
        public void onShake() {
            if(cameraPosition == 1&&isPreviewing){
                isFocused = false;
                Message msg = Message.obtain();
                msg.what = MSG_FOCUS_FAILED;
                handler.sendMessage(msg);
            }
        }
    };
    // 设置一个屏幕旋转监听器--发生屏幕旋转就重新检测
    ScrnOrientDetector.OnSrcnListener onSrcnListener = new ScrnOrientDetector.OnSrcnListener() {
        @Override
        public void onSrcnRoate(int Orientation) {
            if (Orientation>45&&Orientation<135) {
                srcnOrient = ORIENTATION_REV_LAND;
            }else if (Orientation>135&&Orientation<225){
                srcnOrient = ORIENTATION_REV_PORTRAIT;
            }else if (Orientation>225&&Orientation<315){
                srcnOrient = ORIENTATION_LAND;
            }else if ((Orientation>315&&Orientation<360)||(Orientation>=0&&Orientation<45)){
                srcnOrient = ORIENTATION_PORTAIT;
            }
        }
    };
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            float cur_crop_wh_ratio = viApplication.viApp.getCROP_PHOTO_WH_RATIO(vlist_text[position]);
            crop_wh_vlist_show_hide();
            crop_wh_ratio_text.setText(vlist_text[position]);
            if(cur_crop_wh_ratio!=crop_wh_ratio){
                cropImageView.refreshDrawable(cur_crop_wh_ratio);
                crop_wh_ratio = cur_crop_wh_ratio;
            }
        }
    };
    // 全局按钮控件监听
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
                    //切换前后摄像头
                   switchCamera();
                    break;
                case R.id.btn_take_photo:
                    // 拍照
                    takePhoto();
                    break;
                case R.id.btn_take_photo_ok:
                    // 存储照片
                    storePhoto();
                    break;
                case R.id.btn_take_photo_cancell:
                    // 丢弃照片
                    discardPhoto();
                    break;
                case R.id.btn_crop_cancell:
                    // 丢弃照片
                    discardPhoto();
                    break;
                case R.id.btn_crop_done:
                    // 存储照片
                    storePhoto();
                    break;
                case R.id.crop_wh_ratio_btn_layout:
                    crop_wh_vlist_show_hide();
                    break;
            }
        }
    };

    // 得到裁剪宽高比选项菜单内容
    private List<Map<String,Object>> getData(){
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

        int[] vlist_image = new int[]{R.drawable.w9h16_icn,R.drawable.w16h9_icn,R.drawable.w3h4_icn,
                                        R.drawable.w4h3_icn,R.drawable.w3h2_icn,R.drawable.w2h3_icn};

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
            isFocused = success;
        }


    };

    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(camera == null){
            camera = Camera.open();

            try {
                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                camera.setDisplayOrientation(90);
                camera.startPreview();// 开始预览
                // 设置存储照片参数
                previewParameters = camera.getParameters();
                previewParameters.setPictureFormat(PixelFormat.JPEG);

                isPreviewing = true;

                shakeDetector = new ShakeDetector(this);
                scrnOrientDetector = new ScrnOrientDetector(this);

                shakeDetector.registerOnShakeListener(onShakeListener);
                scrnOrientDetector.registerOnShakeListener(onSrcnListener);
                // 启动监听
                shakeDetector.start();
                scrnOrientDetector.start();
                // 设置自动聚焦定时器
                if(myAFthread == null) {
                    myAFthread = new Thread(new myAutoFocusThread());
                    myAFthread.start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //当surfaceview关闭时，先停止自动对焦线程，再关闭预览并释放资源
        // 停止线程
        ifStopAFthread = true;
        if(myAFthread!=null) {
            myAFthread.interrupt();
            myAFthread = null;
        }
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
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    Parameters parameters = camera.getParameters();
                    parameters.set("rotation", 90);
                    camera.setParameters(parameters);
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.setDisplayOrientation(90);
                    camera.startPreview();//开始预览
                    isFocused = true;
                    focus_View.setVisibility(View.INVISIBLE);
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
                    camera.setDisplayOrientation(90);
                    camera.startPreview();//开始预览
                    cameraPosition = 1;
                    isFocused = false;
                    focus_View.setVisibility(View.VISIBLE);
                    break;
                }
            }

        }
    }
    /**
     * 拍照
     */
    private void takePhoto(){
        // 首先自动对焦
        if(cameraPosition == 1&&!isFocused)
        camera.autoFocus(autoFocusCallback);
        if(cameraPosition == 0||isFocused){
            // 设置照片分辨率
            if(cameraPosition == 1){
                previewParameters.setPictureSize(((viApplication)getApplication()).getScreenHeight(), 
                        ((viApplication)getApplication()).getScreenWidth());
                // 设置参数并拍照
                camera.setParameters(previewParameters);
            }
            camera.takePicture(null, null, jpeg);
        }
    }

    /**
     * 设置默认裁剪宽高
     */
    private void setDefaultCropWH(){
        // 显示preview_photo_layout
        {
            take_photo_bar_rl.setVisibility(View.INVISIBLE);
            focus_View.setVisibility(View.INVISIBLE);
        }
        int cropWidth,cropHeight;
        Log.d(TAG,""+crop_wh_ratio);
        int srcWidth = saved_photo.getWidth(),srcHeight = saved_photo.getHeight();
        float src_wh_ratio = (float)srcWidth/srcHeight;
        // 根据源图像的宽高比预设裁剪宽高比
        if(src_wh_ratio>=1){
            crop_wh_ratio = crop_wh_ratio > 1?crop_wh_ratio:1/crop_wh_ratio;
        } else {
            crop_wh_ratio = crop_wh_ratio < 1?crop_wh_ratio:1/crop_wh_ratio;
        }
        // 调整裁剪宽高
        if(crop_wh_ratio>=src_wh_ratio){
            cropWidth = srcWidth;
            cropHeight = (int)(cropWidth / crop_wh_ratio);
        } else {
            cropHeight = srcHeight;
            cropWidth = (int)(cropHeight * crop_wh_ratio);
        }
        // 不能超过屏幕
        if(cropWidth>viApplication.viApp.getScreenWidth()||cropHeight>viApplication.viApp.getScreenHeight()){
            if(crop_wh_ratio>=1){
                cropWidth = viApplication.viApp.getScreenWidth();
                cropHeight = (int) (cropWidth/crop_wh_ratio);
            }
            else {
                cropHeight = viApplication.viApp.getScreenHeight();
                cropWidth = (int) (cropHeight*crop_wh_ratio);
            }
        }
        // 设置固定宽高比值
        cropImageView.setIfFixedWHratio(true);
        cropImageView.setDrawable(new BitmapDrawable(saved_photo), cropWidth, cropHeight);
        crop_photo_rl.setVisibility(View.VISIBLE);
    }

    /**
     * 显示/隐藏裁剪宽高比菜单
     */
    private void crop_wh_vlist_show_hide(){
        if(!cropvlist_visible){
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,-1.0f,
                    Animation.RELATIVE_TO_SELF,0f);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(500);
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(crop_wh_ratio_drop_down_listView.getLayoutParams());
            int left = viApplication.viApp.getScreenWidth() - (int)(getResources().getDimension(R.dimen.crop_drop_down_list_width));
            int top = (int)(getResources().getDimension(R.dimen.photo_preview_bar_height));
            layoutParams.setMargins(left, top, 0, 0);
            crop_wh_ratio_drop_down_listView.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    crop_wh_ratio_drop_down_listView.setLayoutParams(layoutParams);
                    crop_wh_ratio_drop_down_listView.setVisibility(View.VISIBLE);
                    crop_wh_ratio_drop_down_listView.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            cropvlist_visible = true;
        }else {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,-1f);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(500);
            crop_wh_ratio_drop_down_listView.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    crop_wh_ratio_drop_down_listView.clearAnimation();
                    crop_wh_ratio_drop_down_listView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            crop_wh_ratio_drop_down_listView.setVisibility(View.INVISIBLE);
            cropvlist_visible = false;
        }
    }
    // 创建jpeg图片回调数据对象
    PictureCallback jpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try{
                int picRotationDegree = srcnOrient;
                saved_photo = BitmapFactory.decodeByteArray(data,0,data.length);
                camera.stopPreview();
                isPreviewing = false;
                // 先对图片进行旋转处理
                {
                    // 后置摄像头对照片进行顺时针旋转90度，前置摄像头则逆时针转90度
                    Matrix matRotate = new Matrix();
                    if (cameraPosition == 0) {
                        matRotate.setRotate(-picRotationDegree);
                    } else {
                        matRotate.setRotate(picRotationDegree);
                    }
                    // 再对图片进行初步剪切
                    int h = 0, w = (int)(getResources().getDimension(R.dimen.photo_preview_bar_height));
                    saved_photo = Bitmap.createBitmap(saved_photo, w, h, storeImageWidth, storeImageHeight, null, true);
                    // 再进行旋转
                    saved_photo = Bitmap.createBitmap(saved_photo, 0, 0, saved_photo.getWidth(), saved_photo.getHeight(), matRotate, true);
                }
                // 是否裁剪图片
                if(ifCrop) {
                    setDefaultCropWH();
                }else {
                    preview_photo_rl.setVisibility(View.VISIBLE);
                    imageView_photo_preview.setImageBitmap(saved_photo);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    /**
     * 存储照片
     */
    private void storePhoto(){
        try {
            String filepath = storeFileDir.getPath() + "//" +  new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
            File file = new File(filepath);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            if(ifCrop){
                saved_photo = cropImageView.getCropImage();
            }
            saved_photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();    // 刷新此缓冲区的输出流
            bos.close();    // 关闭此输出流并释放与此流有关的所有系统资源
            // 跳转到其他的Activity
            if(originActivity!=null){
                Intent newint = new Intent(viCameraActivity.this, originActivity);
                newint.putExtra("imagepath", filepath);
                setResult(RESULT_OK,newint);
            }else if(destActivity!=null){
                Intent newint = new Intent(viCameraActivity.this, destActivity);
                newint.putExtra("imagepath", filepath);
                startActivity(newint);
            }
            saved_photo.recycle();//回收bitmap空间
            saved_photo = null;
            System.gc();
            this.finish();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 放弃存储照片
     */
    private void discardPhoto(){
        // 显示take_photo_bar_layout
        take_photo_bar_rl.setVisibility(View.VISIBLE);
        preview_photo_rl.setVisibility(View.INVISIBLE);
        crop_photo_rl.setVisibility(View.INVISIBLE);
        focus_View.setVisibility(View.VISIBLE);

        camera.setDisplayOrientation(90);
        camera.startPreview();
        isPreviewing = true;
        isFocused = false;
    }

    /**
     * 自动对焦线程
     */
    private class myAutoFocusThread implements Runnable{
        @Override
        public void run() {
            while (!ifStopAFthread) {
                if (camera != null) {
                    // 必须是后置摄像头且正在预览状态
                    if (cameraPosition==1&&isPreviewing && !isFocused)
                        camera.autoFocus(autoFocusCallback);
                        Message msg = Message.obtain();
                    if (isFocused) {
                        msg.what = MSG_FOCUSED;
                    } else {
                        msg.what = MSG_FOCUS_FAILED;
                    }
                    handler.sendMessage(msg);
                }
                try{
                    Thread.currentThread().sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        }
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
            // 获得图片索引值
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            // 将光标移至开头
            cursor.moveToFirst();
            // 最后根据索引值获取图片路径
            String filepath = cursor.getString(index);
            if (ifCrop) {
                saved_photo = BitmapFactory.decodeFile(filepath);
                setDefaultCropWH();
            }else {
                // 跳转到其他的Activity
                if (originActivity != null) {
                    Intent newint = new Intent(viCameraActivity.this, originActivity);
                    newint.putExtra("imagepath", filepath);
                    setResult(RESULT_OK, newint);
                } else if (destActivity != null) {
                    Intent newint = new Intent(viCameraActivity.this, destActivity);
                    newint.putExtra("imagepath", filepath);
                    startActivity(newint);
                }
                this.finish();
            }
        }
    }

    @Override
    protected void onPause() {
        // 取消注册监听器
        if(shakeDetector!=null)
            shakeDetector.unregisterOnShakeListener(onShakeListener);
        if(scrnOrientDetector!=null)
            scrnOrientDetector.unregisterOnShakeListener(onSrcnListener);
        // 停止自动对焦线程
        if(myAFthread!=null) {
            ifStopAFthread = true;
            myAFthread.interrupt();
            myAFthread = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(shakeDetector!=null)
            shakeDetector.registerOnShakeListener(onShakeListener);
        if(scrnOrientDetector!=null)
            scrnOrientDetector.registerOnShakeListener(onSrcnListener);
        // 重新启动自动对焦线程
        if(myAFthread == null) {
            isFocused = false;
            ifStopAFthread = false;
            myAFthread = new Thread(new myAutoFocusThread());
            myAFthread.start();
        }
        super.onResume();
    }


    //无意中按返回键时要释放内存
    @Override
    public void onBackPressed() {
        if(saved_photo!=null) {
            saved_photo.recycle();
            saved_photo = null;
        }
        this.finish();
        System.gc();
    }
}
