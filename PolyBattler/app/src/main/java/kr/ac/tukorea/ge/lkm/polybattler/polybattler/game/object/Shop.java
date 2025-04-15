package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class Shop implements IGameObject {
    private boolean active;
    private boolean fold;
    private final RectF IconRect;
    private float textOffsetX, textOffsetY;
    private final Paint shopTextPaint;
    private final RectF backboardRect;
    private final Paint backboardPaint;
    private final RectF boxOutline;
    private final Paint boxOutlinePaint;
    private final int numberOfBox = 3;
    private final Position interlude;

    public Shop() {
        active = true;
        fold = true;

        IconRect = new RectF(0, 0, Metrics.GRID_UNIT, Metrics.GRID_UNIT);
        IconRect.offsetTo(Metrics.width - Metrics.GRID_UNIT, 0);

        shopTextPaint = new Paint();
        shopTextPaint.setColor(0xffffffff);
        shopTextPaint.setTextSize(Metrics.GRID_UNIT*0.25f);

        backboardRect = new RectF(0, 0, Metrics.width, Metrics.height / 2);
        backboardPaint = new Paint();
        backboardPaint.setColor(0x33000000);

        boxOutline = new RectF(0, 0, Metrics.GRID_UNIT * 2.0f, Metrics.GRID_UNIT * 2.5f);
        boxOutlinePaint = new Paint();
        boxOutlinePaint.setColor(0x88aa0000); // 나중에 레벨이나 상품 별로 다른 외곽선으로 직관적 구분할까?
        boxOutlinePaint.setStyle(Paint.Style.STROKE);
        boxOutlinePaint.setStrokeWidth(Metrics.GRID_UNIT / 5);

        interlude = new Position();
        interlude.x = (backboardRect.width() - boxOutline.width() * numberOfBox) / (numberOfBox + 1);
        interlude.y = (backboardRect.height() - boxOutline.height()) / 2;

        float textWidth = shopTextPaint.measureText("SHOP");
        Paint.FontMetrics fontMetrics = shopTextPaint.getFontMetrics();
        this.textOffsetX = -textWidth / 2;
        this.textOffsetY = -(fontMetrics.ascent + fontMetrics.descent) / 2;
    }

    @Override
    public void update() {
        return;
    }

    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        if (active) {
            if (fold) {
                canvas.drawBitmap(BitmapPool.get(R.mipmap.icon_shop), null, IconRect, null);
                canvas.drawRect(IconRect, backboardPaint);
                canvas.drawText("SHOP", IconRect.centerX() + textOffsetX, IconRect.centerY() + textOffsetY, shopTextPaint);
            } else {
                canvas.drawRect(backboardRect, backboardPaint);
                boxOutline.offsetTo(backboardRect.left + interlude.x, backboardRect.top + interlude.y);
                for (int i = 0; i < numberOfBox; i++) {
                    canvas.drawRect(boxOutline, boxOutlinePaint);
                    boxOutline.offset(boxOutline.width() + interlude.x, 0);
                }
            }
        }
    }

    public IGameObject onTouch(MotionEvent event, float x, float y) {
        if (!active) { return null; }
        if(fold){
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (IconRect.contains(x, y)) {
                    fold = false;
                    return null;
                }
            }
        }else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                boxOutline.offsetTo(backboardRect.left + interlude.x, backboardRect.top + interlude.y);
                for (int i = 0; i < numberOfBox; i++) {
                    if (boxOutline.contains(x, y)) {
                        fold = true;
                        Log.d("Shop", "clicked box no." + (i + 1));

                        active = false;
                        return null;
                    }
                    boxOutline.offset(boxOutline.width() + interlude.x, 0);
                }
            }
        }
        return null;
    }

    public void setActive(boolean b) {
        this.active = b;
    }
    public boolean isActive(){
        return this.active;
    }

    public boolean isFold(){
        return this.fold;
    }
}
