package com.kairong.myPathButton;

import java.util.ArrayList;
import java.util.List;

import com.kairong.viUtils.DisplayUtil;
import com.kairong.vision_recognition.R;
import com.kairong.vision_recognition.viApplication;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
public class myAnimations {

	public int R; // 半径
	public static byte RIGHTBOTTOM = 1, CENTERBOTTOM = 2, LEFTBOTTOM = 3,
			LEFTCENTER = 4, LEFTTOP = 5, CENTERTOP = 6, RIGHTTOP = 7,
			RIGHTCENTER = 8;

	private int pc; // 位置代号
	private ViewGroup clayout; // 父laoyout
	private int amount; // 有几多个按钮
	private double angleoffset = 30; // 角度偏移
	private double fullangle = 120.0;// 在几大的角度内排佈
	private byte xOri = 1, yOri = 1; // x、y值的方向，即系向上还是向下
	private boolean isOpen = false;// 记录是已经打开还是关闭
	public static Rect composerRect = null; // composerWrapper View 的Rect
	public static Rect composerBtnRect = null; // composerButton view的Rect
	private List<ViewPropertyAnimator> viewAnimators = new ArrayList<ViewPropertyAnimator>();

	private viApplication app;

	private String TAG = "myAnimations";
	/**
	 * 构造函数
	 * 
	 * @param comlayout
	 *            包裹弹出按钮的layout
	 * @param poscode
	 *            位置代号，分别对应RIGHTBOTTOM、CENTERBOTTOM、LEFTBOTTOM、LEFTCENTER、
	 *            LEFTTOP、CENTERTOP、RIGHTTOP、RIGHTCENTER
	 * @param radius
	 *            半径
	 */
	public myAnimations(final viApplication app,ViewGroup comlayout, int poscode, int radius) {
		this.pc = poscode;
		this.clayout = comlayout;
		this.amount = clayout.getChildCount();
		this.R = radius;
		this.app = app;

		initComposerBtns(app);

		// 初始化动画，每个view对应一个animator
		for (int i = 0; i < amount; i++) {
			View childAt = clayout.getChildAt(i);
			ViewPropertyAnimator anim = animate(childAt);
			viewAnimators.add(anim);
		}

		if (poscode == RIGHTBOTTOM) { // 右下角
			fullangle = 90;
			xOri = -1;
			yOri = -1;
		} else if (poscode == CENTERBOTTOM) {// 中下
			fullangle = 180;
			xOri = -1;
			yOri = -1;
		} else if (poscode == LEFTBOTTOM) { // 左下角
			fullangle = 90;
			xOri = 1;
			yOri = -1;
		} else if (poscode == LEFTCENTER) { // 左中
			fullangle = 180;
			xOri = 1;
			yOri = -1;
		} else if (poscode == LEFTTOP) { // 左上角
			fullangle = 90;
			xOri = 1;
			yOri = 1;
		} else if (poscode == CENTERTOP) { // 中上
			fullangle = 120;
			xOri = -1;
			yOri = 1;
		} else if (poscode == RIGHTTOP) { // 右上角
			fullangle = 90;
			xOri = -1;
			yOri = 1;
		} else if (poscode == RIGHTCENTER) { // 右中
			fullangle = 180;
			xOri = -1;
			yOri = -1;
		}
	}

	public void initComposerBtns(final viApplication app){
		// 初始化一些activity_main控件位置信息
		// 计算composerButtonWrapper 和 composerButtons的坐标信息
		composerRect = new Rect();
		composerBtnRect = new Rect();
		
		composerRect.top = app.getScreenHeight()/2 + (app.getMain_1_btn_size()/2);
		Log.d(TAG,"ScreenHeight"+app.getScreenHeight());
		composerRect.left = app.getActivity_horizontal_margin();
		composerRect.bottom = app.getScreenHeight() - app.getActivity_vertical_margin();
		composerRect.right = app.getScreenWidth() - app.getActivity_horizontal_margin();

		composerBtnRect.top = composerRect.top;
		composerBtnRect.left = app.getScreenWidth()/2 - (app.getMain_2_btn_size())/2;
		composerBtnRect.right = app.getScreenWidth()/2 + (app.getMain_2_btn_size())/2;
		composerBtnRect.bottom = composerBtnRect.top + (app.getMain_2_btn_size());
	}
//	private class AnimListener implements AnimatorListener {
//
//		private View target;
//
//		public AnimListener(View _target) {
//			target = _target;
//		}
//
//		@Override
//		public void onAnimationStart(Animator animation) {
//
//		}
//
//		@Override
//		public void onAnimationEnd(Animator animation) {
//			if (!isOpen) {
//				target.setVisibility(View.INVISIBLE);
//			}
//		}
//
//		@Override
//		public void onAnimationCancel(Animator animation) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onAnimationRepeat(Animator animation) {
//			// TODO Auto-generated method stub
//
//		}
//	}

	/**
	 * 弹几个按钮出来
	 * 
	 * @param durationMillis
	 *            用几多时间
	 */
	public void startAnimationsIn(int durationMillis) {
		isOpen = true;
		for (int i = 0; i < clayout.getChildCount(); i++) {
			final ImageView inoutimagebutton = (ImageView) clayout
					.getChildAt(i);

			double offangle = fullangle / (amount - 1);

			final int deltaY, deltaX;
			if (pc == LEFTCENTER || pc == RIGHTCENTER) {
				deltaX = (int) (Math.sin((angleoffset+offangle * i) * Math.PI / 180) * R);
				deltaY = (int) (Math.cos((angleoffset+offangle * i) * Math.PI / 180) * R);
			} else {
				deltaY = (int) (Math.sin((angleoffset+offangle * i) * Math.PI / 180) * R);
				deltaX = (int) (Math.cos((angleoffset+offangle * i) * Math.PI / 180) * R);
			}
			Log.d(TAG,"composerBtnRect left:"+composerBtnRect.left);

			/**Created by Kairong Wang,2015.05.29*/
			Animation animation = new TranslateAnimation(0,deltaX,0,deltaY);
			animation.setFillAfter(true);
			animation.setDuration(durationMillis);
			animation.setStartOffset((int) ((i * 100) / (-1 + clayout.getChildCount())));
			animation.setInterpolator(new OvershootInterpolator(2F));
			// 获取每个按钮的layout属性
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(inoutimagebutton.getLayoutParams());
			Rect r = new Rect(composerBtnRect);
			Rect r2 = new Rect(composerRect);
			r.offset(deltaX, deltaY);
			params.setMargins(r.left - r2.left, r.top - r2.top, 0,0);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					inoutimagebutton.setLayoutParams(params);
					inoutimagebutton.setVisibility(View.VISIBLE);
					inoutimagebutton.clearAnimation();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			inoutimagebutton.startAnimation(animation);

		    /*************************************/
	    }
	}

	/**
	 * 收埋几个按钮入去
	 * 
	 * @param durationMillis
	 *            用几多时间
	 */
	public void startAnimationsOut(int durationMillis) {
		isOpen = false;
		for (int i = 0; i < clayout.getChildCount(); i++) {
			final ImageView inoutimagebutton = (ImageView) clayout
					.getChildAt(i);
			// 将按钮移动到原来的位置
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(inoutimagebutton.getLayoutParams());
			Rect r = new Rect(composerBtnRect);
			Rect r2 = new Rect(composerRect);
			params.setMargins(r.left-r2.left,r.top - r2.top,0,0);
			inoutimagebutton.setLayoutParams(params);
			double offangle = fullangle / (amount - 1);

			final int deltaY, deltaX;
			if (pc == LEFTCENTER || pc == RIGHTCENTER) {
				deltaX = (int) (Math.sin((angleoffset+offangle * i) * Math.PI / 180) * R);
				deltaY = (int) (Math.cos((angleoffset+offangle * i) * Math.PI / 180) * R);
			} else {
				deltaY = (int) (Math.sin((angleoffset+offangle * i) * Math.PI / 180) * R);
				deltaX = (int) (Math.cos((angleoffset+offangle * i) * Math.PI / 180) * R);
			}
			Log.d(TAG,"composerBtnRect left:"+composerBtnRect.left);
			/**Created by Kairong Wang,2015.05.29*/
			Animation animation = new TranslateAnimation((int)deltaX,0,(int)deltaY,0);
			animation.setFillAfter(true);
			animation.setDuration(durationMillis);
			animation.setStartOffset((int) ((clayout.getChildCount() - i) * 100 / (-1 + clayout.getChildCount())));
			animation.setInterpolator(new AnticipateInterpolator(2F));
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					if (!isOpen) {
						// 获取每个按钮的layout属性
						inoutimagebutton.clearAnimation();
						inoutimagebutton.setVisibility(View.GONE);
						Log.d(TAG, "onAnimationEnd");
					}

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			inoutimagebutton.startAnimation(animation);
			/*************************************/
		}

	}

	/**
	 * 获取位置代码（其实貌似都冇乜用）
	 */
	public int getPosCode() {
		return this.pc;
	}

	/**
	 * 自转函数（原本就有的静态函数，未实体化都可以调用）
	 * 
	 * @param fromDegrees
	 *            从几多度
	 * @param toDegrees
	 *            到几多度
	 * @param durationMillis
	 *            转几耐
	 */
	public static Animation getRotateAnimation(float fromDegrees,
			float toDegrees, int durationMillis) {
		RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(durationMillis);
		rotate.setFillAfter(true);
		return rotate;
	}

	/**
	 * 返回按钮动画状态：是打开还是合并
	 */
	public boolean isOpen(){
		return isOpen;
	}
}