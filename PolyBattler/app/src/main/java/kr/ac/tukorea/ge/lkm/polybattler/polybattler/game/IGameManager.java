package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.view.MotionEvent;

public interface IGameManager {
    public boolean onTouch(MotionEvent event);
    public void setGameState(GameState state);
}