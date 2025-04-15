package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ColorType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Map;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ShapeType;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.DragAndDropManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.ShopManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;

public class MainScene extends Scene {
    private final Bitmap backgroundImage;
    private final Map map;
    private IGameObject purchasedObject;
    private ArrayList<IGameManager> managerArray;
    DragAndDropManager dragAndDropManager;

    public MainScene() {
        Metrics.setGameSize(700, 1600);
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;
        backgroundImage = BitmapPool.get(R.mipmap.game_background);

        GameManager.getInstance().setGameState(GameState.PREPARE);

        map = new Map(4, 7, 5);
        gameObjects.add(map);

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

        setManagers(GameState.PREPARE);
    }

    public void setManagers(GameState state){
        managerArray = new ArrayList<IGameManager>();
        dragAndDropManager = new DragAndDropManager(map);
        GameManager.getInstance().setGameState(state);
        ShopManager.getInstance().setGameState(state);
        managerArray.add(dragAndDropManager);
        managerArray.add(GameManager.getInstance());
        managerArray.add(ShopManager.getInstance());
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(backgroundImage, null, Metrics.screenRect, null);
        for (IGameObject gobj : gameObjects) {
            gobj.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean keep = true;
        for (IGameManager manager : managerArray) {
            keep = manager.onTouch(event);
            if (!keep) break;
        }
        return true;
    }
}
