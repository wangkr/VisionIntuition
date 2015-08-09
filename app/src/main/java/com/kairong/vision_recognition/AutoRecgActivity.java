package com.kairong.vision_recognition;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.kairong.viUIControls.viAlertDialog.viAlertDialog;
import com.kairong.viUIControls.viCamera.viCameraActivity;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.CameraUtil;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class AutoRecgActivity extends Activity{

    private viApplication app = null;
    public int ImageView_Width;
    public int ImageView_Height;
    private ImageView mImageView = null;
    private Bitmap bitmap = null;
    private String filepath = null;

    private final int CAMERA_IMAGE_CODE = 20152017;
    private final int GALLERY_IMAGE_CODE = 20152018;
    private String TAG = "AutoRecgActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_recognition_activity);

        // 初始化auto_recognition_activity.xml 的一些信息
        Resources r = getResources();
        int activity_horizontal_margin = (int)(r.getDimension(R.dimen.activity_horizontal_margin));
        int activity_vertical_margin = (int)(r.getDimension(R.dimen.activity_vertical_margin));
        int secondary_margin = (int)(r.getDimension(R.dimen.activity_secondary_margin));
        int select_image_layout_height = (int)(r.getDimension(R.dimen.at_select_image_btn_height));
        int at_image_layout_marginBottom = (int)(r.getDimension(R.dimen.at_image_layout_margin_bottom));
        app = (viApplication)getApplication();
        ImageView_Width = app.getScreenWidth() - 2*app.getActivity_horizontal_margin() - 2*secondary_margin;
        ImageView_Height = app.getScreenHeight() - 2*activity_vertical_margin - 2*secondary_margin - select_image_layout_height - at_image_layout_marginBottom;

        filepath = getIntent().getStringExtra("imagepath");
        mImageView = (ImageView)findViewById(R.id.auto_image_view);
//       findViewById(R.id.auto_image_layout).setOnLongClickListener(onLongClickListener_ImageView);


        bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath, ImageView_Width, ImageView_Height);
        mImageView.setImageBitmap(bitmap);
    }
    private View.OnLongClickListener onLongClickListener_ImageView = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Intent imgIntent = new Intent(AutoRecgActivity.this, viCameraActivity.class);
            imgIntent.putExtra("OriginTag", TAG);
            startActivityForResult(imgIntent, CAMERA_IMAGE_CODE);
            return false;
        }
    };

    public void onSelectImage(View view){
        // 初始化viAlertDialog
        final Dialog dialog = new viAlertDialog(AutoRecgActivity.this, R.style.viAlertDialog,"获取照片");
        dialog.show();
        ((viAlertDialog)dialog).init(AutoRecgActivity.this,CAMERA_IMAGE_CODE,GALLERY_IMAGE_CODE,TAG);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            Toast.makeText(AutoRecgActivity.this,"没有获取任何图片",Toast.LENGTH_SHORT).show();
            return;
        }
        String filepath;
        switch (requestCode) {
            case CAMERA_IMAGE_CODE:// 从相机添加图片
                filepath = data.getStringExtra("imagepath");
                bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath, ImageView_Width, ImageView_Height);
                mImageView.setImageBitmap(bitmap);
                break;
            case GALLERY_IMAGE_CODE:// 从相册获取图片
                filepath = CameraUtil.getImageFromSysGallery(AutoRecgActivity.this,data);
                // 判断图像大小是否超过最大值，超过则不加载
                if (BitmapUtil.getImageSizeBeforeLoad(filepath) > BitmapUtil.IMAGE_MAX_LOAD_SIZE) {
                    Toast.makeText(this, "图片尺寸过大!", Toast.LENGTH_SHORT).show();
                    return;
                }
                bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath, ImageView_Width, ImageView_Height);
                mImageView.setImageBitmap(bitmap);
                break;
        }
    }
    @Override
    public void finish() {
        if(bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        super.finish();
    }
}
