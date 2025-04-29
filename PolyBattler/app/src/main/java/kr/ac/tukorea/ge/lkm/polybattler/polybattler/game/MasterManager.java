package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.util.ArrayMap;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop.ShopManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class MasterManager implements IGameManager {
    private static final ArrayMap<Scene, ArrayList<IGameManager>> managers;
    private Scene master;
    private GameState currentState;

    GameManager gameManager;
    ShopManager shopManager;
    UiManager uiManager;
    EffectManager effectManager;

    private MasterManager(Scene master){
        this.master = master;
        currentState = GameState.PREPARE;

        GameManager.getInstance(master).setGameState(currentState);
        ShopManager.getInstance(master).setGameState(currentState);
        UiManager.getInstance(master).setGameState(currentState);

        managerArray.add(UiManager.getInstance(this));
        managerArray.add(ShopManager.getInstance(this));
        managerArray.add(GameManager.getInstance(this));
        managerArray.add(EffectManager.getInstance(this));
    }

    public MasterManager get(Scene master){
        return instances.computeIfAbsent(master, GameManager::new);
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        return false;
    }

    @Override
    public void setGameState(GameState state) {
        currentState = state;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }
}
