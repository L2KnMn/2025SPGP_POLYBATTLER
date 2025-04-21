package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;
import java.util.function.BiPredicate; // Predicate 사용 예시

/**
 * 특정 조건(람다식으로 정의 가능)을 확인하는 노드.
 * 조건이 참이면 SUCCESS, 거짓이면 FAILURE를 반환합니다.
 */
public class ConditionNode implements BTNode {
    // BiPredicate<BattleUnit, BattleManager> 사용 예시
    private final BiPredicate<BattleUnit, BattleManager> conditionLogic;

    /**
     * @param conditionLogic 조건을 검사하는 로직. (unit, manager) -> boolean 형태의 람다식.
     */
    public ConditionNode(BiPredicate<BattleUnit, BattleManager> conditionLogic) {
        this.conditionLogic = conditionLogic;
    }

    @Override
    public BTStatus tick(BattleUnit unit, BattleManager battleManager) {
        // System.out.println("Ticking ConditionNode");
        boolean result = conditionLogic.test(unit, battleManager);
        // System.out.println("Condition result: " + result);
        return result ? BTStatus.SUCCESS : BTStatus.FAILURE;
    }

    // Condition 노드는 보통 상태가 없으므로 reset은 비워둠
}
