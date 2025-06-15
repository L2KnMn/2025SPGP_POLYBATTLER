package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.media.MediaPlayer;
import android.util.ArrayMap;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop.ShopManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;

public class MasterManager implements IGameManager {
    private static final ArrayMap<Scene, MasterManager> instances = new ArrayMap<>();
    private final ArrayList<IGameManager> managers;
    private final Scene master;
    private GameState currentState;
    MediaPlayer touchPlayer;

    public static MasterManager getInstance(Scene master){
        return instances.computeIfAbsent(master, MasterManager::new);
    }

    private MasterManager(Scene master){
        this.master = master;
        managers = new ArrayList<>();
        currentState = GameState.PREPARE;
        managers.add(UiManager.getInstance(master).setGameState(currentState));
        managers.add(ShopManager.getInstance(master).setGameState(currentState));
        managers.add(EffectManager.getInstance(master).setGameState(currentState));
        managers.add(GameManager.getInstance(master).setGameState(currentState));

        touchPlayer = MediaPlayer.create(GameView.view.getContext(), R.raw.touch_effect);
        touchPlayer.setLooping(false);
        touchPlayer.setVolume(1.0f, 1.0f);
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        boolean spend = false;
        for (IGameManager manager : managers) {
            GameState curr = currentState;
            spend = manager.onTouch(event);
            if(curr != currentState || spend)
                break;
        }
        if(spend)
            touchPlayer.start();
        return spend;
    }

    @Override
    public IGameManager setGameState(GameState state) {
        for(IGameManager manager : managers){
            manager.setGameState(state);
        }
        currentState = state;
        return this;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }
}
