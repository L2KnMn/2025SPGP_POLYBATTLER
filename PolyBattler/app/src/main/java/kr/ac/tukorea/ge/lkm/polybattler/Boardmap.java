package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Boardmap implements IGameObject {

    final private float length;
    private int width;
    private int height;
    private IGameObject[][] board;
    private final RectF dstRect;
    private final RectF tileRect;
    private final Paint paintLight;
    private final Paint paintDark;

    final private Position startTileLeftTop;

    public Boardmap(){
        width = 4;
        height = 7;

        float tileWidth = (Metrics.SCREEN_WIDTH-2) / width;
        float tileHeight = (Metrics.SCREEN_HEIGHT-2) / height;
        length = tileWidth < tileHeight ? tileWidth : tileHeight;

        tileRect = new RectF(0, 0, length, length);
        startTileLeftTop = new Position( (Metrics.SCREEN_WIDTH-tileRect.width()*width)/2
                , (Metrics.SCREEN_HEIGHT-tileRect.height()*height)/2);

        dstRect  = new RectF(startTileLeftTop.x, startTileLeftTop.y,
                startTileLeftTop.x + tileRect.width() * width,
                startTileLeftTop.y + tileRect.height() * height);

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

    @Override
    public Transform getTransform() {
        return null;
    }

    public float getTileSize() {
        return length;
    }

    public Position getTileLeftTop() {
        return startTileLeftTop;
    }

    public int getWidth(float x) {
        return (int) ((x - startTileLeftTop.x) / length);
    }

    public int getHeight(float y) {
        return (int) ((y - startTileLeftTop.y) / length);
    }

    public enum TilePosition{
        CENTER, TOP, BOTTOM, LEFT, RIGHT,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public void setPositionTile(Transform transform, int width, int height){
        setPositionTile(transform, width, height, TilePosition.CENTER);
    }

    public void setPositionTile(Transform transform, int width, int height, TilePosition gravity){
        float ax = 0f, ay = 0f;
        switch (gravity){
            case CENTER:
            case TOP:
            case BOTTOM:
                ax = 0.5f;
                break;
            case LEFT:
            case TOP_LEFT:
            case BOTTOM_LEFT:
                ax = 0.0f;
                break;
            case RIGHT:
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                ax = 1.0f;
                break;
        }
        switch (gravity){
            case CENTER:
            case LEFT:
            case RIGHT:
                ay = 0.5f;
                break;
            case TOP:
            case TOP_LEFT:
            case TOP_RIGHT:
                ay = 0.0f;
                break;
            case BOTTOM:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                ay = 1.0f;
                break;
        }
        float x = startTileLeftTop.x + (width + ax) * length;
        float y = startTileLeftTop.y + (height + ay) * length;
        transform.set(x, y);
    }

    public void setPositionNearTile(Transform transform){
        setPositionNearTile(transform, TilePosition.CENTER);
    }
    public void setPositionNearTile(Transform transform, TilePosition gravity){
        int targetWidth = getWidth(transform.getPosition().x);
        int targetHeight = getHeight(transform.getPosition().y);
        setPositionTile(transform, targetWidth, targetHeight, gravity);
    }
}

