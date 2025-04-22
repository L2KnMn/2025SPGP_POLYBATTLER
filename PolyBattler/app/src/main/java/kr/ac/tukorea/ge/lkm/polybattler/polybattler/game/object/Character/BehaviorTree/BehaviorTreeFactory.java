package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

// 필요한 클래스들을 임포트합니다.
import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman.ShapeType; // ShapeType Enum 경로 확인
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;

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

    static BiPredicate<BattleUnit, BattleManager> hasTarget;
    static BiPredicate<BattleUnit, BattleManager> isTargetInRange;
    static BiPredicate<BattleUnit, BattleManager> isAttackReady;

    // 실제 트리 생성 로직
    private static void buildAndCacheNodes() {
        // --- 1. 공통 액션/조건 노드 정의 (람다식 활용) ---

        // Action: 타겟 찾기 (성공/실패)
        findTargetAction = new ActionNode((unit, manager) -> {
            //Log.d("find target action", String.valueOf(System.identityHashCode(unit)));
            if (manager == null) return BTStatus.FAILURE;
            // BattleManager에게 가장 가까운 적 찾기 요청 (BattleUnit 타입 반환 가정)
            BattleUnit target = manager.findClosestEnemy(unit);
            unit.setCurrentTarget(target); // 찾은 타겟을 유닛 내부에 저장
            return (target != null) ? BTStatus.SUCCESS : BTStatus.FAILURE;
        });

        // Action: 단일 타겟 공격 (성공/실패/진행중)
        attackSingleTargetAction = new ActionNode((unit, manager) -> {
            BattleUnit target = unit.getCurrentTarget();
            // 타겟 없거나 죽었으면 실패
            if (target == null || target.isDead()) return BTStatus.FAILURE;
            // 공격 쿨다운 준비 안됐으면 진행중
            if (!unit.isAttackReady()) return BTStatus.RUNNING;

            // 공격 실행 및 쿨다운 초기화
            unit.attackTarget(target); // BattleUnit의 공격 메서드 호출
            unit.resetAttackCooldown();
            // System.out.println(unit + " attacks " + target);
            return BTStatus.SUCCESS; // 공격 성공 (이번 틱에)
        });

        // Action: 범위 타겟 공격 (성공/실패/진행중) - Circle 용
        attackAreaAction = new ActionNode((unit, manager) -> {
            // 타겟 개념이 다를 수 있음 (예: 가장 가까운 적 그룹 중심?)
            // 여기서는 일단 현재 타겟 주변 범위로 가정
            BattleUnit target = unit.getCurrentTarget();
            if (target == null || target.isDead()) return BTStatus.FAILURE; // 기준 타겟 필요
            if (!unit.isAttackReady()) return BTStatus.RUNNING;

//             BattleManager에게 범위 내 적 목록 요청
//             List<BattleUnit> targetsInArea = manager.findEnemiesInArea(target.getTransform().getPosition(), areaAttackRange);
//             for (BattleUnit enemy : targetsInArea) {
//                 unit.applyAreaDamage(enemy); // 범위 데미지 적용 메서드 호출
//             }
            //System.out.println(unit + " performs AREA ATTACK around " + target);
            unit.resetAttackCooldown();
            return BTStatus.SUCCESS;
        });

        // Action: 타겟에게 이동 (성공/실패/진행중)
        moveToTargetAction = new ActionNode((unit, manager) -> {
            BattleUnit target = unit.getCurrentTarget();
            if (target == null || target.isDead()) return BTStatus.FAILURE;
            // 이미 범위 내에 있으면 성공
            if (unit.isTargetInRange()) {
                unit.stopMovement(); // 이동 멈춤 (필요시 BattleUnit에 구현)
                return BTStatus.SUCCESS;
            }
            // 이동 명령 및 상태 반환
            unit.moveTo(target.getTransform()); // BattleUnit의 이동 메서드 호출
            return unit.isMovementComplete() ? BTStatus.SUCCESS : BTStatus.RUNNING;
        });

        // Condition: 타겟이 있고 살아있는가?
        hasTarget = (unit, manager) ->
                unit.getCurrentTarget() != null && !unit.getCurrentTarget().isDead();

        // Condition: 타겟이 공격 범위 내에 있는가?
        isTargetInRange = (unit, manager) ->
                hasTarget.test(unit, manager) && manager != null && unit.isTargetInRange();

        // Condition: 공격 준비가 되었는가? (쿨다운 완료)
        isAttackReady = (unit, manager) ->
                unit.isAttackReady();
    }

    private BehaviorTreeFactory(){}
    /**
     * 지정된 ShapeType에 맞는 BehaviorTree를 반환합니다.
     * @param shapeType 유닛의 ShapeType
     * @return 미리 생성된 BehaviorTree 객체 (없으면 null)
     */
    private static BehaviorTree createNewTreeForShape(ShapeType shapeType) {
        BehaviorTree treeCache = null;
        switch (shapeType){
            case CIRCLE:
                treeCache = new BehaviorTree(
                        new Selector(
                                "Route Node",
                                // 1순위: 공격 가능하면 범위 공격
                                new Sequence(
                                        "Attack Area",
                                        new ConditionNode(hasTarget), // 범위 공격을 위한 타겟 선정 방식 필요시 수정
                                        new ConditionNode(isTargetInRange), // 범위 공격 사거리
                                        new ConditionNode(isAttackReady),
                                        attackAreaAction // 범위 공격 액션 사용
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
                break;
            case RECTANGLE:
                treeCache = new BehaviorTree(
                        new Selector( // 우선순위대로 시도
                                "Route Node",
                                // 1순위: 공격 가능하면 공격
                                new Sequence(
                                        "Attack Single",
                                        new ConditionNode(hasTarget),
                                        new ConditionNode(isTargetInRange), // 근접해야 함
                                        new ConditionNode(isAttackReady),
                                        attackSingleTargetAction
                                ),
                                // 2순위: 타겟 있으면 이동
                                new Sequence(
                                        "Move to target",
                                        new ConditionNode(hasTarget),
                                        moveToTargetAction
                                ),
                                // 3순위: 타겟 없으면 찾기
                                findTargetAction
                        )
                );
                break;
            case TRIANGLE:
                treeCache = new BehaviorTree(
                        new Selector( // 동일한 구조, 단지 유닛의 attackRange가 다름
                                "Route Node",
                                // 1순위: 공격 가능하면 공격
                                new Sequence(
                                        "Attack Single",
                                        new ConditionNode(hasTarget),
                                        new ConditionNode(isTargetInRange), // 원거리
                                        new ConditionNode(isAttackReady),
                                        attackSingleTargetAction
                                ),
                                // 2순위: 타겟 있으면 이동 (추후 Kiting 등 추가 가능)
                                new Sequence(
                                        "Move to target",
                                        new ConditionNode(hasTarget),
                                        moveToTargetAction
                                ),
                                // 3순위: 타겟 없으면 찾기
                                findTargetAction
                        )
                );
                break;
        }
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

    public static void releaseTree(ShapeType shapeType, BehaviorTree tree){
        if(tree == null)
            return;
        tree.reset(); // 혹시 모르니 상태 초기화!
        ArrayList<BehaviorTree> pool = treePools.computeIfAbsent(shapeType, k -> new ArrayList<>());
        pool.add(tree);
    }
}