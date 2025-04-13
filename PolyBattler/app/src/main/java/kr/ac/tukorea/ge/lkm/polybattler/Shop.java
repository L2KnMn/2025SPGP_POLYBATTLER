package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class Shop implements IGameObject {
    boolean active;
    private RectF backboard;
    private Paint backboardPaint;
    public Shop() {
        active = true;

        backboard = new RectF();
        backboard.left = 0;
        backboard.top = 0;
        backboard.right = Metrics.SCREEN_WIDTH;
        backboard.bottom = Metrics.SCREEN_HEIGHT / 2;

        backboardPaint = new Paint();
        backboardPaint.setColor(0xa0000000);
    }
    @Override
    public boolean isActive() {
        return active;
    }
    @Override
    public void SetActive(boolean active) {
        this.active = active;
    }
    @Override
    public void update() {
        // 업데이트 로직
        if (active) {

        }
    }
    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        if (active) {
            canvas.drawRect(backboard, backboardPaint);
        }else{
            //canvas.drawRect(backboard, backboardPaint);
        }
    }
    @Override
    public Transform getTransform() {
        Log.d("Shop", "call Shop's getTransform() it is error");
        return null;
    }
}
