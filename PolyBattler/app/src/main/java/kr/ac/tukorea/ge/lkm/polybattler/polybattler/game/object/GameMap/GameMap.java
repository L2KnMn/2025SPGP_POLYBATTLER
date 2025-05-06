package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.ResourceBundle;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class GameMap implements IGameObject {
    private final Field field;
    private final MapPart bench;

    private final float length;
    private final RectF tileRect;

    private final Paint paintShadows;
    private final Paint paintOutline;
    private final Paint paintLight;
    private final Paint paintDark;
    private final Paint paintBenchLight;
    private final Paint paintBenchDark;
    private final Paint paintFilter;

//    private final boolean active;

    private final Transform predictPoint;
    private final Paint predictRectPaint;
    private boolean floatObjectOn;
    private boolean drawBlocked;

    public GameMap(final int width, final int height, final int benchSize){
        field = new Field(width, height);
        bench = new MapPart(benchSize, 1);

        int width_max = Math.max(benchSize, width);
        int height_max = height + 1;

        float mapWidthLength = (Metrics.width -Metrics.GRID_UNIT/2);
        float mapHeightLength= (Metrics.height -Metrics.GRID_UNIT);
        length = Math.min(mapWidthLength / width_max, mapHeightLength / height_max);

        float height_term = (Metrics.height - length * height_max) / 3;

        tileRect = new RectF(0, 0, length, length);
        field.leftTop.set((Metrics.width - tileRect.width()*width)/2, height_term);
        bench.leftTop.set((Metrics.width - tileRect.width()*benchSize)/2, Metrics.height - tileRect.height() - height_term);

        field.dstRect.set(field.leftTop.x, field.leftTop.y,
                field.leftTop.x + tileRect.width() * width,
                field.leftTop.y + tileRect.height() * height);
        bench.dstRect.set(bench.leftTop.x, bench.leftTop.y,
                bench.leftTop.x + tileRect.width() * benchSize,
                bench.leftTop.y + tileRect.height());

        Resources res = GameView.view.getResources();

        paintLight = new Paint();
        paintLight.setColor(0xFFD2944A);
        paintLight.setStyle(Paint.Style.FILL);

        paintDark = new Paint();
        paintDark.setColor(0xffAE6B2D);
        paintDark.setStyle(Paint.Style.FILL);

        paintBenchLight = new Paint();
        paintBenchLight.setColor(ResourcesCompat.getColor(res, R.color.benchTileColor, null));
        paintBenchLight.setStyle(Paint.Style.FILL);

        paintBenchDark = new Paint();
        paintBenchDark.setColor(ResourcesCompat.getColor(res, R.color.benchTileColorDark, null));
        paintBenchDark.setStyle(Paint.Style.FILL);

        paintFilter = new Paint();
        paintFilter.setColor(0x80000000);
        paintFilter.setStyle(Paint.Style.FILL);

        predictPoint = new Transform(this);
        predictPoint.setSize(length * 0.9f);
        predictPoint.setRigid(false);

        predictRectPaint = new Paint();
        predictRectPaint.setColor(0xB3E7E306);
        predictRectPaint.setStyle(Paint.Style.STROKE);
        predictRectPaint.setStrokeWidth(Metrics.GRID_UNIT * 0.1f);

        paintShadows = new Paint();
        paintShadows.setColor(ResourcesCompat.getColor(res, R.color.gameMapShadows, null));
        paintShadows.setStrokeCap(Paint.Cap.ROUND);
        paintShadows.setStyle(Paint.Style.STROKE);
        paintShadows.setStrokeWidth(Metrics.GRID_UNIT * 0.3f);

        paintOutline = new Paint();
        paintOutline.setColor(ResourcesCompat.getColor(res, R.color.gameMapOutline, null));
        paintOutline.setStyle(Paint.Style.STROKE);
        paintOutline.setStrokeCap(Paint.Cap.ROUND);
        paintOutline.setStrokeWidth(Metrics.GRID_UNIT * 0.2f);

        floatObjectOn = false;

        drawBlocked = true;
    }

    @Override
    public void update() {}

    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        // 맵 그림
        canvas.drawRect(field.dstRect, paintShadows);
        canvas.drawRect(field.dstRect, paintOutline);
        tileRect.offsetTo(field.leftTop.x, field.leftTop.y);
        for (int i = 0; i < field.width; i++) {
            for (int j = 0; j < field.height; j++) {
                if((i+j)%2 == 0){
                    canvas.drawRect(tileRect, paintLight);
                }else{
                    canvas.drawRect(tileRect, paintDark);
                }
                if(drawBlocked && field.block(i, j)){
                    canvas.drawRect(tileRect, paintFilter);
                }
                tileRect.offset(0,length);
            }
            tileRect.offset(length, -length * field.height);
        }
        // 벤치 그림
        canvas.drawRect(bench.dstRect, paintOutline);
        tileRect.offsetTo(bench.leftTop.x, bench.leftTop.y);
        for (int i = 0; i < bench.width; i++) {
            if(i%2 == 0){
                canvas.drawRect(tileRect, paintBenchLight);
            }else{
                canvas.drawRect(tileRect, paintBenchDark);
            }
            tileRect.offset(tileRect.width(),0);
        }
        // 짚는 물체 예상 위치 그림
        if(floatObjectOn){
            if(isSettable(predictPoint)){
                canvas.drawRect(predictPoint.getRect(), predictRectPaint);
            }else{
                predictPoint.goTo(origin_x, origin_y);
                canvas.drawRect(predictPoint.getRect(), predictRectPaint);
            }
        }
    }

    public float getTileSize() {
        return length;
    }

    public int getWidth(float x) {
        return (int) ((x - field.leftTop.x) / length);
    }

    public float getTileX(int width, Gravity gravity) {
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
        return field.leftTop.x + (width + ax) * length;
    }

    public int getHeight(float y) {
        return (int) ((y - field.leftTop.y) / length);
    }

    public float getTileY(int height, Gravity gravity) {
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
        return field.leftTop.y + (height + ay) * length;
    }
    public boolean setObjectOnTile(Transform transform, int width, int height){
        return setObjectOnTile(transform, width, height, Gravity.CENTER);
    }

    public  boolean swapObject(Transform t1, Transform t2){
        // 전제 : 두 객체 모두 field 혹은 bench에 있고, rigid_body라서 등록되어 있음
        if(t1.isRigid() && t2.isRigid()){
            int width = getWidth(t2.getPosition().x);
            int height = getHeight(t2.getPosition().y);
            int index = getIndex(t2.getPosition().x, t2.getPosition().y);

            int origin_width = getWidth(t1.getPosition().x);
            int origin_height = getHeight(t1.getPosition().y);
            int origin_index = getIndex(t1.getPosition().x, t1.getPosition().y);
            // 두 놈 다 field, bench에 있는지 확인
            // 만약 둘 다 온전하게 등록된 상태로 있다면
            // 둘의 등록 상태를 지워버리고
            if(bench.isCorrectWidth(index)) {
                Log.d("Bench", "t2 index : " + index);
                bench.set(index, null);
            }
            else if(field.isCorrect(width, height) &&
                    field.get(width, height) != null)
                field.set(width, height, null);
            else
                return false;

            if(bench.isCorrectWidth(origin_index)){
                Log.d("Bench", "t1 index : " + origin_index);
                bench.set(origin_index, null);
            }
            else if(field.isCorrect(origin_width, origin_height) &&
                    field.get(origin_width, origin_height) != null)
                field.set(origin_width, origin_height, null);
            else
                return false;
            // 위치를 바꿔서 등록한다
            float temp_x = t1.getPosition().x;
            float temp_y = t1.getPosition().y;
            t1.goTo(t2.getPosition().x, t2.getPosition().y);
            t2.goTo(temp_x, temp_y);

            return setPositionNear(t1) && setPositionNear(t2);
        }
        return false;
    }

    public boolean setObjectOnTile(Transform transform, int width, int height, Gravity gravity){
        if(!field.isCorrect(width, height) || (transform.isRigid() && field.get(width, height) != null)
         || field.block(width, height)){
            return false;
        }

        float x = getTileX(width, gravity);
        float y = getTileY(height, gravity);
        transform.goTo(x, y);

        if(transform.isRigid()) {
            return putOnBoard(transform);
        }
        return true;
    }

    public boolean setObjectOnBench(Transform transform, int index){
        if(!bench.isCorrectWidth(index) || (transform.isRigid() && bench.get(index) != null)){
            return false;
        }
        float x = bench.leftTop.x + length * (index + 0.5f);
        float y = bench.leftTop.y + length/2;
        transform.goTo(x, y);
        if(transform.isRigid())
            return putOnBench(transform);
        return true;
    }

    public boolean setPositionNear(Transform transform){
        return setPositionNear(transform, Gravity.CENTER);
    }
    public boolean setPositionNear(Transform transform, Gravity gravity){
        if(field.dstRect.contains(transform.getPosition().x, transform.getPosition().y)){
            int targetWidth = getWidth(transform.getPosition().x);
            int targetHeight = getHeight(transform.getPosition().y);
            return setObjectOnTile(transform, targetWidth, targetHeight, gravity);
        }else if(bench.dstRect.contains(transform.getPosition().x, transform.getPosition().y)){
            return setObjectOnBench(transform, getIndex(transform.getPosition().x, transform.getPosition().y));
        }else{
            return false;
        }
    }

    public void removeObject(float x, float y){
        int width = getWidth(x);
        int height = getHeight(y);
        int index = getIndex(x, y);
        Log.d("GameMap Remove Function", "width : " + width + ", height : " + height + ", index : " + index);
        if(field.isCorrect(width, height) && field.get(width, height) != null){
            IGameObject temp = field.get(width, height).getInstance();
            field.set(width, height, null);
        }else if(bench.isCorrectWidth(index) && bench.get(index) != null){
            IGameObject temp = bench.get(width).getInstance();
            bench.set(width, null);
        }
    }

    private boolean putOnBoard(Transform transform){
        int width = getWidth(transform.getPosition().x);
        int height = getHeight(transform.getPosition().y);

        if(width < 0 || width >= field.width){
            return false;
        }
        if(height < 0 || height >= field.height) {
            return false;
        }

        if(field.get(width, height) == null &&
            !field.full()) {
            field.set(width, height, transform);
            return true;
        }
        return false;
    }
    private boolean putOnBench(Transform transform){
        int index = getIndex(transform.getPosition().x, transform.getPosition().y);
        if(bench.isCorrectWidth(index) && bench.get(index) == null) {
            bench.set(index, transform);
            return true;
        }
        return false;
    }

    private float origin_x;
    private float origin_y;
    //private Transform pickedObjectTransform = null;
    public void setOnPredictPoint(float x, float y) {
        // 물체를 짚는 것을 지시
        // 이 때 이미 짚은 물체가 있다면 어떻게 처리할 것인지 고민해봐야됨
        // 일단은 에러 로그를 표시하고, 그냥 새 짚는 명령 무시하는 걸로
        if(floatObjectOn){
            Log.d("Boardmap", "already float object is exist");
           return;
        }
        if(field.dstRect.contains(x, y)) {
            int width = getWidth(x);
            int height = getHeight(y);
            if(field.get(width, height) != null) {
                activatePredictPoint(x, y);
                field.set(width, height, null);
            }
        }else if (bench.dstRect.contains(x, y)) {
            int index = getIndex(x, y);
            if(index >= 0 && index < bench.width && bench.get(index) != null) {
                activatePredictPoint(x, y);
                bench.set(index, null);
            }
        }
    }

    private void activatePredictPoint(float x, float y){
        predictPoint.goTo(x, y);
        setPositionNear(predictPoint);
        origin_x = predictPoint.getPosition().x;
        origin_y = predictPoint.getPosition().y;
        floatObjectOn = true;
    }

    public void movePredictPoint(float x, float y) {
        if(!floatObjectOn)
            return;
        predictPoint.goTo(x, y);
        setPositionNear(predictPoint);
    }

    public void setOffPredictPoint(float x, float y) {
        if(floatObjectOn) {
            floatObjectOn = false;
            predictPoint.goTo(x, y);
        }
    }

    public boolean isSettable(Transform transform){
        return isSettable(transform.getPosition().x, transform.getPosition().y);
    }
    public boolean isSettable(float x, float y){
        if(field.dstRect.contains(x, y)){
            int width = getWidth(x);
            int height = getHeight(y);
            if(!field.block(width, height) && !field.full())
                return (field.get(width, height) == null);
            else
                return false;
        }else if(bench.dstRect.contains(x, y)){
            int width = getIndex(x, y);
            return (bench.get(width) == null);
        }
        return false;
    }

    public Transform findTransform(float x, float y){
        if(field.dstRect.contains(x, y)){
            int width = getWidth(x);
            int height = getHeight(y);
            return field.get(width, height);
        }else if(bench.dstRect.contains(x, y)) {
            int index = getIndex(x, y);
            return bench.get(index);
        }else{
            return null;
        }
    }

    public Transform findTransform(int width, int height){
        return field.get(width, height);
    }

    public int getIndex(float x, float y) {
        if(bench.dstRect.contains(x, y)){
            return (int)((x - bench.leftTop.x) / length);
        }
        return -1;
    }
    public float getBenchX(int index){
        return bench.leftTop.x + length * (index + 0.5f);
    }
    public float getBenchY(){
        return bench.leftTop.y + length/2;
    }

    public int getEmptyBenchIndex(){
        for(int i = 0; i < bench.width; i++){
            if(bench.get(i) == null)
                return i;
        }
        return -1;
    }

    public void restore(){ // Battle 후 다시 prepare 상태로 되돌려 놓는 함수
        for(int i = 0; i < field.width; i++){
            for(int j = 0; j < field.height; j++){
                Transform transform = field.get(i, j);
                if(transform != null){
                    // 위치를 먼저 복구하자 전투 동안 움직였을 테니
                    transform.set(getTileX(i, Gravity.CENTER), getTileY(j, Gravity.CENTER));
                }
            }
        }
    }

    public int getCountMax() {
        return field.countMax;
    }

    public int getFieldCount() {
        return field.getCount();
    }

    public float getButtonLine() {
        return ((field.leftTop.y + field.dstRect.height()) + (bench.leftTop.y))/2;
    }

    public Transform getBenchTransform(int i) {
        return bench.get(i);
    }

    public void setDrawBlocked(boolean drawBlocked) {
        this.drawBlocked = drawBlocked;
    }

    public void getEnemyPostions(ArrayList<Position> enemyPositions, int numEnemy) {
        for(int i = 0; i < numEnemy; i++){
            Position randomPosition;
            if(i < enemyPositions.size()) {
                randomPosition = enemyPositions.get(i);
            }else{
                randomPosition = new Position();
                enemyPositions.add(randomPosition);
            }
            int[] width_height = field.getBlockRandom();
            randomPosition.x = getTileX(width_height[0], Gravity.CENTER);
            randomPosition.y = getTileY(width_height[1], Gravity.CENTER);
        }
    }
}

