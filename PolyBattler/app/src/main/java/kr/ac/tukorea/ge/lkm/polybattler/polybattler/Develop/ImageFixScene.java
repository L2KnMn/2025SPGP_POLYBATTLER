package kr.ac.tukorea.ge.lkm.polybattler.polybattler.Develop;

import android.database.MergeCursor;
import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Coin;
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

    public ImageFixScene() {
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;

        Log.d(TAG, "Image Fix Scene");
        initLayers(Layer.COUNT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return false;

        EffectManager em = EffectManager.getInstance(this);

        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

        for(int i = 0; i < 10; ++i) {
            EffectManager.CoinEffect coinEffect = getRecyclable(EffectManager.CoinEffect.class);
            coinEffect.init(x + Metrics.GRID_UNIT * (float) Math.random(), y + Metrics.GRID_UNIT * (float) Math.random());
            em.addEffect(coinEffect);
        }
        return true;
    }
}
