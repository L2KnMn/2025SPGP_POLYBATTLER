package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.view.MotionEvent;

public interface IGameManager {
    boolean onTouch(MotionEvent event);
    void setGameState(GameState state);
    GameState getGameState();
}