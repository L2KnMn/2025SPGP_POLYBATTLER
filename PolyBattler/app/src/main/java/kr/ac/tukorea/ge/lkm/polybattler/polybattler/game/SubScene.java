package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class SubScene extends Scene {
    static final private String TAG = SubScene.class.getSimpleName();
    public SubScene() {
        Log.d(TAG, "SubScene");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pop();
        return false;
    }
}
