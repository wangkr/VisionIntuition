package com.kairong.viUIControls.viCropImage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.viUIControls.viCamera.BitmapIntent;
import com.kairong.viUIControls.viCamera.CameraRequest;
import com.kairong.viUIControls.viCamera.CropPhotoType;
import com.kairong.viUIControls.viCamera.viCameraActivity;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.viSize;
import com.kairong.vision_recognition.R;
import com.kairong.vision_recognition.viApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/8/3.
 * mail:wangkrhust@gmail.com
 */
public class CropImageActivity extends Activity{
    private CropImageView cropImageView = null;                 // 裁剪图片View
    private ListView crop_wh_ratio_drop_down_listView = null;   // 裁剪宽高比菜单列表
    private TextView crop_wh_ratio_text = null;         // 显示当前裁剪的宽高比
    private Bitmap recievedPhoto = null;                // 接受到的图片
    private Bitmap saved_photo = null;                  // 保存的图片
    private CameraRequest cameraRequest = null;         // 相机请求
    private BitmapIntent bitmapIntent = null;           // 图片传送对象
    private SimpleAdapter crop_wh_adapter = null;       // 裁剪宽高比选项列表适配器
    private LinearLayout crop_wh_ratio_btn_ll = null;   // 裁剪宽高比按钮布局
    private ProgressDialog progressDialog = null;       // 加载进程对话框
    private String[] vlist_text = null;                 // 裁剪选项列表文字描述
    private int[] vlist_image = null;                   // 裁剪选项列表图标id

    private float crop_wh_ratio;
    // 显示图片的最大宽高
    private int showMaxWidth;
    private int showMaxHeight;

    private boolean cropvlist_visible = false;
    private static final int REQUEST_CODE_PICK_IMAGE = 3022;
    private final static String[] WH_RATIO_TEXT_COMM = new String[]{"1:1","3:5","5:3","9:16","16:9","3:4","4:3","2:3","3:2"};
    private final static int[] WH_RATIO_IMAGE_COMM = new int[]{
            R.drawable.w1h1_icn,
            R.drawable.w3h5_icn,
            R.drawable.w5h3_icn,
            R.drawable.w9h16_icn,
            R.drawable.w16h9_icn,
            R.drawable.w3h4_icn,
            R.drawable.w4h3_icn,
            R.drawable.w2h3_icn,
            R.drawable.w3h2_icn
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_activity);

        Button btn_crop_back = (Button)findViewById(R.id.btn_crop_back);
        Button btn_crop_done = (Button)findViewById(R.id.btn_crop_done);
        crop_wh_ratio_btn_ll = (LinearLayout)findViewById(R.id.crop_wh_ratio_btn_layout);
        crop_wh_ratio_text = (TextView)findViewById(R.id.crop_wh_ratio_text);
        cropImageView = (CropImageView)findViewById(R.id.crop_image_view);
        crop_wh_ratio_drop_down_listView = (ListView)findViewById(R.id.crop_wh_ratio_drop_down_list);

        btn_crop_back.setOnClickListener(gl_listener);
        btn_crop_done.setOnClickListener(gl_listener);

        crop_wh_ratio_btn_ll.setOnClickListener(gl_listener);
        crop_wh_ratio_drop_down_listView.setAdapter(crop_wh_adapter);
        crop_wh_ratio_drop_down_listView.setOnItemClickListener(onItemClickListener);

        cameraRequest = (CameraRequest)getIntent().getSerializableExtra("cameraRequest");
        bitmapIntent = (BitmapIntent)getIntent().getSerializableExtra("bitmapIntent");
        showMaxWidth = viApplication.getViApp().getScreenWidth();
        showMaxHeight = viApplication.getViApp().getScreenHeight()-2*getResources().getDimensionPixelSize(R.dimen.layout_bar_height);

        // 显示被裁剪的图片的sample size
        viSize load_size = BitmapUtil.getImageReq(bitmapIntent.getBitmappath(),showMaxWidth,
                showMaxHeight);
        recievedPhoto = BitmapUtil.decodeSampledBitmapFromFile(bitmapIntent.getBitmappath(), load_size);

        // 裁剪宽高比初始化
        cropRatioInit();
        // 选项列表的初始化
        cropListInit();

    }

    View.OnClickListener gl_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_crop_back:
                    // 丢弃照片
                    backToCamera();
                    break;
                case R.id.btn_crop_done:
                    // 存储照片
                    storePhoto();
                    break;
                case R.id.crop_wh_ratio_btn_layout:
                    crop_wh_list_show();
                    break;
            }
        }
    };
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            float cur_crop_wh_ratio = CameraRequest.getWHratioFromString(vlist_text[position]);
            crop_wh_list_show();
            crop_wh_ratio_text.setText(vlist_text[position]);
            if(cur_crop_wh_ratio!=crop_wh_ratio){
                cropImageView.refreshDrawable(cur_crop_wh_ratio);
                crop_wh_ratio = cur_crop_wh_ratio;
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
    /**
     * 显示/隐藏裁剪宽高比菜单弹出动画
     */
    private void crop_wh_list_show(){
        if(!cropvlist_visible){
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,-1.0f,
                    Animation.RELATIVE_TO_SELF,0f);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(500);
            crop_wh_ratio_drop_down_listView.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
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

    /**
     * 裁剪宽高预处理
     */
    private void cropRatioInit(){
        int cropWidth,cropHeight;
        int srcWidth = recievedPhoto.getWidth(),srcHeight = recievedPhoto.getHeight();
        float srcWHratio = (float)srcWidth/srcHeight;
        // 设置固定宽高比值
        if(cameraRequest.getCropType()!= CropPhotoType.CROP_FREE_RATIO) {
            cropImageView.setIfFixedWHratio(true);
            crop_wh_ratio = cameraRequest.getDefaultCropWHratioValue();
            // 设置裁剪宽高
            if(crop_wh_ratio>=srcWHratio){
                cropWidth = srcWidth;
                cropHeight = (int)(cropWidth / crop_wh_ratio);
            } else {
                cropHeight = srcHeight;
                cropWidth = (int)(cropHeight * crop_wh_ratio);
            }
            // 不能超过屏幕
            if(cropWidth>showMaxWidth||cropHeight>showMaxHeight){
                if(cropWidth>=cropHeight){
                    cropWidth = showMaxWidth;
                    cropHeight = (int)(cropWidth / crop_wh_ratio);
                } else {
                    cropHeight = showMaxHeight;
                    cropWidth = (int)(cropHeight * crop_wh_ratio);
                }
            }
        } else {
            crop_wh_ratio = srcWHratio;
            cropWidth = (int)(srcWidth * 0.618);  // 默认黄金分割比
            cropHeight = (int)(srcHeight * 0.618);
        }
        cropImageView.setDrawable(new BitmapDrawable(recievedPhoto), cropWidth, cropHeight);
    }
    /**
     * 裁剪宽高比选项列表初始化
     */
    private void cropListInit(){
        if(cameraRequest.getCropType()!=CropPhotoType.CROP_FREE_RATIO){
            String text = cameraRequest.getDefaultCropWHratioString();
            crop_wh_ratio_text.setText(text);
            // 定比模式
            if(cameraRequest.getCropType()==CropPhotoType.CROP_FIXED_RATIO){
                int Idx = 0;
                for(String s:WH_RATIO_TEXT_COMM){
                    if(s.contains(text))break;
                    Idx++;
                }
                if(Idx<WH_RATIO_TEXT_COMM.length){
                    vlist_image = new int[]{WH_RATIO_IMAGE_COMM[Idx]};
                    vlist_text = new String[]{text};
                }
            } else if (cameraRequest.getCropType() == CropPhotoType.CROP_MULTI_RATIO) {// 多比例模式
                vlist_text = cameraRequest.getCrop_wh_options();
                int itemNum = vlist_text.length;
                vlist_image = new int[itemNum];
                int i = 0;
                while(i < itemNum){
                    int Idx = 0;
                    for(String s:WH_RATIO_TEXT_COMM){
                        if(s.contains(vlist_text[i]))break;
                        Idx++;
                    }
                    vlist_image[i] = WH_RATIO_IMAGE_COMM[Idx];
                    i++;
                }
            }
            crop_wh_adapter = new SimpleAdapter(this,getData(vlist_image,vlist_text),R.layout.crop_wh_ratio_vlist,
                    new String[] {"vlist_image","vlist_text"},new int[] {R.id.vlist_image,R.id.vlist_text});
        } else {// 自由比例模式
            crop_wh_ratio_text.setText("自由比例");
            crop_wh_ratio_text.setTextColor(Color.rgb(128, 128, 128));
            crop_wh_ratio_btn_ll.setClickable(false);
        }
    }
    /**
     * 存储照片
     */
    private void storePhoto() {
        progressDialog =  new ProgressDialog(CropImageActivity.this,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在保存,请稍候...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(){
            @Override
            public void run() {
                String filepath = viApplication.getViApp().getSavedPhotoDir() + "//" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
                File file = new File(filepath);
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    saved_photo = cropImageView.getCropImage(bitmapIntent.getBitmappath());
                    saved_photo.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    bos.flush();    // 刷新此缓冲区的输出流
                    bos.close();    // 关闭此输出流并释放与此流有关的所有系统资源
                    handler.sendEmptyMessage(0);
                    Thread.sleep(10);
                    // 跳转到其他的Activity
                    if (cameraRequest.backOriginClass()) {
                        Intent newint = new Intent(CropImageActivity.this, cameraRequest.getOriginClass());
                        newint.putExtra("imagepath", filepath);
                        setResult(RESULT_OK, newint);
                        finish();
                    } else if (cameraRequest.goDestClass()) {
                        Intent newint = new Intent(CropImageActivity.this, cameraRequest.getDestClass());
                        newint.putExtra("imagepath", filepath);
                        startActivity(newint);
                        finish();
                    }
                    if (saved_photo != null)//回收bitmap空间
                        saved_photo.recycle();
                    if (recievedPhoto != null)
                        recievedPhoto.recycle();
                    saved_photo = null;
                    recievedPhoto = null;
                    System.gc();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "文件没有找到", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "图片保存发生错误", Toast.LENGTH_SHORT).show();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /**
     * 上一步
     */
    private void backToCamera(){
        if(bitmapIntent.getSource() == BitmapIntent.FROM_CAMERA) {
            Intent intent = new Intent(CropImageActivity.this, viCameraActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("cameraRequest", cameraRequest);
            intent.putExtras(bundle);
            startActivity(intent);
            if(saved_photo!=null)//回收bitmap空间
                saved_photo.recycle();
            if(recievedPhoto!=null)
                recievedPhoto.recycle();
            saved_photo = null;
            recievedPhoto = null;
            System.gc();
            this.finish();
        } else if(bitmapIntent.getSource() == BitmapIntent.FROM_GALLERY){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            if(saved_photo!=null)//回收bitmap空间
                saved_photo.recycle();
            if(recievedPhoto!=null)
                recievedPhoto.recycle();
            saved_photo = null;
            recievedPhoto = null;
            System.gc();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE:
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                // 将光标移至开头
                cursor.moveToFirst();
                // 获得图片索引值
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 最后根据索引值获取图片路径
                String filepath = cursor.getString(index);// 判断图像大小是否超过最大值，超过则不加载
                if (BitmapUtil.getImageSizeBeforeLoad(filepath) > BitmapUtil.IMAGE_MAX_LOAD_SIZE) {
                    Toast.makeText(this, "图片尺寸过大!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int orientation = BitmapUtil.readPictureDegree(filepath);
                bitmapIntent.setBitmappath(filepath);
                bitmapIntent.setOrientation(orientation);
                bitmapIntent.setSource(BitmapIntent.FROM_GALLERY);
                // 显示被裁剪的图片的samplesize
                viSize load_size = BitmapUtil.getImageReq(bitmapIntent.getBitmappath(), showMaxWidth,
                        showMaxHeight);
                recievedPhoto = BitmapUtil.decodeSampledBitmapFromFile(bitmapIntent.getBitmappath(), load_size);
                // 裁剪宽高比初始化
                cropRatioInit();
        }
    }

    private android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    progressDialog.dismiss();
                    break;
            }
        }
    };
}
