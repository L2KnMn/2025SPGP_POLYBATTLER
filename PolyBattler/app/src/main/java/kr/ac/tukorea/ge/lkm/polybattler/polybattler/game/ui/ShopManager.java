package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ShopManager implements IGameManager {
    private static final Map<Scene, ShopManager> instances = new HashMap<>();
    private final Scene master;
    private final Shop shop;
    private GameState currentState;

    public ShopManager(Scene master) {
        shop = new Shop();
        currentState = GameState.PREPARE;
        this.master = master;
    }

    public static ShopManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, ShopManager::new);
    }

    @Override
    public void setGameState(GameState newState){
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
        currentState = newState;
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

        if (!shop.isActive()) { return false; }
        switch (currentState){
        case PREPARE:
            if(event.getAction() == MotionEvent.ACTION_DOWN
                    && shop.getIconRect().contains(x, y)) {
                shop.openShop();
                currentState = GameState.SHOPPING;
                return true;
            }
        case SHOPPING:
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (shop.getBackboardRect().contains(x, y)) {
                    // 상점 배경 안을 터치 했으니 일단은 상점과 관련된 것이라 판단
                    int selectedBox = shop.purchase(x, y);
                    if (selectedBox != -1 && !shop.isSoldOut(selectedBox)) {
                        if(GameManager.getInstance(master).getGold() < shop.getPrice(selectedBox))
                            UiManager.getInstance(master).showToast("not enough gold");
                        else{
                            boolean result = GameManager.getInstance(master).addCharacter(shop.getPrice(selectedBox), shop.getShape(selectedBox), shop.getColor(selectedBox));
                            if (result) {
                                shop.makeSoldOut(selectedBox);
                            } else {
                                GameManager.getInstance(master).addGold(shop.getPrice(selectedBox));
                                UiManager.getInstance(master).showToast("full bench");
                            }
                        }
                        return true;
                    } else if(shop.RerollButtonRect().contains(x, y)){
                        if(GameManager.getInstance(master).spendGold(1)) {
                            shop.setRandomGoods();
                        }else{
                            UiManager.getInstance(master).showToast("not enough gold");
                        }
                    }
                    // 일단 이벤트를 소비하긴 해서 다른 거 작동 안 하게 만듦
                    return true;
                } else {
                    // 상품 창 밖을 터치하면 상품창 닫고 준비 단계로 돌아가기
                    shop.foldShop();
                    currentState = GameState.PREPARE;
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public GameState getGameState(){
        return currentState;
    }

    public Shop getShop() {
        return shop;
    }
}
