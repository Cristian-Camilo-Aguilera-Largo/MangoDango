package com.example.mangodango.data;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;
public class CustomNestedScrollView extends NestedScrollView {
    public CustomNestedScrollView(Context context) {
        super(context);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // No interceptar toques cuando sean del fragmento de mapa
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
