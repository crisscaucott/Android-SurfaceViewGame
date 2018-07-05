package com.example.ccaucott.surfaceviewexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    private Context mContext;
    private boolean mRunning;
    private Thread mGameThread;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Path mPath;
    private int mBitmapX;
    private int mBitmapY;
    private int mViewWidth;
    private int mViewHeight;
    private FlashlightCone mFlashlighCone;
    private Bitmap mBitmap;
    private RectF mWinnerRect;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mPaint.setColor(Color.DKGRAY);
        mPath = new Path();
    }

    @Override
    public void run() {
        // The interesting stuff, such as drawing and screen refresh synchronization.
        Canvas canvas;
        while (mRunning){
            // A Surface is only available while the SurfaceView's window is visible.
            // If your SurfaceView is not always visible, implement the surfaceCreated(SurfaceHolder)
            // and surfaceDestroyed(SurfaceHolder) callback methods
            if (mSurfaceHolder.getSurface().isValid()){
                int x = mFlashlighCone.getX();
                int y = mFlashlighCone.getY();
                int radius = mFlashlighCone.getRadius();

                // In an app, with more threads, you must enclose this with a try/catch block to
                // make sure only one thread is trying to write to the Surface.
                canvas = mSurfaceHolder.lockCanvas();
                canvas.save();
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);

                // Add a circle that is the size of the flashlight cone to mPath.
                mPath.addCircle(x, y, radius, Path.Direction.CCW);
                // Set the circle as the clipping path using the DIFFERENCE operator,
                // so that's what's inside the circle is clipped (not drawn).
                canvas.clipPath(mPath, Region.Op.DIFFERENCE);
                // Fill everything outside of the circle with black.
                canvas.drawColor(Color.BLACK);
                // Check whether the the center of the flashlight circle is inside the winning rectangle.
                // If so, color the canvas white, redraw the Android image, and draw the winning message.
                if (x > mWinnerRect.left && x < mWinnerRect.right
                        && y > mWinnerRect.top && y < mWinnerRect.bottom){
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);
                    canvas.drawText("WIN!", mViewWidth / 3, mViewHeight / 2, mPaint);
                }

                // Drawing is finished, so you need to rewind the path, restore the canvas,
                // and release the lock on the canvas.
                mPath.rewind();
                canvas.restore();
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Invalidate() is inside the case statements because there are
        // many other motion events, and we don't want to invalidate
        // the view for those.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setUpBitmap();
                updateFrame((int) x, (int) y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                updateFrame((int) x, (int) y);
                invalidate();
                break;
            default:
                // Do nothing.
        }
        return true;
    }

    private void updateFrame(int x, int y) {
        mFlashlighCone.update(x, y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mFlashlighCone = new FlashlightCone(mViewWidth, mViewHeight);
        // Set the font size proportional to the view height.
        mPaint.setTextSize(mViewHeight / 5);
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android);
        setUpBitmap();
    }

    public void pause() {
        mRunning = false;
        try {
            // Stop the thread
            mGameThread.join();
        }catch (InterruptedException e){

        }

    }

    public void resume() {
        mRunning = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    private void setUpBitmap() {
        mBitmapX = (int) Math.floor(Math.random() * (mViewWidth - mBitmap.getWidth()));
        mBitmapY = (int) Math.floor(Math.random() * (mViewHeight - mBitmap.getHeight()));
        mWinnerRect = new RectF(mBitmapX, mBitmapY,mBitmapX + mBitmap.getWidth(),mBitmapY + mBitmap.getHeight());
    }
}
