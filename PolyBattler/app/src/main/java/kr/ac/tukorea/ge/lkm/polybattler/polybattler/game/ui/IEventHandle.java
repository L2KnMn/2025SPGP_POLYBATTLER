package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;

public interface IEventHandle {
    public boolean handleTouchEvent(MotionEvent event, float x, float y);
    public boolean isVisible(GameState state);
}

