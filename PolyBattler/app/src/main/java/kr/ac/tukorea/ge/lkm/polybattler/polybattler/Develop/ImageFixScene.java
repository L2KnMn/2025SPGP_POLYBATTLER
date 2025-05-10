package kr.ac.tukorea.ge.lkm.polybattler.polybattler.Develop;

import android.database.MergeCursor;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Coin;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.Background;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.AnimSprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.Sprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ImageFixScene extends Scene {
    static final private String TAG = ImageFixScene.class.getSimpleName();

    ImageFixer image;

    ArrayList<Polyman> tester = new ArrayList<>();


    public ImageFixScene() {
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;

        Log.d(TAG, "Image Fix Scene");
        initLayers(Layer.COUNT);

        tester.add(new Polyman());
        tester.get(0).transform.set(100f, 100f);
        add(tester.get(0));
        tester.add(1, new Polyman());
        tester.get(1).transform.set(100f, 900f);
        add(tester.get(1));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return false;

        EffectManager em = EffectManager.getInstance(this);

        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

//        EffectManager.getInstance(this).addEffect(
//                new EffectManager.AttackEffect().init(
//                    tester.get(0).getBattleUnit(), tester.get(1).getBattleUnit()));

        tester.get(0).getBattleUnit().setCurrentTarget(tester.get(1).getBattleUnit());
        tester.get(0).getBattleUnit().initAttackEffect();

        return true;
    }
}
