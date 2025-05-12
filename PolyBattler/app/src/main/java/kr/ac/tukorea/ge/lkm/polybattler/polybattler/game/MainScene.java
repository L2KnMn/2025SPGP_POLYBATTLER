package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Canvas;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.Background;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class MainScene extends Scene {
    private static final String TAG = "MainScene";
    private final Background backgorund;
    private final MasterManager master;

    public MainScene() {
        initLayers(Layer.COUNT);
        Metrics.setGameSize(700, 1600);
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;
        master = MasterManager.getInstance(this);
        backgorund = new Background();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return master.onTouch(event);
    }

    @Override
    public void draw(Canvas canvas){
        backgorund.draw(canvas);
        super.draw(canvas);
    }
}
