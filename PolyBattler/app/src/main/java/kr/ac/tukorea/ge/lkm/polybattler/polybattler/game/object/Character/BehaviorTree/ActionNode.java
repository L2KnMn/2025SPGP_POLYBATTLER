package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;

/**
 * 특정 액션(람다식으로 정의 가능)을 수행하는 노드.
 * 액션의 결과로 SUCCESS, FAILURE, RUNNING 중 하나를 반환해야 합니다.
 */
public class ActionNode implements BTNode {
    private final BTNode actionLogic; // 람다식 또는 BTNode 인터페이스 구현 객체

    /**
     * @param actionLogic 실행할 로직. (unit, manager) -> BTStatus 형태의 람다식.
     */
    public ActionNode(BTNode actionLogic) {
        this.actionLogic = actionLogic;
    }

    @Override
    public BTStatus tick(BattleUnit unit, BattleController battleController) {
        // System.out.println("Ticking ActionNode");
        return actionLogic.tick(unit, battleController);
    }

    @Override
    public void reset() {
        // 액션이 상태를 가진다면 여기서 리셋 필요
        actionLogic.reset(); // 위임 가능
        // System.out.println("Resetting ActionNode");
    }
}
