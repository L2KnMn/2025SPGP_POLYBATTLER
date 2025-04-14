package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.lkm.polybattler.framework.util.RectUtil;
import kr.ac.tukorea.ge.lkm.polybattler.framework.view.Metrics;

public class Shop implements IGameObject {
    boolean active;
    private final RectF backboard;
    private final Paint backboardPaint;
    private final RectF boxOutline;
    private final Paint boxOutlinePaint;
    private final int numberOfBox = 3;
    private final Position interlude;

    public Shop() {
        active = true;

        backboard = new RectF(0, 0, Metrics.width, Metrics.height / 2);
        backboardPaint = new Paint();
        backboardPaint.setColor(0x88000000);

        boxOutline = new RectF(0, 0, Metrics.GRID_UNIT * 2.0f, Metrics.GRID_UNIT * 2.5f);
        boxOutlinePaint = new Paint();
        boxOutlinePaint.setColor(0x88aa0000); // 나중에 레벨이나 상품 별로 다른 외곽선으로 직관적 구분할까?
        boxOutlinePaint.setStyle(Paint.Style.STROKE);
        boxOutlinePaint.setStrokeWidth(Metrics.GRID_UNIT / 5);

        interlude = new Position();
        interlude.x = (backboard.width() - boxOutline.width() * numberOfBox) / (numberOfBox + 1);
        interlude.y = (backboard.height() - boxOutline.height()) / 2;
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

            boxOutline.offsetTo(backboard.left + interlude.x, backboard.top + interlude.y);
            for (int i = 0; i < numberOfBox; i++) {
                canvas.drawRect(boxOutline, boxOutlinePaint);
                boxOutline.offset(boxOutline.width() + interlude.x, 0);
            }
        }
    }

    public void SetActive(boolean b) {
        this.active = b;
    }
}
