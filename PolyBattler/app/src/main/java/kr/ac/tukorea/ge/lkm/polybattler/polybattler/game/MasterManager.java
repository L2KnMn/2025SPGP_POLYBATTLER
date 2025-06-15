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
    MediaPlayer prepareBgPlayer;
    ArrayList<MediaPlayer> battleBgPlayer;
    MediaPlayer nowBgPlayer;

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


        prepareBgPlayer = MediaPlayer.create(GameView.view.getContext(), R.raw.prepare_phase_bg);
        prepareBgPlayer.setLooping(true);
        prepareBgPlayer.setVolume(1.0f, 1.0f);

        battleBgPlayer = new ArrayList<>();
        battleBgPlayer.add(MediaPlayer.create(GameView.view.getContext(), R.raw.battle_phase_bg1)); // Battle
        battleBgPlayer.add(MediaPlayer.create(GameView.view.getContext(), R.raw.battle_phase_bg2)); // result
        battleBgPlayer.add(MediaPlayer.create(GameView.view.getContext(), R.raw.battle_phase_bg3)); // post game
        for(MediaPlayer player : battleBgPlayer) {
            if(player != null){
                player.setLooping(true);
                player.setVolume(1.0f, 1.0f);
            }
        }
        nowBgPlayer = prepareBgPlayer;
        nowBgPlayer.start();
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
        // 새 상태가 기존 상태랑 비교해서 음악 교체해야 하는지 검사
        if(battleMusicPlay(state) != battleMusicPlay(currentState)){
            // 기존 전투 음악 끄기
            if(nowBgPlayer != null){
                nowBgPlayer.pause();
            }
            if(battleMusicPlay(state)){
                nowBgPlayer = battleBgPlayer.get(state.ordinal());
            }else{
                nowBgPlayer = prepareBgPlayer;
            }
            if(nowBgPlayer != null){
                nowBgPlayer.start();
            }
        } // 서로 같으면 굳이 음악 교체할 필요 없음

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

    private boolean battleMusicPlay(GameState state){
        return state == GameState.BATTLE || state == GameState.RESULT || state == GameState.POST_GAME;
    }

    public void onExit() {
        nowBgPlayer.stop();
    }

    public void onPause(){
        nowBgPlayer.pause();
    }

    public void onResume() {
        nowBgPlayer.start();
    }
}
