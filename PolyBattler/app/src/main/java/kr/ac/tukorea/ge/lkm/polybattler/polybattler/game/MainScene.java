package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop.ShopManager;

public class MainScene extends Scene {
    private static final String TAG = "MainScene";
    private final Bitmap backgroundImage;
    private GameState currentState;
    private ArrayList<IGameManager> managerArray;

    public MainScene() {
        Metrics.setGameSize(700, 1600);
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;
        backgroundImage = BitmapPool.get(R.mipmap.game_background);
        currentState = GameState.PREPARE;
        setManagers();
        add(ShopManager.getInstance(this).getShop());
    }

    public void setManagers(){
        managerArray = new ArrayList<IGameManager>();
        GameManager.getInstance(this).setGameState(currentState);
        ShopManager.getInstance(this).setGameState(currentState);
        UiManager.getInstance(this).setGameState(currentState);

        managerArray.add(UiManager.getInstance(this));
        managerArray.add(ShopManager.getInstance(this));
        managerArray.add(GameManager.getInstance(this));
        managerArray.add(EffectManager.getInstance(this));
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(backgroundImage, null, Metrics.screenRect, null);
        super.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean keep;
        for (IGameManager manager : managerArray) {
            keep = !manager.onTouch(event);
            if(manager.getGameState() != currentState){
                currentState = manager.getGameState();
                for (IGameManager manager2 : managerArray) {
                    if (manager2.getGameState() != currentState)
                        manager2.setGameState(currentState);
                }
                break;
            }
            if (!keep) break;
        }
        return true;
    }
}
