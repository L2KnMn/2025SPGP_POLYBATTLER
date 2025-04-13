package kr.ac.tukorea.ge.lkm.polybattler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class GameView extends View implements Choreographer.FrameCallback {
    private static final String TAG = GameView.class.getSimpleName();
    private final Matrix transformMatrix = new Matrix();
    private final Matrix invertedMatrix = new Matrix();
    private final float[] pointsBuffer = new float[2];
    private final ArrayList<IGameObject> gameObjects = new ArrayList<>();
    private static long previousNanos;
    public static float frameTime;
    private Bitmap backgroundImage;
    private RectF backgroundRect;
    private Boardmap boardmap;
    private Shop shop;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 실질적 생성자 역할
        Resources res = getResources();
        backgroundImage = BitmapFactory.decodeResource(res, R.mipmap.game_background);
        //backgroundRect = new RectF(0, 0, Metrics.SCREEN_WIDTH, Metrics.SCREEN_HEIGHT);

        boardmap = new Boardmap();
        gameObjects.add(boardmap);
        shop = new Shop();
        gameObjects.add(shop);

        //float size = boardmap.getTileSize() / 2;
//        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
//        polyman.transform.setSize(size);
//        boardmap.setObjectOnTile(polyman.transform, 1, 6);
//        gameObjects.add(polyman);
//
//        Polyman polyman2 = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
//        polyman2.transform.setSize(size);
//        boardmap.setObjectOnTile(polyman2.transform, 2, 5);
//        gameObjects.add(polyman2);
//
//        Polyman polyman3 = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
//        polyman3.transform.setSize(size);
//        boardmap.setObjectOnTile(polyman3.transform, 3, 5);
//        gameObjects.add(polyman3);

        scheduleUpdate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float view_ratio = (float)w / (float)h;
        float game_ratio = Metrics.SCREEN_WIDTH / Metrics.SCREEN_HEIGHT;

        backgroundRect = new RectF(0, 0, w, h);
        transformMatrix.reset();
        if (view_ratio > game_ratio) {
            float scale = h / Metrics.SCREEN_HEIGHT;
            transformMatrix.preTranslate((w - h * game_ratio) / 2, 0);
            transformMatrix.preScale(scale, scale);
        } else {
            float scale = w / Metrics.SCREEN_WIDTH;
            transformMatrix.preTranslate(0, (h - w / game_ratio) / 2);
            transformMatrix.preScale(scale, scale);
        }
        transformMatrix.invert(invertedMatrix);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        // 가장 밑에 바탕 그림 그린다
        canvas.drawBitmap(backgroundImage, null, backgroundRect, null);
        canvas.setMatrix(transformMatrix);
        // 반드시 성공적인 빌드가 진행된 후에 BuildConfig.java 가 생성되므로
        // 아래 코드가 문제가 되면 잠시 삭제해서 빌드만 성공시키고 다시 살려두어도 된다.
        if (BuildConfig.DEBUG) {
            drawDebugBackground(canvas);
        }
        for (IGameObject gobj : gameObjects) {
            gobj.draw(canvas);
        }
        canvas.restore();
        if (BuildConfig.DEBUG) {
            drawDebugInfo(canvas);
        }
    }

    private int origin_width = -1, origin_height = -1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointsBuffer[0] = event.getX();
        pointsBuffer[1] = event.getY();
        invertedMatrix.mapPoints(pointsBuffer);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // Log.d(TAG, "Event=" + event.getAction() + " x=" + pointsBuffer[0] + " y=" + pointsBuffer[1]);
            // check the clicked object
            boardmap.setOnPredictPoint(pointsBuffer[0], pointsBuffer[1]);
            return true;
        case MotionEvent.ACTION_UP:
            boardmap.setOffPredictPoint(pointsBuffer[0], pointsBuffer[1]);
            return true;
        case MotionEvent.ACTION_MOVE:
            // Log.d(TAG, "Event=" + event.getAction());
            boardmap.movePredictPoint(pointsBuffer[0], pointsBuffer[1]);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void scheduleUpdate() {
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long nanos) {
        //Log.d(TAG, "Nanos = " + nanos + " frameTime=" + frameTime);
        if (previousNanos != 0) {
            frameTime = (nanos - previousNanos) / 1_000_000_000f;
            update();
            invalidate();
        }
        previousNanos = nanos;
        if (isShown()) {
            scheduleUpdate();
        }
    };

    private void update() {
        for (IGameObject gobj : gameObjects) {
            gobj.update();
        }
    }

    private RectF borderRect;
    private Paint borderPaint, gridPaint, fpsPaint;
    private void drawDebugBackground(@NonNull Canvas canvas) {
        if (borderRect == null) {
            borderRect = new RectF(0, 0, Metrics.SCREEN_WIDTH, Metrics.SCREEN_HEIGHT);

            borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(0.1f);
            borderPaint.setColor(Color.RED);

            gridPaint = new Paint();
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setStrokeWidth(0.01f);
            gridPaint.setColor(Color.GRAY);
        }

        canvas.drawRect(borderRect, borderPaint);
        for (float x = 1.0f; x < Metrics.SCREEN_WIDTH; x += 1.0f) {
            canvas.drawLine(x, 0, x, Metrics.SCREEN_HEIGHT, gridPaint);
        }
        for (float y = 1.0f; y < Metrics.SCREEN_HEIGHT; y += 1.0f) {
            canvas.drawLine(0, y, Metrics.SCREEN_WIDTH, y, gridPaint);
        }
    }
    private void drawDebugInfo(Canvas canvas) {
        if (fpsPaint == null) {
            fpsPaint = new Paint();
            fpsPaint.setColor(Color.BLUE);
            fpsPaint.setTextSize(100f);
        }

        int fps = (int) (1.0f / frameTime);
        canvas.drawText("FPS: " + fps, 100f, 200f, fpsPaint);
    }
}
