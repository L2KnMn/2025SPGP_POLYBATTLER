package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class Goods implements IGameObject {
    private final Paint paint;
    public int level;
    public int price;
    public Polyman.ShapeType shape;
    public Polyman.ColorType color;
    public Transform transform;
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
        Polyman.drawLevel(canvas, transform, level);
    }

    @Override
    public void update(){}
}