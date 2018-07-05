package com.example.ccaucott.surfaceviewexample;

public class FlashlightCone {
    private int mX, mY, mRadius;

    public int getRadius() {
        return mRadius;
    }

    public int getY() {
        return mY;
    }

    public int getX() {
        return mX;
    }

    public FlashlightCone(int viewWidth, int viewHeight){
        mX = viewWidth / 2;
        mY = viewHeight / 2;
        // Adjust the radius for the narrowest view dimension.
        mRadius = ((viewWidth <= viewHeight) ? mX / 3 : mY / 3);
    }

    public void update(int x, int y) {
        mX = x;
        mY = y;
    }
}
