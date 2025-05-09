package kr.ac.tukorea.ge.lkm.polybattler.polybattler.app;

import android.os.Bundle;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.Develop.ImageFixScene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.activity.GameActivity;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MainScene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class BattleGameActivity extends GameActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Scene main = new MainScene();
        main.push();
    }
}