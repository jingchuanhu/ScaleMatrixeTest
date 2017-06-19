package com.sohu.jch.scalematrixetest;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by jch on 2017/6/14.
 */

public class MoveGestureDetector extends GestureDetector {


    public MoveGestureDetector(OnGestureListener listener, Handler handler) {
        super(listener, handler);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        return super.onTouchEvent(ev);
    }
}
