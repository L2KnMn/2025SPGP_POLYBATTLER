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
    private IGameObject[][] board;
    private final RectF dstRect;
    private final RectF tileRect;
    private final Paint paintLight;
    private final Paint paintDark;

    private Position startTileLeftTop;
    private Position startTileCenter;

    public Boardmap(){
        width = 4;
        height = 7;

        tileRect = new RectF(0, 0, (Metrics.SCREEN_WIDTH-2) / width, (Metrics.SCREEN_WIDTH-2) / width);
        startTileLeftTop = new Position( (Metrics.SCREEN_WIDTH-tileRect.width()*width)/2
                , (Metrics.SCREEN_HEIGHT-tileRect.height()*height)/2);

        dstRect  = new RectF(startTileLeftTop.x, startTileLeftTop.y,
                startTileLeftTop.x + tileRect.width() * width,
                startTileLeftTop.y + tileRect.height() * height);

        startTileCenter = new Position(startTileLeftTop.x + tileRect.width() / 2,
                startTileLeftTop.y + tileRect.height() / 2);

        board = new IGameObject[width][height];

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
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if((i+j)%2 == 0){
                    float sx = startTileLeftTop.x + i * tileRect.width();
                    float sy = startTileLeftTop.y + j * tileRect.height();
                    canvas.drawRect(sx, sy, sx + tileRect.width(), sy + tileRect.height(), paintLight);
                }
            }
        }
    }
}

