package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.framework.interfaces.IGameObject;

public class PlayerControll {
    private IGameObject pickedObject;
    private Map map;
    private Shop shop;

    public PlayerControll() {
        pickedObject = null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        return true;
    }
}
