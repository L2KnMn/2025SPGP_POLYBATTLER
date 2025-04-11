package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class Boardmap implements IGameObject {

    final private float length;
    final private int width;
    final private int height;
    private IGameObject[][] board;
    private IGameObject[][] bench;
    final private int benchSize;
    final private Position startBenchLeftTop;
    private final RectF dstRect;
    private final RectF benchRect;
    private final RectF tileRect;
    private final Paint paintLight;
    private final Paint paintDark;

    private Transform predictPoint;
    private Paint predictRectPaint;
    final private Position startTileLeftTop;
    private boolean availible;
    private boolean floatObjectOn;

    public Boardmap(){
        benchSize = 5;
        width = 4;
        height = 7;

        availible = true;

        int width_max = Math.max(benchSize, width);
        int height_max = height + 1;

        float tileWidth = (Metrics.SCREEN_WIDTH-1) / width_max;
        float tileHeight = (Metrics.SCREEN_HEIGHT-1) / height_max;
        length = tileWidth < tileHeight ? tileWidth : tileHeight;

        tileRect = new RectF(0, 0, length, length);
        startTileLeftTop = new Position( (Metrics.SCREEN_WIDTH-tileRect.width()*width)/2, 0);

        startBenchLeftTop = new Position((Metrics.SCREEN_WIDTH-tileRect.width()*benchSize)/2, Metrics.SCREEN_HEIGHT-tileRect.height());

        dstRect  = new RectF(startTileLeftTop.x, startTileLeftTop.y,
                startTileLeftTop.x + tileRect.width() * width,
                startTileLeftTop.y + tileRect.height() * height);
        benchRect = new RectF(startBenchLeftTop.x, startBenchLeftTop.y,
                startBenchLeftTop.x + tileRect.width() * benchSize,
                startBenchLeftTop.y + tileRect.height());

        board = new IGameObject[width][height];

        paintLight = new Paint();
        paintLight.setColor(0xFFD2944A);
        paintLight.setStyle(Paint.Style.FILL);

        paintDark = new Paint();
        paintDark.setColor(0xffAE6B2D);
        paintDark.setStyle(Paint.Style.FILL);

        predictPoint = new Transform(this);
        predictPoint.setSize(length/2);
        predictPoint.setRigid(false);
        predictRectPaint = new Paint();
        predictRectPaint.setColor(0xA0FFEF82);
        predictRectPaint.setStyle(Paint.Style.STROKE);
        predictRectPaint.setStrokeWidth(0.3f);
        floatObjectOn = false;
    }

    @Override
    public void update() {
        // 업데이트 로직
    }

    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        canvas.drawRect(dstRect, predictRectPaint);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                float sx = startTileLeftTop.x + i * tileRect.width();
                float sy = startTileLeftTop.y + j * tileRect.height();
                tileRect.set(sx, sy, sx + tileRect.width(), sy + tileRect.height());
                if((i+j)%2 == 0){
                    canvas.drawRect(tileRect, paintLight);
                }else{
                    canvas.drawRect(tileRect, paintDark);
                }
            }
        }
        canvas.drawRect(benchRect, predictRectPaint);
        for (int i = 0; i < benchSize; i++) {
            float sx = startBenchLeftTop.x + i * tileRect.width();
            float sy = startBenchLeftTop.y;
            tileRect.set(sx, sy, sx + tileRect.width(), sy + tileRect.height());
            if(i%2 == 0){
                canvas.drawRect(tileRect, paintLight);
            }else{
                canvas.drawRect(tileRect, paintDark);
            }
        }
        if(floatObjectOn){
            if(isSettable(predictPoint.getPosition().x, predictPoint.getPosition().y)){
                setPositionNearTile(predictPoint);
                canvas.drawRect(predictPoint.getRect(), predictRectPaint);
            }else{
                predictPoint.set(getTileX(origin_width, TilePosition.CENTER), getTileY(origin_height, TilePosition.CENTER));
                canvas.drawRect(predictPoint.getRect(), predictRectPaint);
            }
        }
    }

    @Override
    public Transform getTransform() {
        return null;
    }

    @Override
    public void SetActive(boolean active) {
        // Polyman의 활성화 로직
        availible = active;
    }

    @Override
    public boolean isActive() {
        return availible;
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

    public float getTileX(int width, TilePosition gravity) {
        float ax = 0.0f;
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
            default:
                Log.d("Boardmap", "gravity error");
        }
        return startTileLeftTop.x + (width + ax) * length;
    }

    public int getHeight(float y) {
        return (int) ((y - startTileLeftTop.y) / length);
    }

    public float getTileY(int height, TilePosition gravity) {
        float ay = 0.0f;
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
            default:
                Log.d("Boardmap", "gravity error");
        }
        return startTileLeftTop.y + (height + ay) * length;
    }

    public enum TilePosition{
        CENTER, TOP, BOTTOM, LEFT, RIGHT,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public boolean setObjectOnTile(Transform transform, int width, int height){
        return setObjectOnTile(transform, width, height, TilePosition.CENTER);
    }

    public boolean setObjectOnTile(Transform transform, int width, int height, TilePosition gravity){
        if(width < 0 || width >= this.width) {
            return false;
        }
        if(height < 0 || height >= this.height) {
            return false;
        }
        if(transform.isRigid() && board[width][height] != null){
            return false;
        }
        float x = getTileX(width, gravity);
        float y = getTileY(height, gravity);
        transform.set(x, y);
        if(transform.isRigid()){
            putOnBoard(transform.getInstance());
        }
        return true;
    }

    public boolean setPositionNearTile(Transform transform){
        if(!dstRect.contains(transform.getPosition().x, transform.getPosition().y)){
            return false;
        }
        return setPositionNearTile(transform, TilePosition.CENTER);
    }
    public boolean setPositionNearTile(Transform transform, TilePosition gravity){
        int targetWidth = getWidth(transform.getPosition().x);
        int targetHeight = getHeight(transform.getPosition().y);
        return setObjectOnTile(transform, targetWidth, targetHeight, gravity);
    }

    public IGameObject pickUpObject(int width, int height){
        if(width < 0 || width >= this.width){
            return null;
        }
        if(height < 0 || height >= this.height) {
            return null;
        }
        IGameObject temp = board[width][height];
        board[width][height] = null;
        return temp;
    }
    public void putOnBoard(IGameObject object){
        int width = getWidth(object.getTransform().getPosition().x);
        int height = getHeight(object.getTransform().getPosition().y);

        if(width < 0 || width >= this.width){
            return;
        }
        if(height < 0 || height >= this.height) {
            return;
        }

        if(board[width][height] == null) {
            board[width][height] = object;
        }
    }

    int origin_width;
    int origin_height;
    public void setOnPredictPoint(float x, float y) {
        predictPoint.set(x, y);
        origin_width = getWidth(x);
        origin_height = getHeight(y);
        floatObjectOn = true;
    }

    public void movePredictPoint(float x, float y) {
        predictPoint.set(x, y);
    }

    public void setOffPredictPoint() {
        floatObjectOn = false;
        origin_width = -1;
        origin_height = -1;
    }

    public boolean isSettable(float x, float y){
        if(dstRect.contains(x, y)){
            int width = getWidth(x);
            int height = getHeight(y);
            return (board[width][height] == null);
        }
        return false;
    }
}

