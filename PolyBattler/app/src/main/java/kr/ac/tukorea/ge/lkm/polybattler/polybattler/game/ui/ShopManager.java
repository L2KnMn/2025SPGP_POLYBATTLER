package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ColorType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ShapeType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ShopManager implements IGameManager {
    private static ShopManager instance;
    private Scene master;
    private final Shop shop;
    private GameState currentState;

    public ShopManager(Scene master) {
        shop = new Shop();
        currentState = GameState.PREPARE;
        this.master = master;
    }

    public static ShopManager getInstance(Scene master) {
        if (instance == null) {
            instance = new ShopManager(master);
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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (shop.getBackboardRect().contains(x, y)) {
                        int selectedBox = shop.purchase(x, y);
                        if (selectedBox != -1 && shop.soldOut(selectedBox)) {
                            boolean result = GameManager.getInstance(master).purchaseCharactor(shop.getPrice(selectedBox), shop.getShape(selectedBox), shop.getColor(selectedBox));
                            if (result) {
                                shop.soldOut(selectedBox);
                            } else {
                                Log.d("ShopManager", "not enough gold");
                                return false;
                            }
                        } else if(shop.RerollButtonRect().contains(x, y)){
                            shop.setRandomGoods();
                        } else {
                            return false;
                        }
                    } else {
                        // 상품 창 밖을 터치하면 상품창 닫고 준비 단계로 돌아가기
                        shop.foldShop();
                        currentState = GameState.PREPARE;
                    }
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
