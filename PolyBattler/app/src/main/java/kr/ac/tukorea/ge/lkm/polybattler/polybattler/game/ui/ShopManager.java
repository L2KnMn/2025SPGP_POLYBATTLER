package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ShopManager implements IGameManager {
    private static ShopManager instance;
    private final Shop shop;
    private GameState currentState;

    public ShopManager() {
        shop = new Shop();
        currentState = GameState.PREPARE;
    }

    public static ShopManager getInstance() {
        if (instance == null) {
            instance = new ShopManager();
        }
        return instance;
    }

    @Override
    public void setGameState(GameState newState){
        currentState = newState;
        switch (newState){
         case PREPARE:
             shop.setActive(true);
             shop.foldShop();
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

        if (!shop.isActive()) { return true; }
        switch (currentState){
            case PREPARE:
                if(event.getAction() == MotionEvent.ACTION_DOWN
                        && shop.getIconRect().contains(x, y)) {
                    shop.openShop();
                    currentState = GameState.SHOPPING;
                    return false;
                }
                break;
            case SHOPPING:
                if (shop.getBackboardRect().contains(x, y)) {
                    int selectedBox = shop.onTouch(event, x, y);
                    if (selectedBox != -1) {
                        shop.foldShop();
                        currentState = GameState.PREPARE;
                    }else{
                        return false;
                    }
                }else{
                    shop.foldShop();
                    currentState = GameState.PREPARE;
                }
                break;
            case BATTLE:
            case RESULT:
            case POST_GAME:
                break;
        }
        return true;
    }

    @Override
    public GameState getGameState(){
        return currentState;
    }

    public Shop getShop() {
        return shop;
    }
}
