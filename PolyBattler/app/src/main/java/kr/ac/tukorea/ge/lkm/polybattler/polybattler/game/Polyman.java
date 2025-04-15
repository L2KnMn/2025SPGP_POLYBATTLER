package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Canvas;
import android.graphics.Paint;

import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;

enum ShapeType {
    RECTANGLE, CIRCLE, TRIANGLE
}

enum ColorType {
    RED, GREEN, BLUE, BLACK
}

public class Polyman implements IGameObject {
    public Transform transform;
    private Paint paint;
    private ShapeType shape;
    private ColorType color;
    private boolean availible;

    public Polyman(ShapeType shape, ColorType color) {
        this.shape = shape;
        this.color = color;
        transform = new Transform(this, 0, 0);
        transform.setRigid(true);
        paint = new Paint();
        paint.setColor(getColor());
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void update() {
        // Polyman의 업데이트 로직
        transform.turnLeft(0.01f);
    }

    @Override
    public void draw(Canvas canvas) {
        switch (shape){
            case RECTANGLE:
                canvas.drawRect(transform.getRect(), paint);
                break;
            case CIRCLE:
                canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, transform.getSize(), paint);
                break;
            case TRIANGLE:
                canvas.drawPath(transform.getTriangle(), paint);
                break;
            default:
                break;
        }
        //canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, transform.getSize(), paint);
    }

    private int getColor(){
        switch (color) {
            case RED:
                return 0xFFFF0000;
            case GREEN:
                return 0xFF00FF00;
            case BLUE:
                return 0xFF0000FF;
            case BLACK:
                return 0xFF000000;
            default:
                return 0xFFFF0000;
        }
    }
    public boolean inPoint(Position point){
        switch (shape){
            case RECTANGLE:
                if(transform.getRect().contains(point.x, point.y)){
                    return true;
                }
            case CIRCLE:
                if(transform.distance(point.x, point.y) < transform.getSize()){
                    return true;
                }
            case TRIANGLE:
                if(transform.isPointInTriangle(point)){
                    return true;
                }
            default:
                return false;
        }
    }
}
