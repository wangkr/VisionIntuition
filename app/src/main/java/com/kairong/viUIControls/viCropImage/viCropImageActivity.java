package com.kairong.viUIControls.viCropImage;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.kairong.vision_recognition.R;

/**
 * Created by Kairong on 2015/7/13 at USTC
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class viCropImageActivity extends Activity {
    private CropImageView cropImageView = null;
    private String imagePath = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_activity);

        imagePath = getIntent().getStringExtra("imagepath");
        cropImageView = (CropImageView)findViewById(R.id.crop_image_view);

        //cropImageView.setDrawable(Drawable.createFromPath(imagePath));

    }
}
