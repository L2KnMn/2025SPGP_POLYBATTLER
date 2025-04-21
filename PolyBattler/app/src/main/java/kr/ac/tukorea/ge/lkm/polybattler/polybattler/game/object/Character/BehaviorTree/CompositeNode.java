package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;

/**
 * 여러 자식 노드를 가지는 복합 노드의 추상 기본 클래스.
 */
public abstract class CompositeNode implements BTNode {
    protected final List<BTNode> children = new ArrayList<>();

    public CompositeNode(BTNode... children) {
        this.children.addAll(Arrays.asList(children));
    }

    public void addChild(BTNode child) {
        children.add(child);
    }

    /**
     * 이 노드가 리셋될 때 모든 자식 노드도 리셋합니다.
     */
    @Override
    public void reset() {
        // System.out.println("Resetting Composite: " + this.getClass().getSimpleName());
        for (BTNode child : children) {
            child.reset();
        }
    }

    // tick 메서드는 Sequence, Selector 등 구체적인 클래스에서 구현해야 함
    @Override
    public abstract BTStatus tick(BattleUnit unit, BattleManager battleManager);
}
