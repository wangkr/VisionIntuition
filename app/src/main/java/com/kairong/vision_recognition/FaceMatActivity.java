package com.kairong.vision_recognition;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.viAlertDialog.viHintDialog;
import com.kairong.viCamera.viCameraActivity;
import com.kairong.circleProgress.CircleProgress;
import com.kairong.horizonListView.HorizontalListView;
import com.kairong.horizonListView.HorizontalListViewAdapter;
import com.kairong.viAlertDialog.viAlertDialog;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.DisplayUtil;
import com.kairong.viUtils.viSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Kairong on 2015/6/3.
 */
public class FaceMatActivity extends Activity implements viHintDialog.IDialogOnclickInterface{
    public int faceImageViewHeight;
    public int faceImageViewWidth;

    private ImageView leftFaceView = null;
    private ImageView rightFaceView = null;
    private TextView faceSimilarity = null;
    private TextView dialogTextContent = null;
    private CircleProgress circleProgress = null;
    private HorizontalListView hListView = null;
    private HorizontalListViewAdapter hListViewAdapter = null;
    private viHintDialog viHintDialog = null;

    private List<String> imagePathsList = null;
    private Vector<Integer>  imageStatus = null;
    private final int CAMERA_IMAGE_CODE = 20152017;
    private final int GALLERY_IMAGE_CODE = 20152018;
    private final int CAMERA_REPLACE_IMAGE_CODE = 20152019;
    private final int GALLERY_REPLACE_IMAGE_CODE = 20152020;
    private final int CAMERA_TOUCH_IMAGE_CODE = 20152021;
    private final int GALLERY_TOUCH_IMAGE_CODE = 20152022;
    private final int LEFT_FACE_IMAGE_VIEW = 1;
    private final int RIGHT_FACE_IMAGE_VIEW = 2;
    private final int NONE_FACE_IMAGE = -1;
    private int longClickPosition = -1;
    private int touchClickPosition = -1;
    private boolean hasLeftFaceImage = false;
    private boolean hasRightFaceImage = false;
    private final String TAG = "FaceMatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_match_activity);

        // 初始化face_match_activity.xml 的一些信息
        Resources r = getResources();
        int activity_horizontal_margin = (int)(r.getDimension(R.dimen.activity_horizontal_margin));
        int activity_tertiary_margin = (int)(r.getDimension(R.dimen.activity_tertiary_margin));
        faceImageViewWidth = (int)((DisplayUtil.screenWidth - activity_horizontal_margin*2 -
                activity_tertiary_margin*4 - DisplayUtil.dip2px(4))/2);
        faceImageViewHeight = (int)(r.getDimension(R.dimen.faceimage_parent_layout_height)) - 2*activity_tertiary_margin;
        // 初始化控件id
        leftFaceView = (ImageView)findViewById(R.id.left_face_image);
        rightFaceView = (ImageView)findViewById(R.id.right_face_image);
        faceSimilarity = (TextView)findViewById(R.id.face_similarity_text);
        dialogTextContent = (TextView)findViewById(R.id.text_face_match_dialog);
        circleProgress = (CircleProgress)findViewById(R.id.face_match_image_progress);
        hListView = (HorizontalListView)findViewById(R.id.horizon_listView);

        viHintDialog = new viHintDialog(this,R.style.viHintDialog);
        imagePathsList = new ArrayList<String>();
        imageStatus = new Vector<Integer>();
        hListViewAdapter = new HorizontalListViewAdapter(getApplicationContext(),imagePathsList,imageStatus,R.drawable.hlistview_add_btn);
        hListView.setAdapter(hListViewAdapter);
        hListView.setOnItemClickListener(onItemClickListener_hList);
        hListView.setOnItemLongClickListener(onItemLongClickListener_hList);
        findViewById(R.id.left_face_image_layout).setOnClickListener(onClickListener_ImageView);
        findViewById(R.id.right_face_image_layout).setOnClickListener(onClickListener_ImageView);
    }

    private View.OnClickListener onClickListener_ImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String dlTitleString = "";
            // 获取点击的view位置
            if(v.getId() == R.id.left_face_image_layout) {
                touchClickPosition = LEFT_FACE_IMAGE_VIEW;
                if(hasLeftFaceImage){ // 根据图片内容有无设置dialog标题
                    dlTitleString = "替换图片";
                }else{
                    dlTitleString = "获取照片";
                }
            }else{
                touchClickPosition = RIGHT_FACE_IMAGE_VIEW;
                if(hasRightFaceImage){// 根据图片内容有无设置dialog标题
                    dlTitleString = "替换图片";
                }else{
                    dlTitleString = "获取照片";
                }
            }

            // 初始化viAlertDialog
            final Dialog dialog = new viAlertDialog(FaceMatActivity.this, R.style.viAlertDialog,dlTitleString);
            dialog.show();
            dialog.findViewById(R.id.btn_aldl_camera).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imgIntent = new Intent(FaceMatActivity.this, viCameraActivity.class);
                    imgIntent.putExtra("SrcTag", TAG);
                    startActivityForResult(imgIntent, CAMERA_TOUCH_IMAGE_CODE);
                    dialog.cancel();
                }
            });
            dialog.findViewById(R.id.btn_aldl_gallery).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");//相片类型
                    startActivityForResult(intent, GALLERY_TOUCH_IMAGE_CODE);
                    dialog.cancel();
                }
            });
        }
    };

    private AdapterView.OnItemLongClickListener onItemLongClickListener_hList = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (imagePathsList.size() > 0) {
                int[] location = new int[2];
                // 获取当前view在当前屏幕的绝对坐标位置
                // location[0]表示view的x坐标值,location[1]表示view的y坐标值
                view.getLocationOnScreen(location);
                longClickPosition = position;
                DisplayMetrics displayMetrics = new DisplayMetrics();
                Display display = FaceMatActivity.this.getWindowManager().getDefaultDisplay();
                display.getMetrics(displayMetrics);
                WindowManager.LayoutParams layoutParams = viHintDialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.BOTTOM|Gravity.LEFT;
                layoutParams.y = display.getHeight() - location[1];
                layoutParams.x = location[0];
                viHintDialog.getWindow().setAttributes(layoutParams);
                viHintDialog.setCanceledOnTouchOutside(true);
                viHintDialog.show();
                return false;
            }
            return false;
        }
    };
    private AdapterView.OnItemClickListener onItemClickListener_hList = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 只有点击add icon图片时，才添加图片
            if (position == imagePathsList.size()) {
                // 初始化viAlertDialog
                final Dialog dialog = new viAlertDialog(FaceMatActivity.this, R.style.viAlertDialog,"获取照片");
                dialog.show();
                dialog.findViewById(R.id.btn_aldl_camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imgIntent = new Intent(FaceMatActivity.this, viCameraActivity.class);
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
            }else if(position < imagePathsList.size()){
                if(imageStatus.get(position)!=NONE_FACE_IMAGE){
                    // 设置背景透明
                    view.setBackgroundColor(Color.TRANSPARENT);
                    eraseFaceImageView(imageStatus.get(position));
                    imageStatus.set(position,NONE_FACE_IMAGE);
                }else if(!hasLeftFaceImage){// 如果没有左边的头像，则显示
                    imageStatus.set(position,LEFT_FACE_IMAGE_VIEW);
                    // 设置背景为left faceimage背景色
                    view.setBackgroundColor(getResources().getColor(R.color.hList_left_face_item_bk_color));
                    setFaceImage(imagePathsList.get(position), LEFT_FACE_IMAGE_VIEW);
                    hasLeftFaceImage = true;
                }else if(!hasRightFaceImage){
                    imageStatus.set(position,RIGHT_FACE_IMAGE_VIEW);
                    // 设置背景为right faceimage背景色
                    view.setBackgroundColor(getResources().getColor(R.color.hList_right_face_item_bk_color));
                    setFaceImage(imagePathsList.get(position), RIGHT_FACE_IMAGE_VIEW);
                    hasRightFaceImage = true;
                }

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            Toast.makeText(FaceMatActivity.this,"没有获取任何图片",Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode){
            case CAMERA_IMAGE_CODE:// 从相机添加图片
                String filepath = data.getStringExtra("imagepath");
                imagePathsList.add(filepath);
                imageStatus.add(NONE_FACE_IMAGE);
                hListViewAdapter.notifyDataSetChanged();
                break;
            case GALLERY_IMAGE_CODE:// 从相册获取图片
                Uri uri = data.getData();
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri,proj,null,null,null);
                // 获得图片索引值
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String filepath2 = cursor.getString(index);
                // 判断图像大小是否超过最大值，超过则不加载
                if(BitmapUtil.getImageSizeBeforeLoad(filepath2)>BitmapUtil.IMAGE_MAX_LOAD_SIZE){
                    Toast.makeText(this,"图片尺寸过大!",Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePathsList.add(filepath2);
                imageStatus.add(NONE_FACE_IMAGE);
                hListViewAdapter.notifyDataSetChanged();
                break;
            case CAMERA_REPLACE_IMAGE_CODE:// 从相机获取替换的图片
                String filepath3 = data.getStringExtra("imagepath");
                imagePathsList.set(longClickPosition,filepath3);
                hListViewAdapter.notifyDataSetChanged();
                setFaceImage(filepath3,imageStatus.get(longClickPosition));
                break;
            case GALLERY_REPLACE_IMAGE_CODE:// 从相册获取替换的图片
                Uri uri1 = data.getData();
                String[] proj1 = {MediaStore.Images.Media.DATA};
                Cursor cursor1 = managedQuery(uri1,proj1,null,null,null);
                // 获得图片索引值
                int index1 = cursor1.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头
                cursor1.moveToFirst();
                // 最后根据索引值获取图片路径
                String filepath4 = cursor1.getString(index1);
                // 判断图像大小是否超过最大值，超过则不加载
                if(BitmapUtil.getImageSizeBeforeLoad(filepath4)>BitmapUtil.IMAGE_MAX_LOAD_SIZE){
                    Toast.makeText(this,"图片尺寸过大",Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePathsList.set(longClickPosition,filepath4);
                hListViewAdapter.notifyDataSetChanged();
                setFaceImage(filepath4, imageStatus.get(longClickPosition));
                break;
            case CAMERA_TOUCH_IMAGE_CODE: // 触摸添加/替换头像图片：相机方式
                String filepath5 = data.getStringExtra("imagepath");
                // 如果face image view上之前有图片，则替换图片，否则添加。
                if(touchClickPosition == LEFT_FACE_IMAGE_VIEW) {
                    if(hasLeftFaceImage) {
                        imagePathsList.set(imageStatus.indexOf(LEFT_FACE_IMAGE_VIEW), filepath5);
                    }else{
                        imagePathsList.add(filepath5);
                        imageStatus.add(touchClickPosition);
                        hasLeftFaceImage = true;
                    }
                }else if(touchClickPosition == RIGHT_FACE_IMAGE_VIEW){
                    if(hasRightFaceImage){
                        imagePathsList.set(imageStatus.indexOf(RIGHT_FACE_IMAGE_VIEW), filepath5);
                    }else{
                        imagePathsList.add(filepath5);
                        imageStatus.add(touchClickPosition);
                        hasRightFaceImage = true;
                    }
                }
                setFaceImage(filepath5,touchClickPosition);
                hListViewAdapter.notifyDataSetChanged();
                break;
            case GALLERY_TOUCH_IMAGE_CODE:// 触摸添加/替换头像图片：相册方式
                Uri uri2 = data.getData();
                String[] proj2 = {MediaStore.Images.Media.DATA};
                Cursor cursor2 = managedQuery(uri2,proj2,null,null,null);
                // 获得图片索引值
                int index2 = cursor2.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头
                cursor2.moveToFirst();
                // 最后根据索引值获取图片路径
                String filepath6 = cursor2.getString(index2);
                // 判断图像大小是否超过最大值，超过则不加载
                if(BitmapUtil.getImageSizeBeforeLoad(filepath6)>BitmapUtil.IMAGE_MAX_LOAD_SIZE){
                    Toast.makeText(this,"图片尺寸过大",Toast.LENGTH_SHORT).show();
                    touchClickPosition = NONE_FACE_IMAGE;
                    return;
                }
                // 如果face image view上之前有图片，则替换图片，否则添加。
                if(touchClickPosition == LEFT_FACE_IMAGE_VIEW) {
                    if(hasLeftFaceImage) {
                        imagePathsList.set(imageStatus.indexOf(LEFT_FACE_IMAGE_VIEW), filepath6);
                    }else{
                        imagePathsList.add(filepath6);
                        imageStatus.add(touchClickPosition);
                        hasLeftFaceImage = true;
                    }
                }else if(touchClickPosition == RIGHT_FACE_IMAGE_VIEW){
                    if(hasRightFaceImage){
                        imagePathsList.set(imageStatus.indexOf(RIGHT_FACE_IMAGE_VIEW), filepath6);
                    }else{
                        imagePathsList.add(filepath6);
                        imageStatus.add(touchClickPosition);
                        hasRightFaceImage = true;
                    }
                }
                setFaceImage(filepath6,touchClickPosition);
                hListViewAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void leftOnclick() {
        final Dialog dialog = new viAlertDialog(FaceMatActivity.this,R.style.viAlertDialog,"替换照片");
        dialog.show();
        dialog.findViewById(R.id.btn_aldl_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgIntent = new Intent(FaceMatActivity.this, viCameraActivity.class);
                imgIntent.putExtra("SrcTag", TAG);
                startActivityForResult(imgIntent, CAMERA_REPLACE_IMAGE_CODE);
                dialog.cancel();
            }
        });
        dialog.findViewById(R.id.btn_aldl_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");//相片类型
                startActivityForResult(intent, GALLERY_REPLACE_IMAGE_CODE);
                dialog.cancel();
            }
        });
        viHintDialog.cancel();
    }

    @Override
    public void rightOnclick() {
        if(longClickPosition < imagePathsList.size()){
            eraseFaceImageView(imageStatus.get(longClickPosition));
            imagePathsList.remove(longClickPosition);
            imageStatus.remove(longClickPosition);
            hListViewAdapter.notifyDataSetChanged();
            viHintDialog.cancel();
        }
    }

    /**
     * 设置人脸图片View
     * @param filepath：图片路径
     * @param showPosition：显示位置
     */
    private void setFaceImage(String filepath,int showPosition){
        if(showPosition == NONE_FACE_IMAGE)
            return;
        Bitmap facebitmap = BitmapUtil.decodeSampledBitmapFromFile(filepath,faceImageViewWidth,faceImageViewHeight);
        switch (showPosition){
            case LEFT_FACE_IMAGE_VIEW:
                leftFaceView.setImageBitmap(facebitmap);
                break;
            case RIGHT_FACE_IMAGE_VIEW:
                rightFaceView.setImageBitmap(facebitmap);
                break;
        }

    }

    /**
     * 擦除显示的人脸图片View
     * @param showPosition：显示位置
     */
    private void eraseFaceImageView(int showPosition){
        switch (showPosition){
            case LEFT_FACE_IMAGE_VIEW:
                leftFaceView.setImageBitmap(null);
                hasLeftFaceImage = false;
                break;
            case RIGHT_FACE_IMAGE_VIEW:
                rightFaceView.setImageBitmap(null);
                hasRightFaceImage = false;
                break;
        }

    }
}
