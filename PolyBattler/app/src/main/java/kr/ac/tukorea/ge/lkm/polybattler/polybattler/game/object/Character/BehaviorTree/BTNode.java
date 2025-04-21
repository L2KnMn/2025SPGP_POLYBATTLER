package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;

/**
 * Behavior Tree의 모든 노드가 구현해야 하는 기본 인터페이스.
 */
@FunctionalInterface // 자식 노드가 없는 경우 람다식으로 구현 가능
public interface BTNode {
    /**
     * 노드의 로직을 실행합니다.
     * @param unit 이 행동을 실행하는 유닛 객체.
     * @param battleManager 전투 상황 및 월드 정보를 제공하는 컨텍스트 객체.
     * @return 노드 실행 후의 상태 (SUCCESS, FAILURE, RUNNING).
     */
    BTStatus tick(BattleUnit unit, BattleManager battleManager);

    /**
     * 노드가 RUNNING 상태였다가 중단되거나 다시 시작될 때 내부 상태를 초기화하기 위한 메서드 (선택적).
     * 기본적으로 아무것도 하지 않습니다. 상태를 가지는 노드는 이 메서드를 오버라이드해야 할 수 있습니다.
     */
    default void reset() {}
}
