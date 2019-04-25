package me.relex.circleindicator;

import android.content.Context;
import android.util.AttributeSet;

public class CommonCircleIndicator extends BaseCircleIndicator{
    public CommonCircleIndicator(Context context) {
        super(context);
    }

    public CommonCircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonCircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void createIndicators(int count, int currentPosition) {
       super.createIndicators(count,currentPosition);
    }

    public void setPageSelected(int position) {
        if (position == mLastPosition){
            return;
        }
        super.internalPageSelected(position);
        mLastPosition = position;
    }
}
