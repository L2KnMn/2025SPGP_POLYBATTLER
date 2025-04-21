package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

/**
 * Behavior Tree 노드의 실행 결과를 나타내는 Enum.
 */
public enum BTStatus {
    /** 작업 또는 조건 확인이 성공적으로 완료됨 */
    SUCCESS,
    /** 작업 또는 조건 확인이 실패함 */
    FAILURE,
    /** 작업이 아직 진행 중이며 다음 틱(tick)에 이어서 실행해야 함 */
    RUNNING
}