package kr.ac.tukorea.ge.lkm.polybattler;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.ArrayList;

public class MainScene extends Scene {
    private Bitmap backgroundImage;
    private RectF backgroundRect;
    private Boardmap boardmap;
    private Shop shop;

    public MainScene(GameView gameView) {
        Resources res = getResources();
        backgroundImage = BitmapFactory.decodeResource(res, R.mipmap.game_background);
        backgroundRect = new RectF(0, 0, 1, 1);

        boardmap = new Boardmap();
        gameObjects.add(boardmap);
        shop = new Shop();
        gameObjects.add(shop);

        float size = boardmap.getTileSize() / 2;
        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
        polyman.transform.setSize(size);
        boardmap.setObjectOnTile(polyman.transform, 1, 6);
        gameObjects.add(polyman);

        Polyman polyman2 = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
        polyman2.transform.setSize(size);
        boardmap.setObjectOnTile(polyman2.transform, 2, 5);
        gameObjects.add(polyman2);

        Polyman polyman3 = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
        polyman3.transform.setSize(size);
        boardmap.setObjectOnTile(polyman3.transform, 3, 5);
        gameObjects.add(polyman3);

    }

    public void update() {
        for (IGameObject gobj : gameObjects) {
            gobj.update();
        }
    }
    public void draw(Canvas canvas) {
        for (IGameObject gobj : gameObjects) {
            gobj.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float[] xy = Metrics.fromScreen(event.getX(), event.getY());
                fighter.setTargetPosition(xy[0], xy[1]);
                return true;
        }
        return false;
    }
}
