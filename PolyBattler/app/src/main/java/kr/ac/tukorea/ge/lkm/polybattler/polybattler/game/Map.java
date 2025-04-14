package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.framework.util.Position;
import kr.ac.tukorea.ge.lkm.polybattler.framework.util.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.lkm.polybattler.framework.view.Metrics;

public class Map implements IGameObject {

    final private float length;
    final private int width;
    final private int height;
    private final IGameObject[][] board;
    private final boolean[][] boardState;
    private final IGameObject[] bench;
    final private int benchSize;
    final private Position startBenchLeftTop;
    private final RectF dstRect;
    private final RectF benchRect;
    private final RectF tileRect;
    private final Paint paintLight;
    private final Paint paintDark;
    private final Paint paintFilter;

    private final Transform predictPoint;
    private final Paint predictRectPaint;
    final private Position startTileLeftTop;
    private boolean activate;
    private boolean floatObjectOn;

    public Map(){
        benchSize = 5;
        width = 4;
        height = 7;

        activate = true;

        int width_max = Math.max(benchSize, width);
        int height_max = height + 1;

        float tileWidth = (Metrics.width -Metrics.GRID_UNIT/2) / width_max;
        float tileHeight = (Metrics.height -Metrics.GRID_UNIT) / height_max;
        length = tileWidth < tileHeight ? tileWidth : tileHeight;

        float height_term = (Metrics.height - length * height_max) / 3;

        tileRect = new RectF(0, 0, length, length);
        startTileLeftTop = new Position( (Metrics.width - tileRect.width()*width)/2, height_term);
        startBenchLeftTop = new Position((Metrics.width - tileRect.width()*benchSize)/2, Metrics.height -tileRect.height() - height_term);

        dstRect  = new RectF(startTileLeftTop.x, startTileLeftTop.y,
                startTileLeftTop.x + tileRect.width() * width,
                startTileLeftTop.y + tileRect.height() * height);
        benchRect = new RectF(startBenchLeftTop.x, startBenchLeftTop.y,
                startBenchLeftTop.x + tileRect.width() * benchSize,
                startBenchLeftTop.y + tileRect.height());

        boardState = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height-3; j++) {
                boardState[i][j] = false;
            }
            for (int j = height-3; j < height; j++) {
                boardState[i][j] = true;
            }
        }
        board = new IGameObject[width][height];
        bench = new IGameObject[benchSize];

        paintLight = new Paint();
        paintLight.setColor(0xFFD2944A);
        paintLight.setStyle(Paint.Style.FILL);

        paintDark = new Paint();
        paintDark.setColor(0xffAE6B2D);
        paintDark.setStyle(Paint.Style.FILL);

        paintFilter = new Paint();
        paintFilter.setColor(0x80000000);
        paintFilter.setStyle(Paint.Style.FILL);

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
        // 맵 그림
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
                if(!boardState[i][j]){
                    canvas.drawRect(tileRect, paintFilter);
                }
            }
        }
        // 벤치 그림
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
        // 짚는 물체 예상 위치 그림
        if(floatObjectOn){
            if(isSettable(predictPoint.getPosition().x, predictPoint.getPosition().y)){
                canvas.drawRect(predictPoint.getRect(), predictRectPaint);
            }else{
                predictPoint.moveTo(origin_x, origin_y);
                canvas.drawRect(predictPoint.getRect(), predictRectPaint);
            }
        }
    }

    @Override
    public Transform getTransform() {
        Log.d("Boardmap", "call boardmap's getTransform() it is error");
        return null;
    }

    @Override
    public void SetActive(boolean active) {
        // Polyman의 활성화 로직
        activate = active;
    }

    @Override
    public boolean isActive() {
        return activate;
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
        transform.moveTo(x, y);

        if(transform.isRigid()) {
            return putOnBoard(transform.getInstance());
        }
        return true;
    }

    public boolean setObjectOnBench(Transform transform, int index){
        if(index < 0 || index >= this.benchSize) {
            return false;
        }
        if(transform.isRigid() && bench[index] != null){
            return false;
        }
        float x = startBenchLeftTop.x + length * (index + 0.5f);
        float y = startBenchLeftTop.y + length/2;
        transform.moveTo(x, y);
        if(transform.isRigid())
            putOnBench(transform.getInstance());
        return true;
    }

    public boolean setPositionNear(Transform transform){
        return setPositionNear(transform, TilePosition.CENTER);
    }
    public boolean setPositionNear(Transform transform, TilePosition gravity){
        if(dstRect.contains(transform.getPosition().x, transform.getPosition().y)){
            int targetWidth = getWidth(transform.getPosition().x);
            int targetHeight = getHeight(transform.getPosition().y);
            return setObjectOnTile(transform, targetWidth, targetHeight, gravity);
        }else if(benchRect.contains(transform.getPosition().x, transform.getPosition().y)){
            return setObjectOnBench(transform, getIndex(transform.getPosition().x, transform.getPosition().y));
        }else{
            return false;
        }
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

    private boolean putOnBoard(IGameObject object){
        int width = getWidth(object.getTransform().getPosition().x);
        int height = getHeight(object.getTransform().getPosition().y);

        if(width < 0 || width >= this.width){
            return false;
        }
        if(height < 0 || height >= this.height) {
            return false;
        }

        if(board[width][height] == null) {
            board[width][height] = object;
            return true;
        }
        return false;
    }
    private boolean putOnBench(IGameObject object){
        int index = getIndex(object.getTransform().getPosition().x, object.getTransform().getPosition().y);
        if(index < 0 || index >= this.benchSize) {
            return false;
        }
        if(bench[index] == null) {
            bench[index] = object;
            return true;
        }
        return false;
    }

    private boolean putOnMap(IGameObject object){
        if(dstRect.contains(object.getTransform().getPosition().x, object.getTransform().getPosition().y)){
            return putOnBoard(object);
        }else if(benchRect.contains(object.getTransform().getPosition().x, object.getTransform().getPosition().y)){
            return putOnBench(object);
        }
        return false;
    }

    float origin_x;
    float origin_y;
    private IGameObject pickedObject = null;
    public IGameObject setOnPredictPoint(float x, float y) {
        // 물체를 짚는 것을 지시
        // 이 때 이미 짚은 물체가 있다면 어떻게 처리할 것인지 고민해봐야됨
        // 일단은 에러 로그를 표시하고, 그냥 새 짚는 명령 무시하는 걸로
        IGameObject picked = null;
        if(floatObjectOn){
            Log.d("Boardmap", "already float object is exist");
           return picked;
        }
        if(dstRect.contains(x, y)) {
            int width = getWidth(x);
            int height = getHeight(y);
            if(board[width][height] != null) {
                pickedObject = board[width][height];
                activatePredictPoint(x, y);
                board[width][height] = null;
            }
        }else if (benchRect.contains(x, y)) {
            int index = getIndex(x, y);
            if(index >= 0 && index < benchSize && bench[index] != null) {
                pickedObject = bench[index];
                activatePredictPoint(x, y);
                bench[index] = null;
            }
        }
        return picked;
    }

    private void activatePredictPoint(float x, float y){
        predictPoint.moveTo(x, y);
        setPositionNear(predictPoint);
        origin_x = predictPoint.getPosition().x;
        origin_y = predictPoint.getPosition().y;
        floatObjectOn = true;
    }

    public void movePredictPoint(float x, float y) {
        if(!floatObjectOn)
            return;
        pickedObject.getTransform().moveTo(x, y);
        predictPoint.moveTo(x, y);
        setPositionNear(predictPoint);
    }

    public void setOffPredictPoint(float x, float y) {
        if(floatObjectOn) {
            floatObjectOn = false;
            pickedObject.getTransform().moveTo(x, y);
            predictPoint.moveTo(x, y);
            if(isSettable(pickedObject.getTransform())){
                setPositionNear(pickedObject.getTransform());
            }else{
                pickedObject.getTransform().moveTo(origin_x, origin_y);
            }
            setPositionNear(pickedObject.getTransform());
        }
    }

    public boolean isSettable(Transform transform){
        return isSettable(transform.getPosition().x, transform.getPosition().y);
    }
    public boolean isSettable(float x, float y){
        if(dstRect.contains(x, y)){
            int width = getWidth(x);
            int height = getHeight(y);
            if(boardState[width][height])
                return (board[width][height] == null);
            else
                return false;
        }else if(benchRect.contains(x, y)){
            int width = getIndex(x, y);
            return (bench[width] == null);
        }
        return false;
    }

    public IGameObject findObject(float x, float y){
        if(dstRect.contains(x, y)){
            int width = getWidth(x);
            int height = getHeight(y);
            return board[width][height];
        }else if(benchRect.contains(x, y)) {
            int index = getIndex(x, y);
            return bench[index];
        }
        return null;
    }

    private int getIndex(float x, float y) {
        if(benchRect.contains(x, y)){
            return (int)((x - startBenchLeftTop.x) / length);
        }
        return -1;
    }
}

