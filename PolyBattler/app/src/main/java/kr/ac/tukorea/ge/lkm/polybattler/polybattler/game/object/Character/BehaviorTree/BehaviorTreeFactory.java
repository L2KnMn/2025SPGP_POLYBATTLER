package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

// 필요한 클래스들을 임포트합니다.
import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman.ShapeType; // ShapeType Enum 경로 확인
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class BehaviorTreeFactory {
    private final static Map<ShapeType, ArrayList<BehaviorTree>> treePools;

    // 생성된 트리를 캐싱하기 위한 Map (EnumMap 사용 추천)
    //private static final Map<ShapeType, BehaviorTree> treeCache = new EnumMap<>(ShapeType.class);

    // 클래스 로드 시 트리를 미리 생성하여 캐시에 저장 (Static Initializer)
    static {
        treePools = new EnumMap<>(ShapeType.class);
        buildAndCacheNodes();
    }

    static BTNode findTargetAction;
    static BTNode attackSingleTargetAction;
    static BTNode attackAreaAction;
    static BTNode moveToTargetAction;

    static BiPredicate<BattleUnit, BattleController> hasTarget;
    static BiPredicate<BattleUnit, BattleController> isTargetInRange;

    // 실제 트리 생성 로직
    private static void buildAndCacheNodes() {
        // --- 1. 공통 액션/조건 노드 정의 (람다식 활용) ---

        // Action: 타겟 찾기 (성공/실패)
        findTargetAction = new ActionNode((unit, manager) -> {
            //Log.d("find target action", String.valueOf(System.identityHashCode(unit)));
            if (manager == null) {
                Log.e("findTargetAction", "BattleManager is null");
                return BTStatus.FAILURE;
            }
            // BattleManager에게 가장 가까운 적 찾기 요청 (BattleUnit 타입 반환 가정)
            BattleUnit target = manager.findClosestEnemy(unit);
            unit.setCurrentTarget(target); // 찾은 타겟을 유닛 내부에 저장
            if(target != null) {
                return BTStatus.SUCCESS;
            }else{
                return BTStatus.FAILURE;
            }
        });

        // Action: 단일 타겟 공격 (성공/실패/진행중)
        attackSingleTargetAction = new ActionNode((unit, manager) -> {
            BattleUnit target = unit.getCurrentTarget();
            // 타겟 없거나 죽었으면 실패
            if (target == null || target.isDead()) {
                unit.setCurrentTarget(null);
                return BTStatus.FAILURE;
            }
            // 공격 쿨다운 준비 안됐으면 진행중
            if (!unit.isAttackReady()){
                return BTStatus.RUNNING;
            }

            // 공격 실행 및 쿨다운 초기화
            unit.attackTarget(target); // BattleUnit의 공격 메서드 호출
            // System.out.println(unit + " attacks " + target);
            return BTStatus.SUCCESS; // 공격 성공 (이번 틱에)
        });

        // Action: 범위 타겟 공격 (성공/실패/진행중) - Circle 용
        attackAreaAction = new ActionNode((unit, manager) -> {
            // 타겟 개념이 다를 수 있음 (예: 가장 가까운 적 그룹 중심?)
            // 여기서는 일단 현재 타겟 주변 범위로 가정
            BattleUnit target = unit.getCurrentTarget();
            if (target == null || target.isDead()){
                unit.setCurrentTarget(null);
                return BTStatus.FAILURE; // 기준 타겟 필요
            }

            // 공격 쿨다운 준비 안됐으면 진행중
            if (!unit.isAttackReady()){
                return BTStatus.RUNNING;
            }

            //BattleManager에게 범위 내 적 목록 요청
            ArrayList<BattleUnit> targetsInArea = new ArrayList<>();
            manager.findEnemiesInArea(targetsInArea, target.getTransform().getPosition(), unit.getTeam(), unit.getAreaRange());
            for (BattleUnit enemy : targetsInArea) {
                unit.attackTarget(enemy);
            }
            //System.out.println(unit + " performs AREA ATTACK around " + target);
            return BTStatus.SUCCESS;
        });

        // Action: 타겟에게 이동 (성공/실패/진행중)
        moveToTargetAction = new ActionNode((unit, manager) -> {
            BattleUnit target = unit.getCurrentTarget();
            // 타겟이 이미 죽었으면 이동 실패
            if (target == null || target.isDead()) return BTStatus.FAILURE;
            // 이미 범위 내에 있으면 이동 성공
            if (unit.isTargetInRange()) {
                return BTStatus.SUCCESS;
            }
            // 이동 중인 상태
            // TODO: 현재 이동 방식 변경 중
            // * 원하는 것 : 이동은 자유롭게 하되 하나의 객체는 하나의 타일 위에 도착해서 공격한다
            // 타겟까지의 이동을 movoTo로 유닛에게 일임하는 게 아니라
            // 이 내부에서 타겟까지의 경로를 생성하고 그 경로에 이동 속도만큼을 이동
            //
            unit.moveTo(target.getTransform()); // BattleUnit의 이동 메서드 호출
            return unit.isMovementComplete() ? BTStatus.SUCCESS : BTStatus.RUNNING;
        });

        // Condition: 타겟이 있고 살아있는가?
        hasTarget = (unit, manager) ->
                unit.getCurrentTarget() != null && !unit.getCurrentTarget().isDead();

        // Condition: 타겟이 공격 범위 내에 있는가?
        isTargetInRange = (unit, manager) ->
                hasTarget.test(unit, manager) && manager != null && unit.isTargetInRange();

    }

    private BehaviorTreeFactory(){}
    /**
     * 지정된 ShapeType에 맞는 BehaviorTree를 반환합니다.
     * @param shapeType 유닛의 ShapeType
     * @return 미리 생성된 BehaviorTree 객체 (없으면 null)
     */
    private static BehaviorTree createNewTreeForShape(ShapeType shapeType) {
        BehaviorTree treeCache = null;
        treeCache = new BehaviorTree(
                new Selector(
                        "Route Node",
                        // 1순위: 공격 가능하면 범위 공격
                        new Sequence(
                                "Attack Area",
                                new ConditionNode(isTargetInRange),
                                // 타입에 따라 다른 공격 노드 사용
                                shapeType == ShapeType.CIRCLE ?
                                        attackAreaAction // 범위 공격용
                                        : attackSingleTargetAction // 단일 공격용(근원거리 동일)
                        ),
                        // 2순위: 타겟 있으면 이동 (최적 위치 선정 로직 추가 가능)
                        new Sequence(
                                "Move to target",
                                new ConditionNode(hasTarget),
                                moveToTargetAction
                        ),
                        // 3순위: 타겟 없으면 찾기
                        findTargetAction
                )
        );
        return treeCache;
    }
    public static BehaviorTree getTreeForShape(ShapeType shapeType) {
        ArrayList<BehaviorTree> pool = treePools.get(shapeType);
        BehaviorTree tree;

        if (pool != null && !pool.isEmpty()) {
            tree = pool.get(0); // 풀에서 하나 꺼냄
            pool.remove(0); // 꺼낸 트리 제거
        } else {
            // 풀이 비었거나 해당 타입 풀이 없으면 새로 생성
            tree = createNewTreeForShape(shapeType);
        }

        if (tree != null) {
            tree.reset(); // 반환 전 반드시 상태 초기화!
        }
        return tree; // null이라면 없는 shapeType을 전달한 것
    }

    public static void releaseTree(BattleUnit unit){
        Polyman.ShapeType shapeType = unit.getShapeType();
        BehaviorTree tree = unit.getBehaviorTree();
        if(tree == null)
            return;
        ArrayList<BehaviorTree> pool = treePools.computeIfAbsent(shapeType, k -> new ArrayList<>());
        pool.add(tree);
        unit.setBehaviorTree(null, null);
    }
}