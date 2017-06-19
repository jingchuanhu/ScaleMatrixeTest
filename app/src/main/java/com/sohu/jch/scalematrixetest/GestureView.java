package com.sohu.jch.scalematrixetest;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by jch on 2017/6/12.
 */

public class GestureView extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener, View
        .OnTouchListener {

    public interface VideoChangeEvents {

        public void videoScale(float scaleX, float sccaleY);

        public void videoTranslate(float translateX, float translateY);

        public void videoChangeSize(int left, int top, int right, int bottom);

    }


    private static final String TAG = GestureView.class.getSimpleName();
    public static final float SCALE_MAX = 2.0f;
    private static final float SCALE_MID = 2.0f;
    private static final float SCALE_MIN = 0.5f;

    //前一次放缩比例。
    private float preSclae = 1;

    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float initScale = 0.2f;
    private boolean once = true;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private final Matrix mMatrix = new Matrix();

    private VideoChangeEvents events;

    /**
     * 用于双击检测
     */
//    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    private int mTouchSlop;

    private float mLastX;
    private float mLastY;

    private boolean isCanDrag;
    private int lastPointerCount;

    //view 縮放比例。
    private float viewScale = 1;

    private boolean isCheckTopAndBottom = true;
    private boolean isCheckLeftAndRight = true;

    private View childView;


    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setOnTouchListener(this);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setOnTouchListener(this);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof VideoChangeEvents) {
            events = (VideoChangeEvents) child;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec);
        final int height = getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec);
        childView = getChildAt(0);
        int childWidth = childView.getLayoutParams().width;
        int childHeight = childView.getLayoutParams().height;
//
        float scale = getChileScale(width, height, childWidth, childHeight);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidth * scale + 0.5f), MeasureSpec
                .EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childHeight * scale + 0.5f), MeasureSpec
                .EXACTLY);

        childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private float getChileScale(int bWidth, int bHeight, int cWidth, int cHeight) {

        float wScale = (float) bWidth / (float) cWidth;
        float hScale = (float) bHeight / (float) cHeight;

        float scale = Math.max(wScale, hScale);

        return scale;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout");
        int width = childView.getMeasuredWidth();
        int height = childView.getMeasuredHeight();

        int bwidth = r - l;
        int bheight = b - l;

        int left = (bwidth - width) / 2;
        int top = (bheight - height) / 2;
        int right = left + width;
        int bottom = top + height;
        childView.layout(left, top, right, bottom);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float scaleFactor = detector.getScaleFactor();
        float postScale = scaleFactor / preSclae;   //由于matrix的post方法累计scale,需要去除上一次的scale.

        float scale = getScale();
        float aftercale = scale * postScale;

        // 设置缩放范围
        if (aftercale > SCALE_MAX) {
            postScale = SCALE_MAX / scale;
        } else if (aftercale < SCALE_MIN) {
            postScale = SCALE_MIN / scale;
        }

        mMatrix.postScale(postScale, postScale, detector.getFocusX(), detector.getFocusY());
        Log.d(TAG, " after post scale : " + getScale());

        checkMatrixBounds();

        updateVideoViewScale();
        updateVideoViewTranslate();
        preSclae = detector.getScaleFactor();


        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {

        preSclae = 1;
//        Log.d(TAG, "begin scale : " + viewScale);

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mScaleGestureDetector.onTouchEvent(event);

        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            mLastX = x;
            mLastY = y;
        }


        lastPointerCount = pointerCount;

        Log.d(TAG, "action : " + event.getAction());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                Log.i(TAG, "down: X: " + event.getX() + " Y: " + event.getY());
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;
                Log.d(TAG, "move: X: " + dx + " Y: " + dy);
                if (isCanDrag(dx, dy)) {
//                    RectF rectF = getMatrixRectF();
//                    isCheckLeftAndRight = isCheckTopAndBottom = true;
//                    // 如果宽度小于屏幕宽度，则禁止左右移动
//                    if (rectF.width() < getWidth()) {
//                        dx = 0;
//                        isCheckLeftAndRight = false;
//                    }
//                    // 如果高度小雨屏幕高度，则禁止上下移动
//                    if (rectF.height() < getHeight()) {
//                        dy = 0;
//                        isCheckTopAndBottom = false;
//                    }
                    Log.d(TAG, "before translate x : " + getTranslateX() + " Y : " + getTranslateY());
                    mMatrix.postTranslate(dx, dy);
                    checkMatrixBounds();

                    Log.d(TAG, "after translate x : " + getTranslateX() + " Y : " + getTranslateY());

                    updateVideoViewTranslate();

//                    childView.setTranslationX(getTranslateX());
//                    childView.setTranslationY(getTranslateY());
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:

                Log.v(TAG, "up for translate X: " + getTranslateX() + "  Y : " + getTranslateY());
                Log.v(TAG, "up for scale  : " + getScale());
                break;
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                break;
        }

        return true;
    }

    private void updateVideoViewScale() {

        events.videoScale(getScale(), getScale());

//        RectF rectF = getMatrixRectF();
//        videoChangeEvents.videoChangeSize((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
    }


    /**
     * 根据视图窗口算出移动百分比。
     */
    private void updateVideoViewTranslate() {

        float width = childView.getMeasuredWidth();
        float height = childView.getMeasuredHeight();

        float percentX = getTranslateX() / width;
        float percentY = getTranslateY() / height;

        Log.d(TAG, "xTranslate : " + getTranslateX() + "width : " + width + " height : " + height);
        Log.d(TAG, "yTranslate : " + getTranslateY());

        events.videoTranslate(getTranslateX(), getTranslateY());
    }

//    @Override
//    public void onGlobalLayout() {
//        if (once) {
//            Drawable d = getDrawable();
//            if (d == null)
//                return;
//            Log.e(TAG, d.getIntrinsicWidth() + " , " + d.getIntrinsicHeight());
//            int width = getWidth();
//            int height = getHeight();
//            // 拿到图片的宽和高
//            int dw = d.getIntrinsicWidth();
//            int dh = d.getIntrinsicHeight();
//            float scale = 1.0f;
//            // 如果图片的宽或者高大于屏幕，则缩放至屏幕的宽或者高
//            if (dw > width && dh <= height) {
//                scale = width * 1.0f / dw;
//            }
//            if (dh > height && dw <= width) {
//                scale = height * 1.0f / dh;
//            }
//            // 如果宽和高都大于屏幕，则让其按按比例适应屏幕大小
//            if (dw > width && dh > height) {
//                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
//            }
//            initScale = scale;
//
//            Log.e(TAG, "initScale = " + initScale);
//            mMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
//            mMatrix.postScale(scale, scale, getWidth() / 2,
//                    getHeight() / 2);
//            // 图片移动至屏幕中心
//            setImageMatrix(mMatrix);
//
//            once = false;
//        }
//    }

    private void measureInitLayout() {


    }

    private void measureLayout() {

    }

    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {

        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }
        // 如果宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }


        Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);

        mMatrix.setTranslate(deltaX, deltaY);
        childView.setTranslationX(deltaX);
        childView.setTranslationY(deltaY);
    }

    /**
     * 根据当view在Matrix中的布局的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mMatrix;
        RectF rect = new RectF();
        rect.set(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        matrix.mapRect(rect);

        return rect;
    }

    public void initMatrix(int width, int height) {
        int bWidth = getMeasuredWidth();
        int bHeight = getMeasuredHeight();
        childView = getChildAt(0);
        mMatrix.reset();
        childView.layout((bWidth - width) / 2, (bHeight - height) / 2, (bWidth + width) / 2, (height + bHeight) / 2);
        requestLayout();


    }

    private void scaleLayout(float scaleX, float scaleY) {
        /**
         * 设置缩放比例
         */
        mMatrix.setScale(scaleX, scaleX);
//            checkBorderAndCenterWhenScale();
        childView.setScaleY(scaleX);     //setScale 既是右乘
        childView.setScaleX(scaleX);
        requestLayout();
    }

    private void translateLayout(float dx, float dy) {
        mMatrix.setTranslate(dx, dy);
        childView.setTranslationX(dx);
        childView.setTranslationY(dy);
        requestLayout();
    }

    private void scaleTranslateLayout(float scaleX, float scaleY, float dx, float dy) {

        mMatrix.setScale(scaleX, scaleY);
        mMatrix.setTranslate(dx, dy);
        requestLayout();
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        mMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    public final float getTranslateX() {
        mMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_X];
    }

    public final float getTranslateY() {
        mMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_Y];
    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getMeasuredWidth();
        final float viewHeight = getMeasuredHeight();
        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > 0 && isCheckTopAndBottom) {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom) {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight) {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight) {
            deltaX = viewWidth - rect.right;
        }
        mMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }


}
