package com.kairong.vision_recognition;


import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.hardware.Camera.Size;
import android.support.annotation.NonNull;
import android.view.Window;

import com.kairong.viUIControls.viPreferences.viListPreference;
import com.kairong.viUIControls.viPreferences.viSeekBarPreference;
import com.kairong.viUtils.CameraUtil;

import java.util.List;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener{
    private viSeekBarPreference picQual;
    private viListPreference defCam;
    private List<Size> picQualSizelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        addPreferencesFromResource(R.xml.pref_vi);
        setPicQuality();
        setDefCamera();

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals(picQual.getKey())){

        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(picQual.getKey())){
            setPicQualSumm((String)newValue);
        }else if (preference.getKey().equals(defCam.getKey())){
            String defVal = (String)newValue;
            if(defVal.equals("1")){
                defCam.setSummary("后置");
            }else {
                defCam.setSummary("前置");
            }
        }
        return false;
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if(key.equals(picQual.getKey())){
//            String newValue = sharedPreferences.getString(key,"0x0");
//            setPicQualSumm(newValue);
//        }else if (key.equals(defCam.getKey())){
//            String defVal = defCam.getSharedPreferences().getString(key,"1");
//            if(defVal.equals("1")){
//                defCam.setSummary("后置");
//            }else {
//                defCam.setSummary("前置");
//            }
//        }
//    }
    private void setActionBar(){
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.layout_bar_color)));
        getActionBar().setIcon(R.mipmap.setting_button_icn);
    }
    private void setDefCamera(){
        String defCamKey = getResources().getString(R.string.def_cam_key);

        defCam = (viListPreference)findPreference(defCamKey);

        String defVal = defCam.getSharedPreferences().getString(defCamKey,"0");
        if(defVal.equals("0")){
            defCam.setSummary("后置");
        }else {
            defCam.setSummary("前置");
        }
    }

    private void setPicQuality(){
        String picQualKey = getResources().getString(R.string.pic_quality_key);
        CameraUtil cameraUtil = CameraUtil.getCameraUtil();

        picQual = (viSeekBarPreference)findPreference(picQualKey);
        int w = viApplication.getViApp().getScreenWidth(),h = viApplication.getViApp().getScreenHeight();
        picQualSizelist = cameraUtil.getAllPictureSizeOfRatio(Camera.CameraInfo.CAMERA_FACING_BACK, CameraUtil.getWHratioString(h, w));

        int defValue = Math.round(picQualSizelist.size() / 2);
        int picQualMax = picQualSizelist.size()-1;

        picQual.setMax(picQualMax);
        picQual.setProgress(defValue);

        String value = picQual.getSharedPreferences().getString(picQualKey,""+picQualSizelist.get(defValue).width+"x"+picQualSizelist.get(defValue).height);
        setPicQualSumm(value);
    }
    private void setPicQualSumm(String value){
        int iValue = 0;
        for(Camera.Size s:picQualSizelist){
            if(value.equals(s.width+"x"+s.height)) {
                picQual.setSummary(viSeekBarPreference.getQualLevel(iValue, picQualSizelist.size() - 1)+"("+value+")");
                break;
            }
            iValue++;
        }
    }
}
