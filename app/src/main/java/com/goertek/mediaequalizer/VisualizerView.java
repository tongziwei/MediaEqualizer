package com.goertek.mediaequalizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by clara.tong on 2020/9/16
 */
public class VisualizerView extends View {
    private byte[] mBytes;
    private float[] mPoints;
    // 矩形区域
    private Rect mRect = new Rect();
    // 画笔
    private Paint mPaint = new Paint();

    // 初始化画笔
    private void init()
    {
        mBytes = null;
        mPaint.setStrokeWidth(1f);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
    }

    public VisualizerView(Context context)
    {
        super(context);
        init();
    }
    //mBytes就是采集来的数据 这里是个大小为1024的数组，里面的数据都是byts类型，所以大小为-127到128
    public void updateVisualizer(byte[] mbyte)
    {
        mBytes = mbyte;
        //			for (int i=0;i<mbyte.length;i++){
        //				int x=mbyte[i];
        //				Log.d(TAG, "responsed: " +x);
        //			}

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        if (mBytes == null)
        {


            return;
        }
        if (mPoints == null || mPoints.length < mBytes.length * 4)
        {
            mPoints = new float[mBytes.length * 4];



        }

        mRect.set(0, 0, getWidth(), getHeight());

        for (int i = 0; i < mBytes.length - 1; i++)
        {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2
                    + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2)
                    / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1)
                    / (mBytes.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2
                    + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2)
                    / 128;
        }

        canvas.drawLines(mPoints, mPaint);

    }

}
