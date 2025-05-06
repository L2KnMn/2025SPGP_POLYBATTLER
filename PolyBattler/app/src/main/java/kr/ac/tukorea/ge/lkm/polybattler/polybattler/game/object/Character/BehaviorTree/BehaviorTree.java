package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;

/**
 * Behavior Tree 전체를 관리하고 실행하는 클래스.
 */
public class BehaviorTree {
    private final BTNode rootNode;
    /**
     * @param rootNode 트리의 최상위 루트 노드.
     */
    public BehaviorTree(BTNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Behavior Tree를 실행합니다 (매 프레임 호출).
     * @param unit 이 트리를 사용하는 유닛.
     */
    public void tick(BattleUnit unit, BattleController battleController) {
        if (rootNode != null) {
            rootNode.tick(unit, battleController);
        }
    }

    /**
     * 트리의 모든 노드 상태를 초기화합니다. (예: 전투 시작 시 호출)
     */
    public void reset() {
        if (rootNode != null) {
            rootNode.reset();
            // System.out.println("BehaviorTree Reset");
        }
    }
}