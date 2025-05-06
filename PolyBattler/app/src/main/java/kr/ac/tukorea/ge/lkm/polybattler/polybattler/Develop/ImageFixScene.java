package kr.ac.tukorea.ge.lkm.polybattler.polybattler.Develop;

import android.database.MergeCursor;
import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.BuildConfig;
import kr.ac.tukorea.ge.lkm.polybattler.R;
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
    enum Layer {
        BACK, IMAGE,
    }

    public ImageFixScene() {
        GameView.drawsDebugStuffs = BuildConfig.DEBUG;

        Log.d(TAG, "Image Fix Scene");
        initLayers(2);
        image = new ImageFixer(R.mipmap.coin_images);
        image.setPos(Metrics.width/2, 500);
//        add(Layer.IMAGE, image);
//        image.rearrangeAndSaveSprites(GameView.view.getContext());

        Coin coin = new Coin();
        coin.setPosition(Metrics.width/2, 1000, Metrics.width/2);
        add(Layer.IMAGE, coin);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
