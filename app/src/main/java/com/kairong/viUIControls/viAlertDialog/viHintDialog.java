package com.kairong.viUIControls.viAlertDialog;

/**
 * Created by Kairong on 2015/6/14.
 * mail:wangkrhust@gmail.com
 */
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kairong.vision_recognition.R;

/**
 * @文件名称: MyDialog.java
 * @功能描述: 自定义dialog
 * @版本信息: Copyright (c)2014
 * @开发人员: vincent
 * @版本日志: 1.0
 * @创建时间: 2014年3月18日 下午1:45:38
 */
public class viHintDialog extends Dialog implements OnClickListener {
    private TextView leftTextView, rightTextView;
    private IDialogOnclickInterface dialogOnclickInterface;
    private Context context;

    public viHintDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hint_dialog_layout);

        leftTextView = (TextView) findViewById(R.id.textview_one);
        rightTextView = (TextView) findViewById(R.id.textview_two);
        leftTextView.setOnClickListener(this);
        rightTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        dialogOnclickInterface = (IDialogOnclickInterface) context;
        switch (v.getId()) {
            case R.id.textview_one:
                dialogOnclickInterface.leftOnclick();
                break;
            case R.id.textview_two:
                dialogOnclickInterface.rightOnclick();
                break;
            default:
                break;
        }
    }

    public interface IDialogOnclickInterface {
        void leftOnclick();

        void rightOnclick();
    }
}

