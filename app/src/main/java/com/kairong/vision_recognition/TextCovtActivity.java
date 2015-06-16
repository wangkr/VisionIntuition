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
import com.kairong.viUtils.DisplayUtil;

/**
 * Created by Kairong on 2015/6/3.
 */
public class TextCovtActivity extends Activity {
    public int ImageView_Width;
    public int ImageView_Height;
    private ImageView mImageView = null;
    private Bitmap bitmap = null;
    private String filepath = null;

    private final int CAMERA_IMAGE_CODE = 20152017;
    private final int GALLERY_IMAGE_CODE = 20152018;
    private String TAG = "TextCovtActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_conversion_activity);

        // 初始化age_test_activity.xml 的一些信息
        Resources r = getResources();
        int activity_horizontal_margin = (int)(r.getDimension(R.dimen.activity_horizontal_margin));
        int secondary_margin = (int)(r.getDimension(R.dimen.activity_secondary_margin));
        ImageView_Width = DisplayUtil.screenWidth - 2*activity_horizontal_margin - 2*secondary_margin;
        ImageView_Height = (int)(r.getDimension(R.dimen.tc_imageview_height)) - 2*secondary_margin;

        mImageView = (ImageView)findViewById(R.id.text_conversion_image);
        findViewById(R.id.text_conversion_layout).setOnClickListener(onClickListener_ImageView);
    }
    private View.OnClickListener onClickListener_ImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 初始化viAlertDialog
            final Dialog dialog = new viAlertDialog(TextCovtActivity.this, R.style.viAlertDialog,"获取照片");
            dialog.show();
            dialog.findViewById(R.id.btn_aldl_camera).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imgIntent = new Intent(TextCovtActivity.this, viCameraActivity.class);
                    imgIntent.putExtra("SrcTag", TAG);
                    startActivityForResult(imgIntent, CAMERA_IMAGE_CODE);
                    dialog.cancel();
                }
            });
            dialog.findViewById(R.id.btn_aldl_gallery).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");//相片类型
                    startActivityForResult(intent, GALLERY_IMAGE_CODE);
                    dialog.cancel();
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            Toast.makeText(TextCovtActivity.this,"没有获取任何图片",Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case CAMERA_IMAGE_CODE:// 从相机添加图片
                String filepath = data.getStringExtra("imagepath");
                bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath, ImageView_Width, ImageView_Height);
                mImageView.setImageBitmap(bitmap);
                break;
            case GALLERY_IMAGE_CODE:// 从相册获取图片
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                // 获得图片索引值
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String filepath2 = cursor.getString(index);
                // 判断图像大小是否超过最大值，超过则不加载
                if (BitmapUtil.getImageSizeBeforeLoad(filepath2) > BitmapUtil.IMAGE_MAX_LOAD_SIZE) {
                    Toast.makeText(this, "图片尺寸过大!", Toast.LENGTH_SHORT).show();
                    return;
                }
                bitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath2, ImageView_Width, ImageView_Height);
                mImageView.setImageBitmap(bitmap);
                break;
        }
    }
}
