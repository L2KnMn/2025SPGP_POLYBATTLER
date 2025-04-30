package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.util.ArrayMap;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop.ShopManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class MasterManager implements IGameManager {
    private final ArrayList<IGameManager> managers;
    private final Scene master;
    private GameState currentState;

    public MasterManager(Scene master){
        this.master = master;
        managers = new ArrayList<>();
        currentState = GameState.PREPARE;
        managers.add(UiManager.getInstance(master).setGameState(currentState));
        managers.add(ShopManager.getInstance(master).setGameState(currentState));
        managers.add(GameManager.getInstance(master).setGameState(currentState));
        managers.add(EffectManager.getInstance(master).setGameState(currentState));
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        boolean spend = false;
        for (IGameManager manager : managers) {
            spend = manager.onTouch(event);
            if (spend) break;
        }
        return spend;
    }

    @Override
    public IGameManager setGameState(GameState state) {
        currentState = state;
        for(IGameManager manager : managers){
            manager.setGameState(state);
        }
        return this;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }
}
