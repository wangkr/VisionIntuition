package com.kairong.viUIControls.viAlertDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kairong.viUIControls.viCamera.viCameraActivity;
import com.kairong.vision_recognition.R;

/**
 * Created by Kairong on 2015/6/10.
 * mail:wangkrhust@gmail.com
 */
public class viAlertDialog extends Dialog {
    Context context;
    private String titleString;
    public viAlertDialog(Context context){
        super(context);
        this.context = context;
        titleString = "";

    }
    public viAlertDialog(Context context,String titleString){
        super(context);
        this.context = context;
        this.titleString = titleString;
    }
    public viAlertDialog(Context context, int theme,String titleString){
        super(context,theme);
        this.context = context;
        this.titleString = titleString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vi_dialog_xml);
        RelativeLayout cancell_button = (RelativeLayout)findViewById(R.id.aldl_clbtn_layout);
        ((TextView)findViewById(R.id.alertTitle)).setText(titleString);
        cancell_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void init(final Activity activity,final int requestCamCode,final int requestGalCode,final String tag){
        findViewById(R.id.aldl_cmbtn_layout).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent imgIntent = new Intent(activity, viCameraActivity.class);
                imgIntent.putExtra("SrcTag", tag);
                activity.startActivityForResult(imgIntent, requestCamCode);
                dismiss();
            }
        });
        findViewById(R.id.aldl_glbtn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");//相片类型
                activity.startActivityForResult(intent, requestGalCode);
                dismiss();
            }
        });
    }

}
