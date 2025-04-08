package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.content.res.Resources;

public class Boardmap implements IGameObject {

    private int width;
    private int height;
    private Transform transform;
    private IGameObject[][] board;
    private final Bitmap bitmap;
    private final RectF dstRect;
    private final RectF tileRect;
    private final Paint paintLight;
    private final Paint paintDark;

    public Boardmap(Bitmap bitmap){
        this.bitmap = bitmap;
        dstRect  = new RectF(0, 0, Metrics.SCREEN_WIDTH, Metrics.SCREEN_HEIGHT);
        tileRect = new RectF(0, 0, 1, 1);
        width = 4;
        height = 7;
        board = new IGameObject[width][height];
        transform = new Transform(0, 0);

        paintLight = new Paint();
        paintLight.setColor(0xFFD2944A);
        paintLight.setStyle(Paint.Style.FILL);

        paintDark = new Paint();
        paintDark.setColor(0xffAE6B2D);
        paintDark.setStyle(Paint.Style.FILL);
    }

    @Override
    public void update() {
        // 업데이트 로직
    }

    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        canvas.drawRect(dstRect, paintDark);
        float tileWidth = dstRect.width() / width;
        float tileHeight = dstRect.height() / height;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if((i+j)%2 == 0){
                    canvas.drawRect(i * tileWidth,
                            j * tileHeight,
                            (i + 1) * tileWidth,
                            (j + 1) * tileHeight, paintLight);
                }
            }
        }
    }
}

