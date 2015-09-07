package com.kairong.viUIControls.viPreferences;

/**
 * Created by Kairong on 2015/8/29.
 * mail:wangkrhust@gmail.com
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.kairong.viUtils.CameraUtil;
import com.kairong.vision_recognition.R;
import com.kairong.vision_recognition.viApplication;

import java.util.List;


public class viSeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private static final String androidns="http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mSplashText,mValueText;
    private Context mContext;

    private String mDialogMessage;
    private List<Camera.Size> mPicSizelist;
    private int mMax, mValue = 0;
    private String stringValue;

    public viSeekBarPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
        mContext = context;
        mDialogMessage = mContext.getResources().getString(R.string.picQual_dialogMsg1);
        stringValue = viApplication.getViApp().getScreenHeight()+"x"+viApplication.getViApp().getScreenWidth();
        mMax = attrs.getAttributeIntValue(androidns, "max", 100);
        initPicSizeList();
    }
    private void initPicSizeList(){
        CameraUtil cameraUtil = CameraUtil.getCameraUtil();
        int w = viApplication.getViApp().getScreenWidth(),h = viApplication.getViApp().getScreenHeight();
        this.mPicSizelist = cameraUtil.getAllPictureSizeOfRatio(Camera.CameraInfo.CAMERA_FACING_BACK, CameraUtil.getWHratioString(h, w));
    }
    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30,30,30,30);

        mSplashText = new TextView(mContext);
        if (mDialogMessage != null)
            mSplashText.setText(mDialogMessage);
        layout.addView(mSplashText);

        mValueText = new TextView(mContext);
        mValueText.setTextColor(Color.WHITE);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(30);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setThumb(mContext.getResources().getDrawable(R.drawable.vi_thumb_bar));
        mSeekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.vi_bg_bar));
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        try {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View summary_layout = inflater.inflate(R.layout.vi_pic_qual_seek_bar_summary, null);
            layout.addView(summary_layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }catch (StackOverflowError e){
            e.printStackTrace();
        }


        if (shouldPersist()) {
            int iValue = 0;
            for(Camera.Size s:mPicSizelist){
                if(getPersistedString(stringValue).equals(s.width+"x"+s.height)) {
                    mValue = iValue;
                    break;
                }
                iValue++;
            }
        }

        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
        Camera.Size size = mPicSizelist.get(mValue);
        mValueText.setText("分辨率:" + size.width+"x"+size.height);
        return layout;
    }
    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
    }
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            int iValue = 0;
            for(Camera.Size s:mPicSizelist){
                if(getPersistedString(stringValue).equals(s.width+"x"+s.height)) {
                    mValue = shouldPersist() ? iValue : 0;
                    break;
                }
                iValue++;
            }
        }
        else {
            int iiValue = 0;
            String dfVal = (String)defaultValue;
            for(Camera.Size s:mPicSizelist){
                if(dfVal.equals(s.width + "x" + s.height)) {
                    mValue = iiValue;
                    break;
                }
                iiValue++;
            }
        }
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
    {
        Camera.Size s = mPicSizelist.get(value);
        String stringVal = s.width+"x"+s.height;
        mValueText.setText("分辨率:" + stringVal);
        setSummary(getQualLevel(value, mMax) + "(" + stringVal + ")");
        if (shouldPersist())
            persistString(stringVal);
        callChangeListener(stringVal);
    }
    public void onStartTrackingTouch(SeekBar seek) {}
    public void onStopTrackingTouch(SeekBar seek) {}
    public void setMax(int max) { mMax = max; }
    public int getMax() { return mMax; }

    public void setProgress(int progress) {
        mValue = progress;
        if (mSeekBar != null)
            mSeekBar.setProgress(progress);
    }
    public int getProgress() { return mValue; }
    public static String getQualLevel(int value,int mMax){
        float level = value*1f/mMax;
        if(level>=0&&level<=0.25){
            return "低";
        }else if (level>0.25&&level<=0.50){
            return "中";
        }else if (level>0.5&&level<=0.75){
            return "高";
        }else if (level>0.75){
            return "超高";
        }
        return "";
    }
}
