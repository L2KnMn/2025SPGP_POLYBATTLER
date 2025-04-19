package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class DragAndDropManager implements IGameManager {
    private GameState currentState;
    private final GameMap gameMap;
    private boolean active;
    private Transform draggedTransform;
    private final Position dragStartPoint;
    private final Position previous;
    private boolean isDragging;
    private String TAG = "DragAndDropManager";

    public DragAndDropManager(GameMap gameMap) { // private 생성자
        this.gameMap = gameMap;
        this.draggedTransform = null;
        this.isDragging = false;
        this.dragStartPoint = new Position();
        this.previous = new Position();
        active = true;
        currentState = GameState.PREPARE;
    }

    @Override
    public void setGameState(GameState state) {
        currentState = state;
        switch (state){
            case PREPARE:
                active = true;
                break;
            case SHOPPING:
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
    public GameState getGameState() {
        return currentState;
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
                return isDragging;
            case MotionEvent.ACTION_UP:
                handleActionUp(x, y);
                return isDragging;
        }
        previous.set(x,y);
        return false ;
    }

    private boolean handleActionDown(float x, float y) {
        if (isDragging) {
            return false;
        }
        draggedTransform = gameMap.findTransform(x, y);
        if (draggedTransform != null && isDraggable(draggedTransform)) {
            dragStartPoint.set(x,y);
            previous.set(x,y);
            isDragging = true;
            gameMap.setOnPredictPoint(x, y);
            return true;
        }else {
            draggedTransform = null; // Transform이 없는 경우 드래그 실패
            return false;
        }
    }

    private void handleActionMove(float x, float y) {
        if (isDragging && draggedTransform != null) {
            float deltaX = x - previous.x;
            float deltaY = y - previous.y;
            draggedTransform.move(deltaX, deltaY);
            gameMap.movePredictPoint(x, y);
            previous.x = x;
            previous.y = y;
        }
    }

    private void handleActionUp(float x, float y) {
        if (isDragging && draggedTransform != null) {
            isDragging = false;
            draggedTransform.moveTo(x, y);
            gameMap.setOffPredictPoint(x, y);
            if(gameMap.isSettable(draggedTransform)){
                gameMap.setPositionNear(draggedTransform);
            }else{
                draggedTransform.moveTo(dragStartPoint.x, dragStartPoint.y);
                if (gameMap.setPositionNear(draggedTransform)) {
                    // 무사히 원래 자리로 돌아갔다면 자리 바꿀 수 있는 녀석들인지 검사해서
                    // 자리 바꾸기
                    Transform target = gameMap.findTransform(x, y);
                    // 놓으려 한 자리에 다른 물체가 있으면
                    if(target != null && target.isRigid()){
                        // 교체해도 되는 물체인지 확인하고 (일단은 지금은 캐릭터들만 있으니 그냥 교체하자)
                        Polyman t2 = target.getInstance() instanceof Polyman ? ((Polyman) target.getInstance()) : null;
                        // 교체 실행하기
                        if(gameMap.swapObject(draggedTransform, target)){
                            //Log.d(TAG, "두 강체 위치 교환 성공");
                        }else{
                            Log.d(TAG, "두 강체 위치 교환 실패, 둘 중 하나에 문제가 있음");
                        }
                    }
                    // 이 자리에 다른 강체가 없는 데 놓는 게 실패함
                    // -> block된 타일이나, 교환 불가능한 객체가 있는 것
                    // 그냥 뭐 더 하지 말자
                } else {
                    // 원래 자리로 돌려놓으려 했는데 실패한 것이면 뭔가 프로그램이 잘못 돌아가고 있는 것이다
                    // 일단 화면 밖으로 날려버려서 정상적인 척 하자
                    draggedTransform.moveTo(-100, -100);
                    Log.d("Drag And Drop Manager", "드랍 했을 때, 원래 자리로 되돌려 놓기가 실패하여 화면 밖에 버림");
                }
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