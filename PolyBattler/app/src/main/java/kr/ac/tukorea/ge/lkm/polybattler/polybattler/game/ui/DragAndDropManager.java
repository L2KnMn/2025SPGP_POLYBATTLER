package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Map;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class DragAndDropManager {
    private final Map map;
    private Transform draggedTransform;
    private float dragStartX;
    private float dragStartY;
    private float previousX;
    private float previousY;
    private boolean isDragging;

    public DragAndDropManager(Map map) {
        this.map = map;
        this.draggedTransform = null;
        this.isDragging = false;
    }

    public boolean handleTouchEvent(MotionEvent event) {
        float[] worldCoords = Metrics.fromScreen(event.getX(), event.getY());
        float worldX = worldCoords[0];
        float worldY = worldCoords[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleActionDown(worldX, worldY);
            case MotionEvent.ACTION_MOVE:
                handleActionMove(worldX, worldY);
                return isDragging;
            case MotionEvent.ACTION_UP:
                handleActionUp(worldX, worldY);
                return isDragging;
        }
        return false;
    }

    private boolean handleActionDown(float worldX, float worldY) {
        if (isDragging) {
            return false;
        }
        draggedTransform = map.findTransform(worldX, worldY);
        if (draggedTransform != null && isDraggable(draggedTransform)) {
            dragStartX = worldX;
            dragStartY = worldY;
            previousX = worldX;
            previousY = worldY;
            isDragging = true;
            map.setOnPredictPoint(worldX, worldY);
            return true;
        }else {
            draggedTransform = null; // Transform이 없는 경우 드래그 실패
        }
        return false;
    }

    private void handleActionMove(float worldX, float worldY) {
        if (isDragging && draggedTransform != null) {
            float deltaX = worldX - previousX;
            float deltaY = worldY - previousY;
            draggedTransform.move(deltaX, deltaY);
            map.movePredictPoint(worldX, worldY);
            previousX = worldX;
            previousY = worldY;
        }
    }

    private void handleActionUp(float worldX, float worldY) {
        if (isDragging && draggedTransform != null) {
            isDragging = false;
            map.setOffPredictPoint(worldX, worldY);
            map.setPositionNear(draggedTransform);
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