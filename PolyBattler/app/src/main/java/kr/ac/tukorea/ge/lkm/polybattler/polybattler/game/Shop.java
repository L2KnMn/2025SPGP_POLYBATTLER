package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.lkm.polybattler.framework.view.Metrics;

public class Shop implements IGameObject {
    boolean active;
    private final RectF backboard;
    private final Paint backboardPaint;
    public Shop() {
        active = true;

        backboard = new RectF();
        backboard.left = 0;
        backboard.top = 0;
        backboard.right = Metrics.width;
        backboard.bottom = Metrics.height / 2;

        backboardPaint = new Paint();
        backboardPaint.setColor(0xa0000000);
    }

    @Override
    public void update() {
        // 업데이트 로직
//        if (active) {
//
//        }
    }
    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        if (active) {
            canvas.drawRect(backboard, backboardPaint);
        }
    }

    public void SetActive(boolean b) {
        this.active = b;
    }
}
