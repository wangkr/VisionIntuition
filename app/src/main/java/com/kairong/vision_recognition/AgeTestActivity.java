package com.kairong.vision_recognition;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.kairong.viAlertDialog.viAlertDialog;
import com.kairong.viCamera.viCameraActivity;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.CameraUtil;
import com.kairong.viUtils.DisplayUtil;

/**
 * Created by Kairong on 2015/6/3.
 */
public class AgeTestActivity extends Activity {
    public int ImageView_Width;
    public int ImageView_Height;
    private ImageView mImageView = null;
    private Bitmap bitmap = null;
    private String filepath = null;

    private final int CAMERA_IMAGE_CODE = 20152017;
    private final int GALLERY_IMAGE_CODE = 20152018;
    private String TAG = "AgeTestActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.age_test_activity);

        // 初始化age_test_activity.xml 的一些信息
        Resources r = getResources();
        int activity_horizontal_margin = (int)(r.getDimension(R.dimen.activity_horizontal_margin));
        int secondary_margin = (int)(r.getDimension(R.dimen.activity_secondary_margin));
        ImageView_Width = DisplayUtil.screenWidth - 2*activity_horizontal_margin - 2*secondary_margin;
        ImageView_Height = (int)(r.getDimension(R.dimen.at_imageview_height)) - 2*secondary_margin;

        mImageView = (ImageView)findViewById(R.id.age_test_face_image);
        findViewById(R.id.age_test_image_layout).setOnLongClickListener(onLongClickListener_ImageView);
    }
    // 长按打开相机
    private View.OnLongClickListener onLongClickListener_ImageView = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Intent imgIntent = new Intent(AgeTestActivity.this, viCameraActivity.class);
            imgIntent.putExtra("SrcTag", TAG);
            startActivityForResult(imgIntent, CAMERA_IMAGE_CODE);
            return false;
        }
    };

    public void onSelectImage(View view){
        // 初始化viAlertDialog
        final Dialog dialog = new viAlertDialog(AgeTestActivity.this, R.style.viAlertDialog,"获取照片");
        dialog.show();
        ((viAlertDialog)dialog).init(AgeTestActivity.this,CAMERA_IMAGE_CODE,GALLERY_IMAGE_CODE,TAG);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            Toast.makeText(AgeTestActivity.this,"没有获取任何图片",Toast.LENGTH_SHORT).show();
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
                filepath = CameraUtil.getImageFromSysGallery(AgeTestActivity.this,data);
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
