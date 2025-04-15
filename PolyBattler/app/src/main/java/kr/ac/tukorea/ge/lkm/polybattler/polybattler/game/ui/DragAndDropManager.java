package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Map;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class DragAndDropManager implements IGameManager {
    private final Map map;
    private boolean active;
    private Transform draggedTransform;
    private final Position dragStartPoint;
    private final Position previous;
    private boolean isDragging;

    public DragAndDropManager(Map map) { // private 생성자
        this.map = map;
        this.draggedTransform = null;
        this.isDragging = false;
        this.dragStartPoint = new Position();
        this.previous = new Position();
        active = true;
    }

    @Override
    public void setGameState(GameState state) {
        switch (state){
            case PREPARE:
            case SHOPPING:
                active = true;
                break;
            case BATTLE:
            case RESULT:
            case POST_GAME:
                active = false;
                if (draggedTransform != null) {
                    handleActionUp(dragStartPoint.x, dragStartPoint.y);
                }
                break;
        }
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (!active) return true;

        float[] xy = Metrics.fromScreen(event.getX(), event.getY());
        float x = xy[0];
        float y = xy[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleActionDown(x, y);
            case MotionEvent.ACTION_MOVE:
                handleActionMove(x, y);
                return !isDragging;
            case MotionEvent.ACTION_UP:
                handleActionUp(x, y);
                return !isDragging;
        }
        previous.set(x,y);
        return true;
    }

    private boolean handleActionDown(float x, float y) {
        if (isDragging) {
            return true;
        }
        draggedTransform = map.findTransform(x, y);
        if (draggedTransform != null && isDraggable(draggedTransform)) {
            dragStartPoint.set(x,y);
            previous.set(x,y);
            isDragging = true;
            map.setOnPredictPoint(x, y);
            return false;
        }else {
            draggedTransform = null; // Transform이 없는 경우 드래그 실패
            return true;
        }
    }

    private void handleActionMove(float x, float y) {
        if (isDragging && draggedTransform != null) {
            float deltaX = x - previous.x;
            float deltaY = y - previous.y;
            draggedTransform.move(deltaX, deltaY);
            map.movePredictPoint(x, y);
            previous.x = x;
            previous.y = y;
        }
    }

    private void handleActionUp(float worldX, float worldY) {
        if (isDragging && draggedTransform != null) {
            isDragging = false;
            map.setOffPredictPoint(worldX, worldY);
            if(map.isSettable(draggedTransform)){
                map.setPositionNear(draggedTransform);
            }else{
                draggedTransform.moveTo(dragStartPoint.x, dragStartPoint.y);
                map.setPositionNear(draggedTransform);
            }
            draggedTransform = null;
        }
    }

    private boolean isDraggable(Transform transform) {
        return transform.isRigid();
    }

    public IGameObject getDraggedObjectInstance() {
        if (draggedTransform != null) {
            return draggedTransform.getInstance();
        }
        return null;
    }

    public boolean isDragging() {
        return isDragging;
    }
}