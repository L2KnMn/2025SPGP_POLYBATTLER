package kr.ac.tukorea.ge.lkm.polybattler;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.graphics.RectF;

public class MainScene extends Scene {
    private final Bitmap backgroundImage;
    private final Map map;
    private final Shop shop;

    public MainScene(GameView gameView) {
        Metrics.setGameSize(700, 1600);
        Resources res = gameView.getResources();
        backgroundImage = BitmapFactory.decodeResource(res, R.mipmap.game_background);

        map = new Map();
        gameObjects.add(map);
        shop = new Shop();
        gameObjects.add(shop);

        float size = map.getTileSize() / 2;
        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
        polyman.transform.setSize(size);
        map.setObjectOnTile(polyman.transform, 1, 6);
        gameObjects.add(polyman);

        Polyman polyman2 = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
        polyman2.transform.setSize(size);
        map.setObjectOnTile(polyman2.transform, 2, 5);
        gameObjects.add(polyman2);

        Polyman polyman3 = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
        polyman3.transform.setSize(size);
        map.setObjectOnTile(polyman3.transform, 3, 5);
        gameObjects.add(polyman3);

    }

    public void update() {
        for (IGameObject gobj : gameObjects) {
            gobj.update();
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(backgroundImage, null, Metrics.screenRect, null);
        for (IGameObject gobj : gameObjects) {
            gobj.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Log.d(TAG, "Event=" + event.getAction() + " x=" + pointsBuffer[0] + " y=" + pointsBuffer[1]);
                // check the clicked object
                map.setOnPredictPoint(xy[0], xy[1]);
                shop.SetActive(false);
                return true;
            case MotionEvent.ACTION_UP:
                map.setOffPredictPoint(xy[0], xy[1]);
                shop.SetActive(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                // Log.d(TAG, "Event=" + event.getAction());
                map.movePredictPoint(xy[0], xy[1]);
                return true;
        }
        return false;
    }
}
