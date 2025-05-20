package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop;

import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MasterManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ShopManager implements IGameManager {
    private static final Map<Scene, ShopManager> instances = new HashMap<>();
    private final Scene master;
    private final Shop shop;
    private GameState currentState;

    public ShopManager(Scene master) {
        shop = new Shop();
        shop.createUI(master);
        currentState = GameState.PREPARE;
        this.master = master;
        master.add(Layer.ui, shop);
    }

    public static ShopManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, ShopManager::new);
    }

    @Override
    public IGameManager setGameState(GameState newState){
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
        return this;
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

        if (!shop.isActive()) { return false; }
        GameState privState = currentState;
        switch (currentState){
        case PREPARE:
            break;
        case SHOPPING:
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (shop.getBackboardRect().contains(x, y)) {
                    // 상점 배경 안을 터치 했으니 일단은 상점과 관련된 것이라 판단
                    int selectedBox = shop.purchase(x, y);
                    if (selectedBox != -1 && !shop.isSoldOut(selectedBox)) {
                        if(GameManager.getInstance(master).getGold() < shop.getPrice(selectedBox))
                            UiManager.getInstance(master).showToast("not enough gold");
                        else{
                            boolean result = GameManager.getInstance(master).purchaseCharacter(shop.getPrice(selectedBox), shop.getShape(selectedBox), shop.getColor(selectedBox));
                            if (result) {
                                shop.makeSoldOut(selectedBox);
                            } else {
                                GameManager.getInstance(master).addGold(shop.getPrice(selectedBox));
                                UiManager.getInstance(master).showToast("full bench");
                            }
                        }
                        return true;
                    }
                    // 일단 이벤트를 소비하긴 해서 다른 거 작동 안 하게 만듦
                    return true;
                } else {
                    // 상품 창 밖을 터치하면 상품창 닫고 준비 단계로 돌아가기
                    shop.foldShop();
                    MasterManager.getInstance(master).setGameState(GameState.PREPARE);
                    return false;
                }
            }
        }
        return false;
    }

    public void reRoll(){
        shop.setRandomGoods(1);
    }

    public void reRoll(int price){
        if(GameManager.getInstance(master).spendGold(price)) {
            shop.setRandomGoods(price);
        }else{
            UiManager.getInstance(master).showToast("not enough gold");
        }
    }

    @Override
    public GameState getGameState(){
        return currentState;
    }

    public Shop getShop() {
        return shop;
    }
}
