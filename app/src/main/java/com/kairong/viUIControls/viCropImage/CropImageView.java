package com.kairong.viUIControls.viCropImage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kairong.viUtils.BitmapUtil;
import com.kairong.vision_recognition.R;
import com.kairong.vision_recognition.viApplication;

/**
 * Created by Kairong on 2015/7/13 at USTC
 * mail:wangkrhust@gmail.com
 * blog:http://blog.csdn.net/wangkr111
 */
public class CropImageView extends View {
    // 在touch重要用到的点，
    private float mX_1 = 0;
    private float mY_1 = 0;
    // 触摸事件判断
    private final int STATUS_SINGLE = 1;
    private final int STATUS_MULTI_START = 2;
    private final int STATUS_MULTI_TOUCHING = 3;
    // 当前状态
    private int mStatus = STATUS_SINGLE;
    // 默认裁剪的宽高
    private int cropWidth;
    private int cropHeight;
    // 底层Drawable宽高
    private int srcDrawableWidth;
    private int srcDrawableHeight;
    // 浮层Drawable的四个点
    private final int EDGE_LT = 1;
    private final int EDGE_RT = 2;
    private final int EDGE_LB = 3;
    private final int EDGE_RB = 4;
    private final int EDGE_MOVE_IN = 5;
    private final int EDGE_MOVE_OUT = 6;
    private final int EDGE_NONE = 7;

    public int currentEdge = EDGE_NONE;

    protected float oriRationWH = 0;
    protected float fixedWHratio = -1;
    protected float srcDrawableWHratio = -1;
    protected final float maxZoomOut = 5.0f;
    protected final float minZoomIn = 0.333333f;

    protected Drawable mDrawable;
    protected FloatDrawable mFloatDrawable;

    protected Rect mDrawableSrc = new Rect();// 图片Rect变换时的Rect
    protected Rect mDrawableDst = new Rect();// 图片Rect
    protected Rect mDrawableFloat = new Rect();// 浮层的Rect
    protected boolean isFrist = true;
    private boolean isTouchInSquare = true;
    protected boolean ifFixedWHratio = false;

    protected Context mContext;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    @SuppressLint("NewApi")
    private void init(Context context) {
        this.mContext = context;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                this.setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFloatDrawable = new FloatDrawable(context);
    }

    public void fixWHratio(boolean ifFixedWHratio){
        this.ifFixedWHratio = ifFixedWHratio;
    }

    public void setDrawable(Drawable mDrawable, int cropWidth, int cropHeight) {
        this.mDrawable = mDrawable;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.isFrist = true;
        this.fixedWHratio = (float)cropWidth/cropHeight;

        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() > 1) {
            if (mStatus == STATUS_SINGLE) {
                mStatus = STATUS_MULTI_START;
            } else if (mStatus == STATUS_MULTI_START) {
                mStatus = STATUS_MULTI_TOUCHING;
            }
        } else {
            if (mStatus == STATUS_MULTI_START
                    || mStatus == STATUS_MULTI_TOUCHING) {
                mX_1 = event.getX();
                mY_1 = event.getY();
            }

            mStatus = STATUS_SINGLE;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX_1 = event.getX();
                mY_1 = event.getY();
                currentEdge = getTouch((int) mX_1, (int) mY_1);
                isTouchInSquare = mDrawableFloat.contains((int) event.getX(),
                        (int) event.getY());
                break;

            case MotionEvent.ACTION_UP:
                checkBounds();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                currentEdge = EDGE_NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mStatus == STATUS_MULTI_TOUCHING) {

                } else if (mStatus == STATUS_SINGLE) {
                    int dx = (int) (event.getX() - mX_1);
                    int dy = (int) (event.getY() - mY_1);
                    mX_1 = event.getX();
                    mY_1 = event.getY();

                    // 根据得到的那一个角，并且变换Rect
                    if (!(dx == 0 && dy == 0)) {
                        int abs_dx = Math.abs(dx),abs_dy = Math.abs(dy);
                        // 固定比例变换的delta x y 的值
                        int fixed_dy = Math.round(abs_dx/fixedWHratio);
                        int fixed_dx = Math.round(abs_dy*fixedWHratio);
                        int dx_tmp = fixed_dx - abs_dx,dy_tmp = fixed_dy - abs_dy;
                        int dL = 0,dT = 0,dR = 0,dB = 0;
                        switch (currentEdge) {
                            case EDGE_LT:
                                dL = dx;dT = dy;
                                if(ifFixedWHratio){
                                    dB = fixed_dy>abs_dy?(dy>=0?(dx>=0?-dy_tmp:abs_dy+fixed_dy):(dx>=0?-(abs_dy+fixed_dy):dy_tmp)):0;
                                    dR = fixed_dx>abs_dx?(dx>=0?(dy>=0?-dx_tmp:abs_dx+fixed_dx):(dy>=0?-(abs_dx+fixed_dx):dx_tmp)):0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left + dL,
                                        mDrawableFloat.top + dT, mDrawableFloat.right + dR,
                                        mDrawableFloat.bottom + dB);
                                break;

                            case EDGE_RT:
                                dR = dx;dT = dy;
                                if(ifFixedWHratio){
                                    dB = fixed_dy>abs_dy?(dy>=0?(dx>=0?abs_dy+fixed_dy:-dy_tmp):(dx>=0?dy_tmp:-(abs_dy+fixed_dy))):0;
                                    dL = fixed_dx>abs_dx?(dx>=0?(dy>=0?abs_dx+fixed_dx:-dx_tmp):(dy>=0?abs_dx:-(abs_dx+fixed_dx))):0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left + dL,
                                        mDrawableFloat.top + dT, mDrawableFloat.right + dR,
                                        mDrawableFloat.bottom + dB);
                                break;

                            case EDGE_LB:
                                dL = dx;dB = dy;
                                if(ifFixedWHratio){
                                    dT = fixed_dy>abs_dy?(dy>=0?(dx>=0?abs_dy+fixed_dy:-dy_tmp):(dx>=0?dy_tmp:-(abs_dy+fixed_dy))):0;
                                    dR = fixed_dx>abs_dx?(dx>=0?(dy>=0?abs_dx+fixed_dx:-dx_tmp):(dy>=0?dx_tmp:-(abs_dx+fixed_dx))):0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left + dL,
                                        mDrawableFloat.top + dT, mDrawableFloat.right + dR,
                                        mDrawableFloat.bottom + dB);
                                break;

                            case EDGE_RB:
                                dR = dx;dB = dy;
                                if(ifFixedWHratio){
                                    dT = fixed_dy>abs_dy?(dy>=0?(dx>=0?-dy_tmp:abs_dy+fixed_dy):(dx>=0?-(abs_dy+fixed_dy):dy_tmp)):0;
                                    dL = fixed_dx>abs_dx?(dx>=0?(dy>=0?-dx_tmp:abs_dx+fixed_dx):(dy>=0?-(abs_dx+fixed_dx):dx_tmp)):0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left + dL,
                                        mDrawableFloat.top + dT, mDrawableFloat.right + dR,
                                        mDrawableFloat.bottom + dB);
                                break;

                            case EDGE_MOVE_IN:
                                if (isTouchInSquare) {
                                    mDrawableFloat.offset(dx, dy);
                                }
                                break;

                            case EDGE_MOVE_OUT:
                                break;
                        }
                        mDrawableFloat.sort();
                        invalidate();
                    }
                }
                break;
        }

        return true;
    }

    // 根据初触摸点判断是触摸的Rect哪一个角
    public int getTouch(int eventX, int eventY) {
        if (mFloatDrawable.getBounds().left <= eventX
                && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable
                .getBorderWidth())
                && mFloatDrawable.getBounds().top <= eventY
                && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable
                .getBorderHeight())) {
            return EDGE_LT;
        } else if ((mFloatDrawable.getBounds().right - mFloatDrawable
                .getBorderWidth()) <= eventX
                && eventX < mFloatDrawable.getBounds().right
                && mFloatDrawable.getBounds().top <= eventY
                && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable
                .getBorderHeight())) {
            return EDGE_RT;
        } else if (mFloatDrawable.getBounds().left <= eventX
                && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable
                .getBorderWidth())
                && (mFloatDrawable.getBounds().bottom - mFloatDrawable
                .getBorderHeight()) <= eventY
                && eventY < mFloatDrawable.getBounds().bottom) {
            return EDGE_LB;
        } else if ((mFloatDrawable.getBounds().right - mFloatDrawable
                .getBorderWidth()) <= eventX
                && eventX < mFloatDrawable.getBounds().right
                && (mFloatDrawable.getBounds().bottom - mFloatDrawable
                .getBorderHeight()) <= eventY
                && eventY < mFloatDrawable.getBounds().bottom) {
            return EDGE_RB;
        } else if (mFloatDrawable.getBounds().contains(eventX, eventY)) {
            return EDGE_MOVE_IN;
        }
        return EDGE_MOVE_OUT;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mDrawable == null) {
            return;
        }

        if (mDrawable.getIntrinsicWidth() == 0
                || mDrawable.getIntrinsicHeight() == 0) {
            return;
        }

        configureBounds();
        // 在画布上画图片
        mDrawable.draw(canvas);
        canvas.save();
        // 在画布上画浮层FloatDrawable,Region.Op.DIFFERENCE是表示Rect交集的补集
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        // 在交集的补集上画上灰色用来区分
        canvas.drawColor(Color.parseColor("#880099cc"));
        canvas.restore();
        // 画浮层
        mFloatDrawable.draw(canvas);
    }

    protected void configureBounds() {
        // configureBounds在onDraw方法中调用
        // isFirst的目的是下面对mDrawableSrc和mDrawableFloat只初始化一次，
        // 之后的变化是根据touch事件来变化的，而不是每次执行重新对mDrawableSrc和mDrawableFloat进行设置
        if (isFrist) {
            oriRationWH = ((float) mDrawable.getIntrinsicWidth())
                    / ((float) mDrawable.getIntrinsicHeight());

            final float scale = mContext.getResources().getDisplayMetrics().density;
            int w = Math.min(getWidth(), (int) (mDrawable.getIntrinsicWidth()
                    * scale + 0.5f));
            int h = (int) (w / oriRationWH);

            srcDrawableWidth = w;
            srcDrawableHeight = h;

            srcDrawableWHratio = (float)srcDrawableWidth/srcDrawableHeight;

            int left = (getWidth() - w) / 2;
            int top = (getHeight() - h) / 2;
            int right = left + w;
            int bottom = top + h;

            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);

            int floatWidth = cropWidth;
            int floatHeight = cropHeight;

            int floatLeft = (getWidth() - floatWidth) / 2;
            int floatTop = (getHeight() - floatHeight) / 2;
            mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth,
                    floatTop + floatHeight);

            isFrist = false;
        }

        mDrawable.setBounds(mDrawableDst);
        mFloatDrawable.setBounds(mDrawableFloat);
    }
    // 在up事件中调用了该方法，目的是检查是否把浮层拖出了屏幕
    protected void checkBounds() {
        int newLeft = mDrawableFloat.left;
        int newTop = mDrawableFloat.top;


        boolean isChange = false;
        if (mDrawableFloat.left < mDrawableSrc.left) {
            newLeft = mDrawableSrc.left;
            isChange = true;
        }

        if (mDrawableFloat.top < mDrawableSrc.top) {
            newTop = mDrawableSrc.top;
            isChange = true;
        }

        if (mDrawableFloat.right > mDrawableSrc.right) {
            newLeft = mDrawableSrc.right - mDrawableFloat.width();
            isChange = true;
        }

        if (mDrawableFloat.bottom > mDrawableSrc.bottom) {
            newTop = mDrawableSrc.bottom - mDrawableFloat.height();
            isChange = true;
        }
        // 超出大小则重新绘制
        if(mDrawableFloat.width()>mDrawableSrc.width()||mDrawableFloat.height()>mDrawableSrc.height()){
            int left = (getWidth() - srcDrawableWidth) / 2;
            int top = (getHeight() - srcDrawableHeight) / 2;
            int right = left + srcDrawableWidth;
            int bottom = top + srcDrawableHeight;

            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);

            int floatWidth = cropWidth;
            int floatHeight = cropHeight;

            int floatLeft = (getWidth() - floatWidth) / 2;
            int floatTop = (getHeight() - floatHeight) / 2;
            mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth,
                    floatTop + floatHeight);
            isChange = true;
        }else {
            mDrawableFloat.offsetTo(newLeft, newTop);
        }
        if (isChange) {
            invalidate();
        }
    }

    public void refreshDrawable(float crop_wh_ratio){
        fixedWHratio = crop_wh_ratio;

        if(fixedWHratio>=srcDrawableWHratio){
            cropWidth = srcDrawableWidth;
            cropHeight = (int)(cropWidth / fixedWHratio);
        } else {
            cropHeight = srcDrawableHeight;
            cropWidth = (int)(cropHeight*fixedWHratio);
        }

        int left = (getWidth() - srcDrawableWidth) / 2;
        int top = (getHeight() - srcDrawableHeight) / 2;
        int right = left + srcDrawableWidth;
        int bottom = top + srcDrawableHeight;

        mDrawableSrc.set(left, top, right, bottom);
        mDrawableDst.set(mDrawableSrc);

        int floatWidth = cropWidth;
        int floatHeight = cropHeight;

        int floatLeft = (getWidth() - floatWidth) / 2;
        int floatTop = (getHeight() - floatHeight) / 2;
        mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth,
                floatTop + floatHeight);
        invalidate();
    }
    // 进行图片的裁剪，所谓的裁剪就是根据Drawable的新的坐标在画布上创建一张新的图片
    public Bitmap getCropImage(String originFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originFilePath,options);
        float scale = (float)options.outWidth/srcDrawableWidth;

        Bitmap tmpBitmap = BitmapFactory.decodeFile(originFilePath);
        int savedWidth = (int)Math.floor(mDrawableFloat.width()*scale);
        int savedHeight = (int)Math.floor(mDrawableFloat.height()*scale);
        int newleft = (int)Math.floor((mDrawableFloat.left-mDrawableSrc.left) * scale);
        int newtop = (int)Math.floor((mDrawableFloat.top-mDrawableSrc.top) * scale);

        Bitmap ret = Bitmap.createBitmap(tmpBitmap, newleft,
                newtop, savedWidth,
                savedHeight, null, true);
        tmpBitmap.recycle();
        return ret;
    }

    public int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
