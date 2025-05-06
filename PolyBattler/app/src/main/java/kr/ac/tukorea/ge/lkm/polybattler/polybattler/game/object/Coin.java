package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.IRemovable;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.ILayerProvider;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IRecyclable;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.AnimSprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;

public class Coin extends AnimSprite implements IRecyclable, IRemovable, ILayerProvider {
    private final Paint paint;
    private boolean removed;

    public Coin(){
        super(R.mipmap.coin_images, 16, 8);
        removed =false;
        paint = new Paint();
        paint.setAlpha(255);

    }

    @Override
    public void update(){
        super.update();
        if(removed){
            Scene.top().remove(this);
        }
    }

    @Override
    public void draw(Canvas canvas){
        // AnimSprite 는 단순반복하는 이미지이므로 time 을 update 에서 꼼꼼히 누적하지 않아도 된다.
        // draw 에서 생성시각과의 차이로 frameIndex 를 계산한다.
        long now = System.currentTimeMillis();
        float time = (now - createdOn) / 1000.0f;
        int frameIndex = Math.round(time * fps) % (frameCount * 2 - 2);
        if(frameIndex >= frameCount){
            frameIndex = (frameCount * 2 - 2) - frameIndex;
        }
        srcRect.set(frameIndex * frameWidth, 0, (frameIndex + 1) * frameWidth, frameHeight);
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
    }

    @Override
    public void remove() {
        removed = true;
    }

    @Override
    public Enum getLayer() {
        return Layer.effect_back;
    }

    @Override
    public void onRecycle() {}

    public void setPosition(float x, float y){
        setPosition(x, y, super.radius);
    }

    public void setAlpha(int alpha){
        paint.setAlpha(alpha);
    }
}
