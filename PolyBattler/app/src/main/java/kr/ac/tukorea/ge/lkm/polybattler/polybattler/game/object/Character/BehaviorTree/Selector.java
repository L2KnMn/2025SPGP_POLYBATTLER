package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;

/**
 * 자식 노드를 순서대로 실행합니다.
 * 자식 중 하나가 SUCCESS 또는 RUNNING을 반환하면 즉시 해당 상태를 반환합니다.
 * 모든 자식이 FAILURE를 반환해야 FAILURE를 반환합니다. (Fallback 역할)
 */
public class Selector extends CompositeNode {
    private int runningChildIndex = 0; // 현재 실행 중이거나 다음에 실행할 자식 인덱스
    private String name;

    public Selector(String name, BTNode... children) {
        super(children);
        this.name = name;
    }

    @Override
    public BTStatus tick(BattleUnit unit, BattleController battleController) {
        for (int i = runningChildIndex; i < children.size(); ++i) {
            BTNode child = children.get(i);
            BTStatus status = child.tick(unit, battleController);

            //System.out.println("Selector " + name + ": child "  + i + " returned " + status);

            if (status == BTStatus.SUCCESS) {
                // System.out.println("Selector success at child " + i);
                child.reset(); // 성공한 자식 리셋 (선택적)
                runningChildIndex = 0; // 성공 시 다음 tick은 처음부터 다시 시작
                return BTStatus.SUCCESS;
            } else if (status == BTStatus.RUNNING) {
                // System.out.println("Selector running at child " + i);
                runningChildIndex = i; // 다음 tick에 이 자식부터 다시 실행
                return BTStatus.RUNNING;
            }
            // FAILURE면 다음 자식으로 넘어감
            child.reset(); // 실패한 자식 리셋 (선택적)
        }

        // 모든 자식이 실패함
        // System.out.println("Selector failure");
        runningChildIndex = 0; // 모두 실패 시 다음 tick은 처음부터 다시 시작
        return BTStatus.FAILURE;
    }

    @Override
    public void reset() {
        super.reset(); // 모든 자식 리셋
        runningChildIndex = 0; // 실행 인덱스 초기화
        // System.out.println("Resetting Selector internal state");
    }
}
