package me.hiten.grouppagersnaphelper.sample;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class AverageMeasure {

    private float mCount;

    private boolean mHorizontal;

    public AverageMeasure(float mCount, boolean mHorizontal) {
        this.mCount = mCount;
        this.mHorizontal = mHorizontal;
    }

    public void onViewHolderCreate(RecyclerView viewGroup, View view) {
        int measuredWidth = viewGroup.getMeasuredWidth();
        int measuredHeight = viewGroup.getMeasuredHeight();
        if (measuredWidth > 0 && mCount > 0) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            if (mHorizontal) {
                measuredWidth = measuredWidth - viewGroup.getPaddingLeft() - viewGroup.getPaddingRight();
//                    if (viewGroup.getClipToPadding()) {
//                        measuredWidth = measuredWidth - viewGroup.getPaddingLeft() - viewGroup.getPaddingRight();
//                    }
                int itemWidth = Math.round((measuredWidth - (layoutParams.leftMargin + layoutParams.rightMargin) * mCount) * 1.0f / mCount);
                if (itemWidth > 0) {
                    layoutParams.width = itemWidth;
//                        layoutParams.height = itemWidth;
                }
            } else {
                measuredHeight = measuredHeight - viewGroup.getPaddingTop() - viewGroup.getPaddingBottom();
//                    if (viewGroup.getClipToPadding()) {
//                        measuredHeight = measuredHeight - viewGroup.getPaddingTop() - viewGroup.getPaddingBottom();
//                    }
                int itemHeight = Math.round((measuredHeight - (layoutParams.topMargin + layoutParams.bottomMargin) * mCount) * 1.0f / mCount);
                if (itemHeight > 0) {
                    layoutParams.height = itemHeight;
//                        layoutParams.width = itemHeight;
                }
            }
        }
    }
}
