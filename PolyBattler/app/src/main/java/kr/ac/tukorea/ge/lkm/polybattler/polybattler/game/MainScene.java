package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
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
    private static final String TAG = "MainScene";
    private final Bitmap backgroundImage;
    private final Map map;
    private GameState currentState;
    private ArrayList<IGameManager> managerArray;
    DragAndDropManager dragAndDropManager;

    public MainScene() {
        Metrics.setGameSize(700, 1600);
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;
        backgroundImage = BitmapPool.get(R.mipmap.game_background);

        GameManager.getInstance().setGameState(GameState.PREPARE);

        map = new Map(4, 7, 5);
        add(map);

        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
        map.setObjectOnTile(polyman.transform, 1, 6);
        add(polyman);

        Polyman polyman2 = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
        map.setObjectOnTile(polyman2.transform, 2, 5);
        add(polyman2);

        Polyman polyman3 = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
        map.setObjectOnTile(polyman3.transform, 3, 5);
        add(polyman3);
        
        currentState = GameState.PREPARE;
        setManagers();
        add(ShopManager.getInstance().getShop());
    }

    public void setManagers(){
        managerArray = new ArrayList<IGameManager>();
        dragAndDropManager = new DragAndDropManager(map);

        dragAndDropManager.setGameState(currentState);
        GameManager.getInstance().setGameState(currentState);
        ShopManager.getInstance().setGameState(currentState);

        managerArray.add(ShopManager.getInstance());
        managerArray.add(GameManager.getInstance());
        managerArray.add(dragAndDropManager);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(backgroundImage, null, Metrics.screenRect, null);
        super.draw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean keep = true;
        for (IGameManager manager : managerArray) {
            keep = manager.onTouch(event);
            if(manager.getGameState() != currentState){
                Log.d(TAG, "state changed");
                currentState = manager.getGameState();
                for (IGameManager manager2 : managerArray) {
                    manager2.setGameState(currentState);
                }
                break;
            }
            if (!keep) break;
        }
        return true;
    }
}
