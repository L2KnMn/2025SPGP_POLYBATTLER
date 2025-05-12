package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class Background implements IGameObject {

    private final Bitmap backgroundImage;

    public Background(){
        backgroundImage = BitmapPool.get(R.mipmap.game_background);
    }

    @Override
    public void update() {}

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(backgroundImage, null, Metrics.screenRect, null);    }
}
