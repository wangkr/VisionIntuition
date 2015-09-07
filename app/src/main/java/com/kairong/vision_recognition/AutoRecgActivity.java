package com.kairong.vision_recognition;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.gpuimage.GPUImageFilter;
import com.kairong.gpuimage.GPUImageFilterTools;
import com.kairong.gpuimage.GPUImageView;
import com.kairong.gpuimage.GPUImage.OnPictureSavedListener;
import com.kairong.viUIControls.viCamera.CamTargetList;
import com.kairong.viUIControls.viCamera.CameraRequest;
import com.kairong.viUIControls.viCamera.CropPhotoType;
import com.kairong.viUIControls.viCamera.TakePhotoMode;
import com.kairong.viUIControls.viCamera.viCameraActivity;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.gpuimage.GPUImageFilterTools.FilterAdjuster;
import com.kairong.viUtils.viSize;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class AutoRecgActivity extends Activity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,
        OnPictureSavedListener{

    private int ImageView_Width;
    private int ImageView_Height;
    private GPUImageFilter mFilter = null;
    private FilterAdjuster mFilterAdjuster = null;
    private GPUImageView mGPUImageView;
    private Bitmap bitmap = null;
    private String filepath = null;

    private TabHost tabHost = null;
    private GridView image_filter_gv = null;
    private FrameLayout frameLayout = null;

    private final int CAMERA_IMAGE_CODE = 20152017;
    private final String FILTER_KEY_IMAGE = "image_key";
    private final String FILTER_KEY_TITLE = "title_key";
    private String TAG = "AutoRecgActivity";

    private String[] ratio_opt = new String[]{"16:9","5:3","4:3","3:2"};
    private CameraRequest cameraRequest = new CameraRequest(CropPhotoType.CROP_MULTI_RATIO, TakePhotoMode.AUTO_MODE,null, CamTargetList.getTargetClass("AutoRecgActivity"),
            null,ratio_opt);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_recognition_activity);

        filepath = getIntent().getStringExtra("imagepath");

        TextView btn_re_take = (TextView)findViewById(R.id.btn_re_take_photo);
        TextView btn_save_photo = (TextView)findViewById(R.id.btn_auto_save);
        SeekBar seekBar = (SeekBar)findViewById(R.id.auto_seek_bar);
        btn_re_take.setOnClickListener(this);
        btn_save_photo.setOnClickListener(this);
        seekBar.setOnClickListener(this);

        frameLayout = (FrameLayout)findViewById(R.id.gpu_image_layout);
        mGPUImageView = (GPUImageView)findViewById(R.id.auto_image_view);
        image_filter_gv = (GridView)findViewById(R.id.grid_list_view);
        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup(); // 初始化TabHost容器
        initTabWidget(); // 初始化TabWidget
        setFrameLayout();// 设置frameLayout的位置和tabhost的位置

        SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.imagefilter_item,new String[]{FILTER_KEY_IMAGE,FILTER_KEY_TITLE},new int[]{R.id.filter_image,R.id.filter_title});
        image_filter_gv.setAdapter(adapter);
        image_filter_gv.setOnItemClickListener(filter_item_listener);

        // 设置加载图片大小
        viSize load_size = BitmapUtil.getImageReq(filepath,ImageView_Width,
                ImageView_Height);
        bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath, load_size);
        mGPUImageView.setImage(bitmap);
    }

    private void setFrameLayout(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath,options);
        int outWidth = options.outWidth,outHeight = options.outHeight;
        float imageRatio = (float)outWidth/outHeight;
        int fr_height = Math.round((float)viApplication.getViApp().getScreenWidth()/imageRatio);
        // 先设置tabhost的位置
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(tabHost.getLayoutParams());

        Resources r = this.getResources();
        int marginTop = r.getDimensionPixelSize(R.dimen.auto_title_bar_height) + fr_height + 2*r.getDimensionPixelSize(R.dimen.empty_area_width);
        layoutParams.setMargins(0, marginTop, 0, 0);
        tabHost.setLayoutParams(layoutParams);
        // 再设置tabhost的位置
        RelativeLayout.LayoutParams fr_layout_parm = new RelativeLayout.LayoutParams(frameLayout.getLayoutParams());
        int fr_top = r.getDimensionPixelSize(R.dimen.auto_title_bar_height)+r.getDimensionPixelSize(R.dimen.empty_area_width);
        int fr_bottom = viApplication.getViApp().getScreenHeight() - fr_top - fr_height;
        fr_layout_parm.setMargins(0,fr_top,0,fr_bottom);
        frameLayout.setLayoutParams(fr_layout_parm);

        // 计算显示的图片宽高
        ImageView_Width = viApplication.getViApp().getScreenWidth();
        ImageView_Height = Math.round(ImageView_Width / imageRatio);
    }
    private void initTabWidget(){
        //在TabHost创建标签，然后设置：标题／图标／标签页布局
        View view1 = getLayoutInflater().inflate(R.layout.tabwidget_item,null);
        View view2 = getLayoutInflater().inflate(R.layout.tabwidget_item,null);
        View view3 = getLayoutInflater().inflate(R.layout.tabwidget_item,null);
        TextView tab_title1 = (TextView)view1.findViewById(R.id.widget_title);
        TextView tab_title2 = (TextView)view2.findViewById(R.id.widget_title);
        TextView tab_title3 = (TextView)view3.findViewById(R.id.widget_title);
        tab_title1.setText("文字");
        tab_title2.setText("特效");
        tab_title3.setText("贴图");
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(view1).setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(view2).setContent(R.id.tab2));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(view3).setContent(R.id.tab3));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,""+resultCode);
        if(resultCode!=RESULT_OK){
            Toast.makeText(AutoRecgActivity.this,"没有获取任何图片",Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case CAMERA_IMAGE_CODE:// 从相机添加图片
                filepath = data.getStringExtra("imagepath");
                setFrameLayout();

                // 设置加载图片大小
                viSize load_size = BitmapUtil.getImageReq(filepath,ImageView_Width,
                        ImageView_Height);
                bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath, load_size);
                mGPUImageView.setImage(bitmap);
                break;
        }
    }

    AdapterView.OnItemClickListener filter_item_listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position == 0){
                mGPUImageView.setImage(bitmap);
            }
            if(position>0){
                GPUImageFilterTools.onChooseFilter(AutoRecgActivity.this, position-1,new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(GPUImageFilter filter) {
                        switchFilterTo(filter);
                        mGPUImageView.requestRender();
                    }
                });
            }
        }
    };
    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }
    private List<Map<String,Object>> getData(){
        int[] filter_icn_id = {
                R.drawable.test,R.drawable.amatorka,R.drawable.brannan,R.drawable.earlybird,
                R.drawable.i1977, R.drawable.inkwell,R.drawable.lomo,R.drawable.lordkelvin,
                R.drawable.rise, R.drawable.sepia,R.drawable.sutro,R.drawable.toaster,
                R.drawable.tonecurve,R.drawable.valencia,R.drawable.walden,R.drawable.hefe
        };
        String[] filter_icn_title={
                "原图","Amatorka","Brannan","Earlybird","1977","Inkwell","Lomo","LordKelvin",
                "Rise","Sepia","sutro","Toaster","ToneCurve","Valencia","Walden","Hefe"
        };
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        HashMap<String,Object> map = null;
        for(int i = 0;i < filter_icn_id.length;i++){
            map = new HashMap<String,Object>();
            map.put(FILTER_KEY_IMAGE,filter_icn_id[i]);
            map.put(FILTER_KEY_TITLE,filter_icn_title[i]);
            list.add(map);
        }
        return list;
    }
    private void saveImage() {
        String fileName = System.currentTimeMillis() + ".jpg";
        mGPUImageView.saveToPictures(viApplication.getViApp().getSavedPhotoDir(), fileName, this);
    }
    private void reTakePhoto(){
        Intent intent = new Intent(AutoRecgActivity.this,viCameraActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("cameraRequest",cameraRequest);
        intent.putExtras(bundle);
        startActivityForResult(intent, CAMERA_IMAGE_CODE);
    }
    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            statusBarHeight = frame.top;
        }
        return statusBarHeight;
    }
    @Override
    public void finish() {
        if(bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_auto_save:
                saveImage();
                break;
            case R.id.btn_re_take_photo:
                reTakePhoto();
                break;
        }
    }

    @Override
    public void onPictureSaved(Uri uri) {
        Toast.makeText(this, "Saved: " + uri.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        if (mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);
        }
        mGPUImageView.requestRender();
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
