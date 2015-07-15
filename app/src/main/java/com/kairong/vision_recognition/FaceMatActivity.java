package com.kairong.vision_recognition;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kairong.viUIControls.viAlertDialog.viHintDialog;
import com.kairong.viUIControls.viCamera.viCameraActivity;
import com.kairong.viUIControls.circleProgress.CircleProgress;
import com.kairong.viUIControls.horizonListView.HorizontalListView;
import com.kairong.viUIControls.horizonListView.HorizontalListViewAdapter;
import com.kairong.viUIControls.viAlertDialog.viAlertDialog;
import com.kairong.viUtils.BitmapUtil;
import com.kairong.viUtils.CameraUtil;
import com.kairong.viUtils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Kairong on 2015/6/3.
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class FaceMatActivity extends Activity implements viHintDialog.IDialogOnclickInterface{
    private viApplication app = null;

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
    private Bitmap leftImage = null;
    private Bitmap rightImage = null;
    private final String TAG = "FaceMatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_match_activity);
        app = (viApplication)getApplication();
        // 初始化face_match_activity.xml 的一些信息
        faceImageViewWidth = (app.getScreenWidth() - app.getActivity_horizontal_margin()*2 -
                app.getTertiary_margin()*4 - DisplayUtil.dip2px(4))/2;
        faceImageViewHeight = (int)(getResources().getDimension(R.dimen.faceimage_parent_layout_height)) - 2*app.getTertiary_margin();
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
        hListViewAdapter = new HorizontalListViewAdapter(app,getApplicationContext(),imagePathsList,imageStatus,R.drawable.hlistview_add_btn);
        hListView.setAdapter(hListViewAdapter);
        hListView.setOnItemClickListener(onItemClickListener_hList);
        hListView.setOnItemLongClickListener(onItemLongClickListener_hList);
        findViewById(R.id.left_face_image_layout).setOnLongClickListener(onLongClickListener_LayoutView);
        findViewById(R.id.right_face_image_layout).setOnLongClickListener(onLongClickListener_LayoutView);
    }

    private View.OnLongClickListener onLongClickListener_LayoutView = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            onLongClickToOpenCam(v);
            return false;
        }
    };

    private AdapterView.OnItemLongClickListener onItemLongClickListener_hList = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (imagePathsList.size() != position) {
                // 获取当前view在当前屏幕的绝对坐标位置
                // location[0]表示view的x坐标值,location[1]表示view的y坐标值
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                longClickPosition = position;
                DisplayMetrics displayMetrics = new DisplayMetrics();
                Display display = FaceMatActivity.this.getWindowManager().getDefaultDisplay();
                display.getMetrics(displayMetrics);
                WindowManager.LayoutParams layoutParams = viHintDialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.BOTTOM|Gravity.START;
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
                ((viAlertDialog)dialog).init(FaceMatActivity.this, CAMERA_IMAGE_CODE, GALLERY_IMAGE_CODE, TAG);
            }else if(position < imagePathsList.size()){
                updatehListView(position,view);
            }
        }
    };

    private void onLongClickToOpenCam(View view){
        switch (view.getId()){
            case R.id.left_face_image_layout:
                touchClickPosition = LEFT_FACE_IMAGE_VIEW;
                break;
            case R.id.right_face_image_layout:
                touchClickPosition = RIGHT_FACE_IMAGE_VIEW;
                break;
        }
        Intent imgIntent = new Intent(FaceMatActivity.this, viCameraActivity.class);
        imgIntent.putExtra("OriginTag", TAG);
        startActivityForResult(imgIntent, CAMERA_TOUCH_IMAGE_CODE);
    }
    public void onSelectLImage(View view){
        String dlTitleString = "";
        touchClickPosition = LEFT_FACE_IMAGE_VIEW;
        if(hasLeftFaceImage){ dlTitleString = "替换图片";}
        else{ dlTitleString = "获取照片";}
        // 初始化viAlertDialog
        final Dialog dialog = new viAlertDialog(FaceMatActivity.this, R.style.viAlertDialog,dlTitleString);
        dialog.show();
        ((viAlertDialog)dialog).init(FaceMatActivity.this,CAMERA_TOUCH_IMAGE_CODE,GALLERY_TOUCH_IMAGE_CODE,TAG);
    }
    public void onSelectRImage(View view){
        String dlTitleString = "";
        touchClickPosition = RIGHT_FACE_IMAGE_VIEW;
        if(hasRightFaceImage){ dlTitleString = "替换图片";}
        else{ dlTitleString = "获取照片";}
        // 初始化viAlertDialog
        final Dialog dialog = new viAlertDialog(FaceMatActivity.this, R.style.viAlertDialog,dlTitleString);
        dialog.show();
        ((viAlertDialog)dialog).init(FaceMatActivity.this,CAMERA_TOUCH_IMAGE_CODE,GALLERY_TOUCH_IMAGE_CODE,TAG);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK){
            Toast.makeText(FaceMatActivity.this,"没有获取任何图片",Toast.LENGTH_SHORT).show();
            return;
        }else{
            updateFaceImageView(requestCode,data);
        }

    }

    @Override
    public void leftOnclick() {
        final Dialog dialog = new viAlertDialog(FaceMatActivity.this,R.style.viAlertDialog,"替换照片");
        dialog.show();
        ((viAlertDialog)dialog).init(FaceMatActivity.this, CAMERA_REPLACE_IMAGE_CODE, GALLERY_REPLACE_IMAGE_CODE, TAG);
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
     * 更新horizontal List View 图片
     * @param position
     * @param view
     */
    public void updatehListView(int position, View view) {
        if (imageStatus.get(position) != NONE_FACE_IMAGE) {
            // 设置背景透明
            view.setBackgroundColor(Color.TRANSPARENT);
            eraseFaceImageView(imageStatus.get(position));
            imageStatus.set(position, NONE_FACE_IMAGE);
        } else if (!hasLeftFaceImage) {// 如果没有左边的头像，则显示
            imageStatus.set(position, LEFT_FACE_IMAGE_VIEW);
            // 设置背景为left faceimage背景色
            view.setBackgroundColor(getResources().getColor(R.color.hList_left_face_item_bk_color));
            setFaceImageView(imagePathsList.get(position), LEFT_FACE_IMAGE_VIEW);
            hasLeftFaceImage = true;
        } else if (!hasRightFaceImage) {
            imageStatus.set(position, RIGHT_FACE_IMAGE_VIEW);
            // 设置背景为right faceimage背景色
            view.setBackgroundColor(getResources().getColor(R.color.hList_right_face_item_bk_color));
            setFaceImageView(imagePathsList.get(position), RIGHT_FACE_IMAGE_VIEW);
            hasRightFaceImage = true;
        }
    }

    /**
     * 更新Face Image View图片
     * @param requestCode 
     * @param data
     */
    private void updateFaceImageView(int requestCode,Intent data){
        String filepath = null;
        switch (requestCode){
            case CAMERA_IMAGE_CODE:// 从相机添加图片
                filepath = data.getStringExtra("imagepath");
                imagePathsList.add(filepath);
                imageStatus.add(NONE_FACE_IMAGE);
                hListViewAdapter.notifyDataSetChanged();
                break;
            case GALLERY_IMAGE_CODE:// 从相册获取图片
                filepath = CameraUtil.getImageFromSysGallery(this,data);
                // 判断图像大小是否超过最大值，超过则不加载
                if(BitmapUtil.getImageSizeBeforeLoad(filepath)>BitmapUtil.IMAGE_MAX_LOAD_SIZE){
                    Toast.makeText(this,"图片尺寸过大!",Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePathsList.add(filepath);
                imageStatus.add(NONE_FACE_IMAGE);
                hListViewAdapter.notifyDataSetChanged();
                break;
            case CAMERA_REPLACE_IMAGE_CODE:// 从相机获取替换的图片
                filepath = data.getStringExtra("imagepath");
                imagePathsList.set(longClickPosition,filepath);
                hListViewAdapter.notifyDataSetChanged();
                setFaceImageView(filepath,imageStatus.get(longClickPosition));
                break;
            case GALLERY_REPLACE_IMAGE_CODE:// 从相册获取替换的图片
                filepath = CameraUtil.getImageFromSysGallery(this,data);
                // 判断图像大小是否超过最大值，超过则不加载
                if(BitmapUtil.getImageSizeBeforeLoad(filepath)>BitmapUtil.IMAGE_MAX_LOAD_SIZE){
                    Toast.makeText(this,"图片尺寸过大",Toast.LENGTH_SHORT).show();
                    return;
                }
                imagePathsList.set(longClickPosition,filepath);
                hListViewAdapter.notifyDataSetChanged();
                setFaceImageView(filepath, imageStatus.get(longClickPosition));
                break;
            case CAMERA_TOUCH_IMAGE_CODE: // 触摸"添加/替换"头像图片：相机方式
                filepath = data.getStringExtra("imagepath");
                // 如果face image view上之前有图片，则替换图片，否则添加。
                if(touchClickPosition == LEFT_FACE_IMAGE_VIEW) {
                    if(hasLeftFaceImage) {
                        imagePathsList.set(imageStatus.indexOf(LEFT_FACE_IMAGE_VIEW), filepath);
                    }else{
                        imagePathsList.add(filepath);
                        imageStatus.add(touchClickPosition);
                        hasLeftFaceImage = true;
                    }
                }else if(touchClickPosition == RIGHT_FACE_IMAGE_VIEW){
                    if(hasRightFaceImage){
                        imagePathsList.set(imageStatus.indexOf(RIGHT_FACE_IMAGE_VIEW), filepath);
                    }else{
                        imagePathsList.add(filepath);
                        imageStatus.add(touchClickPosition);
                        hasRightFaceImage = true;
                    }
                }
                setFaceImageView(filepath,touchClickPosition);
                hListViewAdapter.notifyDataSetChanged();
                break;
            case GALLERY_TOUCH_IMAGE_CODE:// 触摸"添加/替换"头像图片：相册方式
                filepath = CameraUtil.getImageFromSysGallery(this,data);
                // 判断图像大小是否超过最大值，超过则不加载
                if(BitmapUtil.getImageSizeBeforeLoad(filepath)>BitmapUtil.IMAGE_MAX_LOAD_SIZE){
                    Toast.makeText(this,"图片尺寸过大",Toast.LENGTH_SHORT).show();
                    touchClickPosition = NONE_FACE_IMAGE;
                    return;
                }
                // 如果face image view上之前有图片，则替换图片，否则添加。
                if(touchClickPosition == LEFT_FACE_IMAGE_VIEW) {
                    if(hasLeftFaceImage) {
                        imagePathsList.set(imageStatus.indexOf(LEFT_FACE_IMAGE_VIEW), filepath);
                    }else{
                        imagePathsList.add(filepath);
                        imageStatus.add(touchClickPosition);
                        hasLeftFaceImage = true;
                    }
                }else if(touchClickPosition == RIGHT_FACE_IMAGE_VIEW){
                    if(hasRightFaceImage){
                        imagePathsList.set(imageStatus.indexOf(RIGHT_FACE_IMAGE_VIEW), filepath);
                    }else{
                        imagePathsList.add(filepath);
                        imageStatus.add(touchClickPosition);
                        hasRightFaceImage = true;
                    }
                }
                setFaceImageView(filepath,touchClickPosition);
                hListViewAdapter.notifyDataSetChanged();
                break;
        }
    }
    /**
     * 设置人脸图片View
     * @param filepath：图片路径
     * @param showPosition：显示位置
     */
    private void setFaceImageView(String filepath,int showPosition){
        if(showPosition == NONE_FACE_IMAGE)
            return;
        switch (showPosition){
            case LEFT_FACE_IMAGE_VIEW:
                // 分配内存前回收内存
                if(leftImage!=null&&!leftImage.isRecycled()){
                    leftImage.recycle();
                    leftImage = null;
                }
                leftImage = BitmapUtil.decodeSampledBitmapFromFile(filepath,faceImageViewWidth,faceImageViewHeight);
                leftFaceView.setImageBitmap(leftImage);
                leftFaceView.setVisibility(View.VISIBLE);
                break;
            case RIGHT_FACE_IMAGE_VIEW:
                // 分配内存前回收内存
                if(rightImage!=null&&!rightImage.isRecycled()){
                    rightImage.recycle();
                    rightImage = null;
                }
                rightImage = BitmapUtil.decodeSampledBitmapFromFile(filepath,faceImageViewWidth,faceImageViewHeight);
                rightFaceView.setImageBitmap(rightImage);
                rightFaceView.setVisibility(View.VISIBLE);
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
                leftFaceView.setVisibility(View.GONE);
                hasLeftFaceImage = false;
                // 回收内存
                if(leftImage!=null&&!leftImage.isRecycled()){
                    leftImage.recycle();
                    leftImage = null;
                }
                break;
            case RIGHT_FACE_IMAGE_VIEW:
                rightFaceView.setVisibility(View.GONE);
                hasRightFaceImage = false;
                if(rightImage!=null&&!rightImage.isRecycled()){
                    rightImage.recycle();
                    rightImage = null;
                }
                break;
        }

    }
    private void bitmapRecycle(){
        // 回收bitmap内存
        if(leftImage!=null&&!leftImage.isRecycled()){
            leftImage.recycle();
            leftImage = null;
        }
        if(rightImage!=null&&!rightImage.isRecycled()){
            rightImage.recycle();
            rightImage = null;
        }
        System.gc();
    }

    @Override
    public void finish() {
        bitmapRecycle();
        super.finish();
    }
}
