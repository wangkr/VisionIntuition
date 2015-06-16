package com.kairong.myPathButton;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

@SuppressLint("ViewConstructor")
public class composerLayout extends RelativeLayout {

	public static byte RIGHTBOTTOM = 1, CENTERBOTTOM = 2, LEFTBOTTOM = 3,
			LEFTCENTER = 4, LEFTTOP = 5, CENTERTOP = 6, RIGHTTOP = 7,
			RIGHTCENTER = 8;
	private boolean hasInit = false; // 初始化了没有
	private boolean areButtonsShowing = false;// 有没有展开
	private Context mycontext;
	private ImageView cross; // 主按钮中间那个十字
	private RelativeLayout rlButton;// 主按钮
	private myAnimations myani; // 动画类
	private RelativeLayout[] llayouts; // 子按钮集
	private int duretime = 300;

	/**
	 * 构造函数 本来想在构造函数度读取参数的，但就要在values下面搞个attr，同埋layout的xml又要加命名空间————
	 * 咁搞的话~好多人可能唔知点用，而且参数太多（例如N个子按钮）处理起身亦比较罗嗦。
	 * 所以而家干脆搞个init()函数，由java代码调用，唔读xml喇。 所以构造函数只记录个context就算
	 */
	public composerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mycontext = context;
	}

	public composerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mycontext = context;
	}

	public composerLayout(Context context) {
		super(context);
		this.mycontext = context;
	}

	/**
	 * 初始化
	 * 
	 * @param imgResId
	 *            子按钮的图片drawalbe的id[]
	 * @param showhideButtonId
	 *            主按钮的图片drawable的id
	 * @param crossId
	 *            主按钮上面那个转动十字的图片drawable的id
	 * @param pCode
	 *            位置代码，例如“右上角”係ALIGN_PARENT_BOTTOM|ALIGN_PARENT_RIGHT
	 * @param radius
	 *            半径
	 * @param durationMillis
	 *            动画耗时
	 */
	public void init(int imageID,int[] imgResId, int showhideButtonId, int crossId,
			byte pCode, int radius, final int durationMillis) {
		duretime = durationMillis;
		// 处理pcode，将自定义的位置值改成align值
		int align1 = 12, align2 = 14;
		if (pCode == RIGHTBOTTOM) { // 右下角
			align1 = ALIGN_PARENT_RIGHT;
			align2 = ALIGN_PARENT_BOTTOM;
		} else if (pCode == CENTERBOTTOM) {// 中下
			align1 = CENTER_HORIZONTAL;
			align2 = ALIGN_PARENT_BOTTOM;
		} else if (pCode == LEFTBOTTOM) { // 左下角
			align1 = ALIGN_PARENT_LEFT;
			align2 = ALIGN_PARENT_BOTTOM;
		} else if (pCode == LEFTCENTER) { // 左中
			align1 = ALIGN_PARENT_LEFT;
			align2 = CENTER_VERTICAL;
		} else if (pCode == LEFTTOP) { // 左上角
			align1 = ALIGN_PARENT_LEFT;
			align2 = ALIGN_PARENT_TOP;
		} else if (pCode == CENTERTOP) { // 中上
			align1 = CENTER_HORIZONTAL;
			align2 = ALIGN_PARENT_TOP;
		} else if (pCode == RIGHTTOP) { // 右上角
			align1 = ALIGN_PARENT_RIGHT;
			align2 = ALIGN_PARENT_TOP;
		} else if (pCode == RIGHTCENTER) { // 右中
			align1 = ALIGN_PARENT_RIGHT;
			align2 = CENTER_VERTICAL;
		}
		// 如果细过半径就整大佢
		LayoutParams thislps = (LayoutParams) this
				.getLayoutParams();
		Bitmap mBottom = BitmapFactory.decodeResource(mycontext.getResources(),
				imageID);
		if (pCode == CENTERBOTTOM || pCode == CENTERTOP) {
			if (thislps.width != -1
					&& thislps.width != -2
					&& thislps.width < (radius + mBottom.getWidth() + radius * 0.1) * 2) {
				thislps.width = (int) ((radius * 1.1 + mBottom.getWidth()) * 2);
			}
		} else {
			if (thislps.width != -1
					&& thislps.width != -2
					&& thislps.width < radius + mBottom.getWidth() + radius
							* 0.1) { // -1是FILL_PARENT，-2是WRAP_CONTENT
				// 因為animation的setInterpolator设咗OvershootInterpolator，即系喐到目标之后仍然行多一段（超过目标位置）~然后再缩返到目标位置，所以父layout就要再放大少少。而因為呢个OvershootInterpolator接纳的是一个弹力（浮点）值，佢经过一定算法计算出个时间……如果要根据呢个弹力转换做距离数值，就比较麻烦，所以我只系求其加咗1/10个半径。想追求完美的~可以自行研究下OvershootInterpolator类同Animation类，http://www.oschina.net可以揾倒android
				// sdk的源码。
				thislps.width = (int) (radius * 1.1 + mBottom.getWidth());
			}
		}
		if (pCode == LEFTCENTER || pCode == RIGHTCENTER) {
			if (thislps.height != -1
					&& thislps.height != -2
					&& thislps.height < (radius + mBottom.getHeight() + radius * 0.1) * 2) {
				thislps.width = (int) ((radius * 1.1 + mBottom.getHeight()) * 2);
			}
		} else {
			if (thislps.height != -1
					&& thislps.height != -2
					&& thislps.height < radius + mBottom.getHeight() + radius
							* 0.1) {
				thislps.height = (int) (radius * 1.1 + mBottom.getHeight());
			}
		}
		this.setLayoutParams(thislps);
		// 两个主要层
		RelativeLayout rl1 = new RelativeLayout(mycontext);// 包含若干子按钮的层

		rlButton = new RelativeLayout(mycontext); // 主按扭
		llayouts = new RelativeLayout[imgResId.length];
		// N个子按钮
		for (int i = 0; i < imgResId.length; i++) {
			ImageView img = new ImageView(mycontext);// 子按扭图片

			img.setId(3250+i);
			img.setImageDrawable(getResources().getDrawable(imgResId[i]));
			//img.setImageResource(imgResId[i]);
			RelativeLayout.LayoutParams llps = new RelativeLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llps.alignWithParent = true;
			llps.addRule(CENTER_IN_PARENT,RelativeLayout.TRUE);
			img.setLayoutParams(llps);
			llayouts[i] = new RelativeLayout(mycontext);// 子按钮层
			llayouts[i].setId(3240 + i);// 随便设个id，方便onclick的时候识别返出值。呢个id值是求其设的，如果发现同其他控件冲突就自行改一下。
			llayouts[i].addView(img);

			LayoutParams rlps = new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			rlps.alignWithParent = true;
			rlps.addRule(align1, RelativeLayout.TRUE);
			rlps.addRule(align2, RelativeLayout.TRUE);
			llayouts[i].setLayoutParams(rlps);
			llayouts[i].setVisibility(View.INVISIBLE);// 此处不能为GONE
			rl1.addView(llayouts[i]);
		}
		LayoutParams rlps1 = new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		rlps1.alignWithParent = true;
		rlps1.addRule(align1, RelativeLayout.TRUE);
		rlps1.addRule(align2, RelativeLayout.TRUE);
		rl1.setLayoutParams(rlps1);

		LayoutParams buttonlps = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		buttonlps.alignWithParent = true;
		buttonlps.addRule(align1, RelativeLayout.TRUE);
		buttonlps.addRule(align2, RelativeLayout.TRUE);
		rlButton.setLayoutParams(buttonlps);
		rlButton.setBackgroundResource(showhideButtonId);
		cross = new ImageView(mycontext);
		cross.setImageResource(crossId);
		LayoutParams crosslps = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		crosslps.alignWithParent = true;
		crosslps.addRule(CENTER_IN_PARENT, RelativeLayout.TRUE);
		cross.setLayoutParams(crosslps);
		rlButton.addView(cross);
		myani = new myAnimations(rl1, pCode, radius);
		rlButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (areButtonsShowing) {
					myani.startAnimationsOut(duretime);
					cross.startAnimation(myAnimations.getRotateAnimation(-45,
							0, duretime));
				} else {
					myani.startAnimationsIn(duretime);
					cross.startAnimation(myAnimations.getRotateAnimation(0,
							-45, duretime));
				}
				areButtonsShowing = !areButtonsShowing;
			}
		});

		cross.startAnimation(myAnimations.getRotateAnimation(0, 360, 200));
		this.addView(rl1);
		this.addView(rlButton);
		hasInit = true;

	}

	/**
	 * 收埋
	 */
	public void collapse() {
		myani.startAnimationsOut(duretime);
		cross.startAnimation(myAnimations.getRotateAnimation(-45, 0, duretime));
		areButtonsShowing = false;
	}

	/**
	 * 打开
	 */
	public void expand() {
		myani.startAnimationsIn(duretime);
		cross.startAnimation(myAnimations.getRotateAnimation(0, -45, duretime));
		areButtonsShowing = true;
	}

	/**
	 * 初始化咗未（其实没有用，原来有就保留）
	 */
	public boolean isInit() {
		return hasInit;
	}

	/**
	 * 有没有展开（其实没有用，原来有就保留）
	 */
	public boolean isShow() {
		return areButtonsShowing;
	}

	/**
	 * 设置各子按钮的onclick事件
	 */
	public void setButtonsOnClickListener(final OnClickListener l) {

		if (llayouts != null) {
			for (int i = 0; i < llayouts.length; i++) {
				if (llayouts[i] != null)
					llayouts[i].setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(final View view) {
							//此处添加其他事件比如按钮增大或者缩回菜单
							collapse();
							Log.d("composerLayout", "setButtonOnClickListener");
							l.onClick(view);
						}

					});
			}
		}
	}
}
