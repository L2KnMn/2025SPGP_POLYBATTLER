package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ColorType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Map;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ShapeType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.DragAndDropManager;

public class MainScene extends Scene {
    private final Bitmap backgroundImage;
    private final Map map;
    private final Shop shop;
    private final DragAndDropManager dragAndDropManager;
    private IGameObject purchasedObject;

    public MainScene() {
        Metrics.setGameSize(700, 1600);

        GameView.drawsDebugStuffs = BuildConfig.DEBUG;

        backgroundImage = BitmapPool.get(R.mipmap.game_background);

        map = new Map(4, 7, 5);
        gameObjects.add(map);
        shop = new Shop();
        gameObjects.add(shop);

        dragAndDropManager = new DragAndDropManager(map);

        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
        map.setObjectOnTile(polyman.transform, 1, 6);
        gameObjects.add(polyman);

        Polyman polyman2 = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
        map.setObjectOnTile(polyman2.transform, 2, 5);
        gameObjects.add(polyman2);

        Polyman polyman3 = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
        map.setObjectOnTile(polyman3.transform, 3, 5);
        gameObjects.add(polyman3);

        purchasedObject = null;
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
//        // Shop 처리
//        if (shop.isActive() && shop.onTouch(event, xy[0], xy[1]) != null) {
//            return true;
//        }
        // DragAndDropManager 처리
        if (dragAndDropManager.handleTouchEvent(event)) {
            return true;
        }
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                // Log.d(TAG, "Event=" + event.getAction() + " x=" + pointsBuffer[0] + " y=" + pointsBuffer[1]);
//                // check the clicked object
//                purchasedObject = shop.onTouch(event, xy[0], xy[1]);
//                if(purchasedObject != null)
//                    return true;
//                if(shop.isFold()){
//                    purchasedObject = map.setOnPredictPoint(xy[0], xy[1]);
//                }
//                return true;
//            case MotionEvent.ACTION_UP:
//                map.setOffPredictPoint(xy[0], xy[1]);
//                if(!map.isFloatObjectOn())
//                    purchasedObject = null;
//                if(!shop.isActive())
//                    shop.setActive(true);
//                return true;
//            case MotionEvent.ACTION_MOVE:
//                // Log.d(TAG, "Event=" + event.getAction());
//                map.movePredictPoint(xy[0], xy[1]);
//                return true;
//        }
        return true;
    }
}
