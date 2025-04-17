package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

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
    class Goods {
        protected int price;
        protected ShapeType shape;
        protected ColorType color;
        protected boolean soldOut;

        Goods(int price, ShapeType shape, ColorType color){
            this.price = price;
            this.shape = shape;
            this.color = color;
            this.soldOut = false;
        }
    }

    enum ShopEvent {
        CLOSE, PURCHASE, REROLL, IGNORE
    }

    private final int numberOfBox = 3;
    private final Goods[] goods = new Goods[numberOfBox];
    private final Position interlude;
    private final RectF RerollButtonRect;
    private final Paint RerollButtonPaint;


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

        RerollButtonRect = new RectF(backboardRect.width()/2-Metrics.GRID_UNIT,
                backboardRect.height() - Metrics.GRID_UNIT * 0.5f, backboardRect.width()/2 + Metrics.GRID_UNIT,
                backboardRect.height() + Metrics.GRID_UNIT * 0.5f);
        RerollButtonPaint = new Paint();
        RerollButtonPaint.setColor(0xFF000000);
        RerollButtonPaint.setStyle(Paint.Style.FILL);

        interlude = new Position();
        interlude.x = (backboardRect.width() - boxOutline.width() * numberOfBox) / (numberOfBox + 1);
        interlude.y = (backboardRect.height() - boxOutline.height()) / 2;

        for(int i = 0; i < numberOfBox; i++){
            goods[i] = new Goods(1, ShapeType.CIRCLE, ColorType.RED);
        }
        setRandomGoods();
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
                drawIconButton(canvas);
            } else {
                canvas.drawRect(backboardRect, backboardPaint);
                boxOutline.offsetTo(backboardRect.left + interlude.x, backboardRect.top + interlude.y);
                for (int i = 0; i < numberOfBox; i++) {
                    if(!goods[i].soldOut){
                        canvas.drawRect(boxOutline, boxOutlinePaint);
                        setTextOffset(goods[i].shape.name());
                        canvas.drawText(goods[i].shape.name(), boxOutline.centerX() + textOffsetX, boxOutline.centerY() + textOffsetY, shopTextPaint);
                        setTextOffset(String.valueOf(goods[i].price));
                        canvas.drawText(String.valueOf(goods[i].price), boxOutline.centerX() + textOffsetX, boxOutline.centerY() + textOffsetY * 2, shopTextPaint);
                    }
                    boxOutline.offset(boxOutline.width() + interlude.x, 0);
                }
                canvas.drawRect(RerollButtonRect, RerollButtonPaint);
                setTextOffset("Reroll");
                canvas.drawText("Reroll", RerollButtonRect.centerX() + textOffsetX, RerollButtonRect.centerY() + textOffsetY, shopTextPaint);
            }
        }
    }

    private void drawText(Canvas canvas, String string, float x, float y) {
        drawText(canvas, string, x, y, Gravity.CENTER);
    }

    private void drawText(Canvas canvas, String string, float x, float y, Gravity gravity) {
        setTextOffset(string);
        canvas.drawText(string, x+textOffsetX, y+textOffsetY, shopTextPaint);
    }

    private void drawIconButton(Canvas canvas) {
        canvas.drawBitmap(BitmapPool.get(R.mipmap.icon_shop), null, IconRect, null);
        canvas.drawRect(IconRect, backboardPaint);
        drawText(canvas, "SHOP", IconRect.centerX(), IconRect.centerY());
    }

    public int purchase(float x, float y) {
        if (!active) { return -1; }
        boxOutline.offsetTo(backboardRect.left + interlude.x, backboardRect.top + interlude.y);
        for (int i = 0; i < numberOfBox; i++) {
            if (boxOutline.contains(x, y)) {
                Log.d("Shop", "clicked box no." + (i + 1));
                return i;
            }
            boxOutline.offset(boxOutline.width() + interlude.x, 0);
        }
        return -1;
    }

    Random random = new Random();
    public void setRandomGoods(){
        for (int i = 0; i < numberOfBox; i++) {
            goods[i].soldOut = false;
            goods[i].price = random.nextInt(10);
            goods[i].shape = ShapeType.values()[random.nextInt(ShapeType.values().length)];
            goods[i].color = ColorType.values()[random.nextInt(ColorType.values().length-1)];
        }
    }

    public int getPrice(int index){
        return this.goods[index].price;
    }
    public ShapeType getShape(int index){
        return this.goods[index].shape;
    }
    public ColorType getColor(int index){
        return this.goods[index].color;
    }

    public boolean soldOut(int goods){
        // already sold out = false
        // sold out now = true
        if(this.goods[goods].soldOut){
            return false;
        }else{
            this.goods[goods].soldOut = true;
            return true;
        }
    }

    public RectF getIconRect(){
        return this.IconRect;
    }
    public RectF getBackboardRect(){
        return this.backboardRect;
    }
    public RectF RerollButtonRect(){
        return this.RerollButtonRect;
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

    public void foldShop(){
        fold = true;
    }
    public void openShop(){
        fold = false;
    }

    private void setTextOffset(String string){
        float textWidth = shopTextPaint.measureText(string);
        Paint.FontMetrics fontMetrics = shopTextPaint.getFontMetrics();
        this.textOffsetX = -textWidth / 2;
        this.textOffsetY = -(fontMetrics.ascent + fontMetrics.descent) / 2;
    }
}
