package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.tv.TableRequest;
import android.util.Log;

import java.util.Random;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MasterManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
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
    static class Goods implements IGameObject {
        private final Paint paint;
        private int level;
        protected int price;
        protected Polyman.ShapeType shape;
        protected Polyman.ColorType color;
        protected Transform transform;
        protected boolean soldOut;

        Goods(int price, Polyman.ShapeType shape, Polyman.ColorType color, float x, float y){
            this.price = price;
            this.shape = shape;
            this.color = color;
            this.soldOut = false;

            this.level = 1;

            transform = new Transform(this, new Position(x, y));
            transform.setSize(Metrics.GRID_UNIT);

            paint = new Paint();
            paint.setColor(Polyman.getColor(color));
            paint.setStyle(Paint.Style.FILL);
        }

        int getRandomLevel(Random random){
            int rand = random.nextInt(100);
            if(rand <= 1){
                return 3;
            }else if(rand <= 9){
                return 2;
            }else {
                return 1;
            }
        }

        void reset(Random random){
            this.soldOut = false;
            this.price = random.nextInt(10);
            this.shape = Polyman.ShapeType.values()[random.nextInt(Polyman.ShapeType.values().length)];
            this.color = Polyman.ColorType.values()[random.nextInt(Polyman.ColorType.values().length-1)];
            this.paint.setColor(Polyman.getColor(color));
            this.level = getRandomLevel(random);
        }

        @Override
        public void draw(Canvas canvas){
            Polyman.drawShape(canvas, transform, paint, shape);
        }

        @Override
        public void update(){}
    }

    private final int numberOfBox = 3;
    private final Goods[] goods = new Goods[numberOfBox];
    private final Position interlude;
    private final RectF RerollButtonRect;
//    private final Paint RerollButtonPaint;
    private int rerollPrice = 1;


    public Shop() {
        active = true;
        fold = true;

        IconRect = new RectF(0, 0, Metrics.GRID_UNIT, Metrics.GRID_UNIT);
        IconRect.offsetTo(Metrics.width - Metrics.GRID_UNIT, 0);

        shopTextPaint = new Paint();
        shopTextPaint.setColor(0xffffffff);
        shopTextPaint.setTextSize(Metrics.GRID_UNIT*0.25f);

        backboardRect = new RectF(0, Metrics.GRID_UNIT, Metrics.width, Metrics.height / 2);
        backboardPaint = new Paint();
        backboardPaint.setColor(0x33000000);

        boxOutline = new RectF(0, 0, Metrics.GRID_UNIT * 2.0f, Metrics.GRID_UNIT * 2.5f);
        boxOutlinePaint = new Paint();
        boxOutlinePaint.setColor(0x88FFFFFF); // 나중에 레벨이나 상품 별로 다른 외곽선으로 직관적 구분할까?
        boxOutlinePaint.setStyle(Paint.Style.STROKE);
        boxOutlinePaint.setStrokeWidth(Metrics.GRID_UNIT / 5);

        RerollButtonRect = new RectF(backboardRect.width()/2-Metrics.GRID_UNIT,
                backboardRect.height() - Metrics.GRID_UNIT, backboardRect.width()/2 + Metrics.GRID_UNIT,
                backboardRect.height());
//        RerollButtonPaint = new Paint();
//        RerollButtonPaint.setColor(0xFF000000);
//        RerollButtonPaint.setStyle(Paint.Style.FILL);

        interlude = new Position();
        interlude.x = (backboardRect.width() - boxOutline.width() * numberOfBox) / (numberOfBox + 1);
        interlude.y = (backboardRect.height() - boxOutline.height()) / 2;

        for(int i = 0; i < numberOfBox; i++){
            goods[i] = new Goods(1, Polyman.ShapeType.CIRCLE, Polyman.ColorType.RED,
                    backboardRect.left + interlude.x + boxOutline.width() / 2 + (boxOutline.width() + interlude.x) * i,
                    backboardRect.top + interlude.y + boxOutline.height() / 2);
        }
        setRandomGoods(1);
    }

    public void createUI(Scene scene){
        UiManager ui = UiManager.getInstance(scene);
        ui.addButton(R.mipmap.icon_shop,"SHOP", IconRect.centerX(), IconRect.centerY(), IconRect.width(), IconRect.height(),
                () -> {
                    openShop();
                    MasterManager.getInstance(scene).setGameState(GameState.SHOPPING);
                }
            ).setVisibility(GameState.PREPARE, true);
        UiManager.Button rerollButton = ui.addButton("REROLL", RerollButtonRect.centerX(), RerollButtonRect.centerY(), RerollButtonRect.width(), RerollButtonRect.height(),
                () -> {
                    ShopManager.getInstance(scene).reRoll(rerollPrice);
                    rerollPrice++;
                }
        ).setVisibility(GameState.SHOPPING, true);
    }

    @Override
    public void update() {}

    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        if (active) {
            if (!fold) {
                canvas.drawRect(backboardRect, backboardPaint);
                boxOutline.offsetTo(backboardRect.left + interlude.x, backboardRect.top + interlude.y);
                for (int i = 0; i < numberOfBox; i++) {
                    if(!goods[i].soldOut){
                        canvas.drawRect(boxOutline, boxOutlinePaint);
                        goods[i].draw(canvas);
                        drawText(canvas,goods[i].shape.name(), boxOutline.centerX(), boxOutline.centerY() - boxOutline.height() / 4);
                        drawText(canvas, String.valueOf(goods[i].price), boxOutline.centerX(), boxOutline.centerY() + boxOutline.height() / 4);
                    }
                    boxOutline.offset(boxOutline.width() + interlude.x, 0);
                }
                // canvas.drawRect(RerollButtonRect, RerollButtonPaint);
                float height_offset = RerollButtonRect.height() / 3;
                drawText(canvas, "price : " + rerollPrice, RerollButtonRect.centerX(), RerollButtonRect.centerY() + height_offset);
            }
        }
    }

    private void drawText(Canvas canvas, String string, float x, float y) {
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
    public void setRandomGoods(int price){
        rerollPrice = price;
        for (int i = 0; i < numberOfBox; i++) {
            goods[i].reset(random);
        }
    }

    public int getPrice(int index){
        return this.goods[index].price;
    }
    public Polyman.ShapeType getShape(int index){
        return this.goods[index].shape;
    }
    public Polyman.ColorType getColor(int index){
        return this.goods[index].color;
    }

    public boolean isSoldOut(int goods){
        return this.goods[goods].soldOut;
    }

    public void makeSoldOut(int goods){
        this.goods[goods].soldOut = true;
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
