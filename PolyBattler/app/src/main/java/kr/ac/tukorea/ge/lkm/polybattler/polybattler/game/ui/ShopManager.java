package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ShopManager implements IGameManager {
    private static ShopManager instance;
    private final Shop shop;

    public ShopManager() {
        shop = new Shop();
    }

    public static ShopManager getInstance() {
        if (instance == null) {
            instance = new ShopManager();
        }
        return instance;
    }

    public void setGameState(GameState newState){
        switch (newState){
         case PREPARE:
             shop.setActive(true);
             break;
         case SHOPPING:
             shop.setActive(true);
             shop.openShop();
             break;
        case BATTLE:
        case RESULT:
        case POST_GAME:
            shop.foldShop();
            shop.setActive(false);
            break;
        }
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

        shop.onTouch(event, x, y);

        return true;
    }
}
