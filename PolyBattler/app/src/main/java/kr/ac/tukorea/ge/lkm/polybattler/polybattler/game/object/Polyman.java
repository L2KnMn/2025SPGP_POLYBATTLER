package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.graphics.Canvas;
import android.graphics.Paint;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.Sprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class Polyman extends Sprite {
    public final Transform transform;
    private final Paint paint;
    private final ShapeType shape;
    private final ColorType color;

    public Polyman(ShapeType shape, ColorType color) {
        super(0);
        this.shape = shape;
        this.color = color;
        transform = new Transform(this, 0, 0);
        transform.setSize(Metrics.GRID_UNIT);
        transform.setRigid(true);
        paint = new Paint();
        paint.setColor(getColor());
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void update() {
        transform.turnLeft(30 * GameView.frameTime);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(transform.getAngle(), transform.getPosition().x, transform.getPosition().y);
        switch (shape){
            case RECTANGLE:
                canvas.drawRect(transform.getRect(), paint);
                break;
            case CIRCLE:
                canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, transform.getSize()/2, paint);
                break;
            case TRIANGLE:
                canvas.drawPath(transform.getTriangle(), paint);
                break;
            default:
                break;
        }
        canvas.restore();
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
                return 0xFFFFFFFF;
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

    public enum ShapeType {
        RECTANGLE, CIRCLE, TRIANGLE
    }
    public enum ColorType {
        RED, GREEN, BLUE, BLACK
    }
}
