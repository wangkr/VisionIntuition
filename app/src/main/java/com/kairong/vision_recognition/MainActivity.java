package com.kairong.vision_recognition;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kairong.viCamera.viCameraActivity;
import com.kairong.myPathButton.myAnimations;
import com.kairong.viUtils.BitmapUtil;


public class MainActivity extends Activity {
    private ImageView btn_camera = null;
    private ImageView btn_gallery = null;
    private RelativeLayout composerButtonWrapper = null;
    private ImageView composerButtonShowHideButtonIcon = null;
    private ImageView composerButtonShowHideButton = null;
    private ImageView btn_face_match = null;
    private ImageView btn_age_test = null;
    private ImageView btn_text_conversion = null;
    private long exitTime = 0;
    private static final int CAMERA_WITH_DATA = 3021;
    private static final int REQUEST_CODE_PICK_IMAGE = 3022;
    private myAnimations myanimation = null;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        btn_camera = (ImageView)findViewById(R.id.btn_camera);
        btn_gallery = (ImageView)findViewById(R.id.btn_gallery);
        btn_face_match = (ImageView)findViewById(R.id.btn_composer_face_match);
        btn_age_test = (ImageView)findViewById(R.id.btn_composer_age_test);
        btn_text_conversion = (ImageView)findViewById(R.id.btn_composer_text_conversion);
        composerButtonShowHideButton = (ImageView)findViewById(R.id.btn_composer_button);
        composerButtonShowHideButtonIcon = (ImageView)findViewById(R.id.btn_composer_button_icon);
        composerButtonWrapper = (RelativeLayout)findViewById(R.id.composer_wrapper_layout);

        btn_face_match.setOnClickListener(cpBtnListener);
        btn_age_test.setOnClickListener(cpBtnListener);
        btn_text_conversion.setOnClickListener(cpBtnListener);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,viCameraActivity.class);
                intent.putExtra("DestTag","AutoRecgActivity");
                startActivity(intent);
            }
        });
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });
        myanimation = new myAnimations(composerButtonWrapper,myAnimations.CENTERTOP,300);
        composerButtonShowHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myanimation.isOpen()){
                    myanimation.startAnimationsOut(500);
                    composerButtonShowHideButtonIcon.startAnimation(myAnimations.getRotateAnimation(-45, 0, 500));
                }
                else{
                    myanimation.startAnimationsIn(500);
                    composerButtonShowHideButtonIcon.startAnimation(myAnimations.getRotateAnimation(0, -45, 500));
                }
            }
        });
        Log.d("MainActivity", "OnCreate success!");
    }

    private View.OnClickListener cpBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btn_composer_face_match:
                    Intent intent = new Intent(MainActivity.this,FaceMatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_composer_age_test:
                    Intent intent2 = new Intent(MainActivity.this,AgeTestActivity.class);
                    startActivity(intent2);
                    break;
                case R.id.btn_composer_text_conversion:
                    Intent intent3 = new Intent(MainActivity.this,TextCovtActivity.class);
                    startActivity(intent3);
                    break;
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        if(resultCode!=RESULT_OK)
            return;
        switch (requestCode)
        {
            case REQUEST_CODE_PICK_IMAGE:
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                // 获得图片索引值
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String filepath = cursor.getString(index);
                // 判断图像大小是否超过最大值，超过则不加载
                if (BitmapUtil.getImageSizeBeforeLoad(filepath) > BitmapUtil.IMAGE_MAX_LOAD_SIZE) {
                    Toast.makeText(this, "图片尺寸过大!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent newint = new Intent(MainActivity.this,AutoRecgActivity.class);
                newint.putExtra("imagepath",filepath);
                startActivity(newint);
                break;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if((System.currentTimeMillis() - exitTime)>2000){
            // 如果“扩展”按钮是开状态，则首先关闭“扩展”按钮
            if(myanimation.isOpen()){
                myanimation.startAnimationsOut(500);
                composerButtonShowHideButtonIcon.startAnimation(myAnimations.getRotateAnimation(-45, 0, 500));
                return;
            }
            Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        }else{
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
