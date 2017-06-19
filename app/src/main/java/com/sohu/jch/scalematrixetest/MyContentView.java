package com.sohu.jch.scalematrixetest;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;

/**
 * Created by jch on 2017/6/14.
 */

public class MyContentView extends AppCompatTextView implements GestureView.VideoChangeEvents {

    private static final String TAG = "MyContentView";


    public MyContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Log.d(TAG, "onLayout ");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "onMeasure");
    }


    @Override
    public void videoScale(float scaleX, float scaleY) {

        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    @Override
    public void videoTranslate(float translateX, float translateY) {
        setTranslationX(translateX);
        setTranslationY(translateY);
    }

    @Override
    public void videoChangeSize(int left, int top, int right, int bottom) {

    }
}
