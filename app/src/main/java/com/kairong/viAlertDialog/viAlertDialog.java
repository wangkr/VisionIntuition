package com.kairong.viAlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        setContentView(R.layout.vi_alertdialog_activity);
        ImageView cancell_button = (ImageView)findViewById(R.id.btn_aldl_cancell);
        ((TextView)findViewById(R.id.alertTitle)).setText(titleString);
        cancell_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

}
