package me.hiten.grouppagersnaphelper;


import java.util.ArrayList;
import java.util.List;

public class ScrollEventAdapter {


    private int position = -1;

    public ScrollEventAdapter() {

    }

    private List<OnPageChangeCallback> mPageChangeCallbacks = new ArrayList<>();

    public void addOnPageChangeCallback(OnPageChangeCallback onPageChangeCallback) {
        this.mPageChangeCallbacks.add(onPageChangeCallback);
        if (onPageChangeCallback != null && position > -1) {
            onPageChangeCallback.onPageSelected(position);
        }
    }

    public void removeOnPageChangeCallback(OnPageChangeCallback onPageChangeCallback) {
        this.mPageChangeCallbacks.remove(onPageChangeCallback);
    }


    public int getPosition() {
        return position;
    }

    public void dispatchPageSelected(int position) {
        if (this.position == position) {
            return;
        }
        this.position = position;
        for (OnPageChangeCallback onPageChangeCallback : mPageChangeCallbacks) {
            onPageChangeCallback.onPageSelected(position);
        }
        clearHistory();
    }

    public void updatePageScrolled() {

    }

    private void clearHistory() {
        mOffsetPixels = 0;
    }

    private int mOffsetPixels;

    public void updatePageScrolled(int position, int dOffset, int totalSpace) {
        mOffsetPixels += dOffset;
        float offsetPercent = 1.0f*mOffsetPixels/totalSpace;
        if (offsetPercent<0){
            offsetPercent = 1 + offsetPercent;
        }
        if (mOffsetPixels>0){
            position = this.position;
        }
        for (OnPageChangeCallback onPageChangeCallback : mPageChangeCallbacks) {
            onPageChangeCallback.onPageScrolled(position, offsetPercent, mOffsetPixels);
        }
    }
}
