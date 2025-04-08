package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;

enum ShapeType {
    RECTANGLE, CIRCLE, TRIANGLE
}

enum ColorType {
    RED, GREEN, BLUE, BLACK
}

public class Polyman implements IGameObject {
    private float x, y;
    private float width, height;
    private float speed;
    private ShapeType shape;
    private ColorType color;

    @Override
    public void update() {
        // Polyman의 업데이트 로직
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
