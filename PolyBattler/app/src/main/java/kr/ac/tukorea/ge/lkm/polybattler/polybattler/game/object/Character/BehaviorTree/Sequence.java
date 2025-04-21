package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;

/**
 * 자식 노드를 순서대로 실행합니다.
 * 자식 중 하나가 FAILURE 또는 RUNNING을 반환하면 즉시 해당 상태를 반환합니다.
 * 모든 자식이 SUCCESS를 반환해야 SUCCESS를 반환합니다.
 */
public class Sequence extends CompositeNode {
    private int runningChildIndex = 0; // 현재 실행 중이거나 다음에 실행할 자식 인덱스

    public Sequence(BTNode... children) {
        super(children);
    }

    @Override
    public BTStatus tick(BattleUnit unit, BattleManager battleManager) {
        for (int i = runningChildIndex; i < children.size(); ++i) {
            BTNode child = children.get(i);
            BTStatus status = child.tick(unit, battleManager);

            // System.out.println("Sequence child " + i + " returned " + status);

            if (status == BTStatus.FAILURE) {
                // System.out.println("Sequence failed at child " + i);
                child.reset(); // 실패한 자식 리셋 (선택적)
                runningChildIndex = 0; // 실패 시 다음 tick은 처음부터 다시 시작
                return BTStatus.FAILURE;
            } else if (status == BTStatus.RUNNING) {
                // System.out.println("Sequence running at child " + i);
                runningChildIndex = i; // 다음 tick에 이 자식부터 다시 실행
                return BTStatus.RUNNING;
            }
            // SUCCESS면 다음 자식으로 넘어감
            child.reset(); // 성공한 자식 리셋 (선택적, 다음 반복에 영향 없도록)
        }

        // 모든 자식이 성공적으로 완료됨
        // System.out.println("Sequence success");
        runningChildIndex = 0; // 완료 시 다음 tick은 처음부터 다시 시작
        return BTStatus.SUCCESS;
    }

    @Override
    public void reset() {
        super.reset(); // 모든 자식 리셋
        runningChildIndex = 0; // 실행 인덱스 초기화
        // System.out.println("Resetting Sequence internal state");
    }
}