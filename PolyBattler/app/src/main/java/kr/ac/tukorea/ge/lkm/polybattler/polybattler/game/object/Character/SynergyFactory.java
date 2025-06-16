package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit.SynergyEffect; // SynergyEffect Import
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.SynergyDisplay;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class SynergyFactory {
    private final SynergyDisplay synergyDisplay;
    // 현재 활성화된 시너지 효과 목록
    private List<ActiveSynergy> activeSynergies = new ArrayList<>();

    // 시너지 조건 및 효과 정의 (데이터 기반으로 분리하는 것이 좋음)
    private static final Map<Polyman.ShapeType, Map<Integer, List<SynergyEffect>>> shapeSynergies = new HashMap<>();
    private static final Map<Polyman.ColorType, Map<Integer, List<SynergyEffect>>> colorSynergies = new HashMap<>();

    static {
        // ShapeType 시너지 (2/4마리)
        Map<Integer, List<SynergyEffect>> rectangleSynergy = new HashMap<>();
        rectangleSynergy.put(2, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.DEFENSE_BONUS, 3, 1)));
        rectangleSynergy.put(4, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.DEFENSE_BONUS, 6, 2),
                new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.MAX_HP_BONUS, 50, 2))); // 방어력 및 체력 보너스
        shapeSynergies.put(Polyman.ShapeType.RECTANGLE, rectangleSynergy);

        Map<Integer, List<SynergyEffect>> circleSynergy = new HashMap<>();
        circleSynergy.put(2, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_SPEED_BONUS, 0.2f, 1))); // 공격 속도 20% 증가
        circleSynergy.put(4, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_SPEED_BONUS, 0.5f, 2),
                new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.AREA_RANGE_BONUS, 1, 2))); // 공격 속도 및 범위 범위 증가
        shapeSynergies.put(Polyman.ShapeType.CIRCLE, circleSynergy);

        Map<Integer, List<SynergyEffect>> triangleSynergy = new HashMap<>();
        triangleSynergy.put(2, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_BONUS, 5, 1))); // 공격력 증가 (예시)
        triangleSynergy.put(4, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_BONUS, 10, 2),
                new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_RANGE_BONUS, 1, 2))); // 공격력 및 공격 범위 증가
        shapeSynergies.put(Polyman.ShapeType.TRIANGLE, triangleSynergy);


        // ColorType 시너지 (2/4/6마리)
        Map<Integer, List<SynergyEffect>> redSynergy = new HashMap<>();
        redSynergy.put(2, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_BONUS, 4, 1)));
        redSynergy.put(4, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_BONUS, 10, 2),
                new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.SPEED_BONUS, 0.2f, 2)));
        redSynergy.put(6, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.ATTACK_BONUS, 20, 3),
                new SynergyEffect(BattleController.Team.PLAYER,SynergyEffect.EffectType.SPEED_BONUS, 0.5f, 3)));
        colorSynergies.put(Polyman.ColorType.RED, redSynergy);

        Map<Integer, List<SynergyEffect>> greenSynergy = new HashMap<>();
        greenSynergy.put(2, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.MAX_HP_BONUS, 20, 1)));
        greenSynergy.put(4, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.MAX_HP_BONUS, 50, 2),
                new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.DEFENSE_BONUS, 5, 2)));
        greenSynergy.put(6, List.of(new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.MAX_HP_BONUS, 100, 3),
                new SynergyEffect(BattleController.Team.PLAYER, SynergyEffect.EffectType.DEFENSE_BONUS, 10, 3)));
        colorSynergies.put(Polyman.ColorType.GREEN, greenSynergy);

        Map<Integer, List<SynergyEffect>> blueSynergy = new HashMap<>();
        // 파랑 시너지는 적에게 공격 속도 감소 디버프를 적용
        blueSynergy.put(2, List.of(new SynergyEffect(BattleController.Team.ENEMY, SynergyEffect.EffectType.ATTACK_SPEED_BONUS, -0.1f, 1)));
        blueSynergy.put(4, List.of(new SynergyEffect(BattleController.Team.ENEMY, SynergyEffect.EffectType.ATTACK_SPEED_BONUS, -0.25f, 2)));
        blueSynergy.put(6, List.of(new SynergyEffect(BattleController.Team.ENEMY, SynergyEffect.EffectType.ATTACK_SPEED_BONUS, -0.5f, 3),
                new SynergyEffect(BattleController.Team.ENEMY, SynergyEffect.EffectType.ATTACK_BONUS, -1, 3))); // 추가로 적 공격력도 감소
        colorSynergies.put(Polyman.ColorType.BLUE, blueSynergy);
    }

    public SynergyFactory(Scene master) {
        // SynergyDisplay 인스턴스 생성 및 초기 위치 설정
        // 화면 좌측에 패널 배치 예시
        float panelWidth = Metrics.GRID_UNIT * (4.5f); // 패널 너비 (그리드 4.5칸)
        float panelHeight = Metrics.height; // 화면 전체 높이
        float panelX = panelWidth / 2; // 중앙 정렬을 위해 너비의 절반
        float panelY = Metrics.height / 2; // 화면 중앙

        synergyDisplay = UiManager.getInstance(master).addSynergyDisplay(panelX, panelY, panelWidth, panelHeight);
        synergyDisplay.appendSynergies(this.getActiveSynergies()); // 자원 등록 -> 알아서 업데이트 될 것임
        synergyDisplay.setVisibility(false); // 시너지 패널은 항상 보이도록 설정 (또는 게임 상태에 따라 제어)
        UiManager.Button button = UiManager.getInstance(master).addButton("시너지 확인", Metrics.GRID_UNIT, Metrics.GRID_UNIT, Metrics.GRID_UNIT * 2, Metrics.GRID_UNIT,
                synergyDisplay::open
        ).setVisibility(GameState.PREPARE, true).setVisibility(GameState.BATTLE, true);
        synergyDisplay.addOpener(button);
    }

    // 시너지 계산 및 적용 (아군 유닛 목록과 적 유닛 목록 모두 필요)
    Map<Polyman.ShapeType, Integer> shapeCounts = new HashMap<>();
    Map<Polyman.ColorType, Integer> colorCounts = new HashMap<>();

    /**
     * 활성화된 시너지를 기록하기 위한 함수형 인터페이스
     * @param <T_CATEGORY> 시너지 카테고리 타입 (예: ShapeType, ColorType)
     */
    @FunctionalInterface
    private interface ActiveSynergyRecorder<T_CATEGORY extends Enum<T_CATEGORY>> {
        void record(T_CATEGORY categoryValue, int condition, int currentUnitCount, SynergyEffect effect);
    }

    public void calculateAndApplySynergies(List<BattleUnit> friendlyUnits, List<BattleUnit> enemyUnits) {
        // 1. 이전 시너지 효과 모두 제거
        removeAllSynergies(friendlyUnits, enemyUnits);
        activeSynergies.clear();
        if (friendlyUnits == null || friendlyUnits.isEmpty()) {
            System.out.println("아군 유닛이 없어 시너지를 계산하지 않습니다.");
            return;
        }

        // 2. ShapeType별 유닛 수 계산
        shapeCounts.clear();
        for (BattleUnit unit : friendlyUnits) {
            shapeCounts.put(unit.getShapeType(), shapeCounts.getOrDefault(unit.getShapeType(), 0) + 1);
        }

        // 3. ColorType별 유닛 수 계산
        colorCounts.clear();
        for (BattleUnit unit : friendlyUnits) {
            colorCounts.put(unit.getColorType(), colorCounts.getOrDefault(unit.getColorType(), 0) + 1);
        }

        // 4. ShapeType 시너지 적용
        applySynergiesForCategory(
                shapeCounts,
                shapeSynergies,
                friendlyUnits,
                enemyUnits,
                BattleUnit::getShapeType, // 유닛에서 ShapeType을 추출하는 함수
                (type, condition, count, effect) -> // 활성화된 Shape 시너지를 기록하는 람다
                        activeSynergies.add(new ActiveSynergy(type, null, condition, count, effect))
        );

        // 5. ColorType 시너지 적용
        applySynergiesForCategory(
                colorCounts,
                colorSynergies,
                friendlyUnits,
                enemyUnits,
                BattleUnit::getColorType, // 유닛에서 ColorType을 추출하는 함수
                (type, condition, count, effect) -> // 활성화된 Color 시너지를 기록하는 람다
                        activeSynergies.add(new ActiveSynergy(null, type, condition, count, effect))
        );
        System.out.println("총 활성화된 시너지 수: " + activeSynergies.size());
    }

    /**
     * 특정 카테고리(Shape 또는 Color)의 시너지를 처리하는 제네릭 메서드
     * @param categoryCounts      해당 카테고리의 유닛 수 맵 (예: shapeCounts)
     * @param synergiesMap        해당 카테고리의 시너지 정의 맵 (예: shapeSynergies)
     * @param friendlyUnits       아군 유닛 리스트
     * @param enemyUnits          적군 유닛 리스트
     * @param categoryExtractor   BattleUnit에서 해당 카테고리 값을 추출하는 함수
     * @param recorder            활성화된 시너지를 기록하는 함수
     * @param <T_CATEGORY>        시너지 카테고리의 타입 (Polyman.ShapeType 또는 Polyman.ColorType)
     */
    private <T_CATEGORY extends Enum<T_CATEGORY>> void applySynergiesForCategory(
            Map<T_CATEGORY, Integer> categoryCounts,
            Map<T_CATEGORY, Map<Integer, List<SynergyEffect>>> synergiesMap,
            List<BattleUnit> friendlyUnits,
            List<BattleUnit> enemyUnits,
            Function<BattleUnit, T_CATEGORY> categoryExtractor,
            ActiveSynergyRecorder<T_CATEGORY> recorder
    ) {
        if (categoryCounts == null || synergiesMap == null) {
            return;
        }

        for (Map.Entry<T_CATEGORY, Integer> entry : categoryCounts.entrySet()) {
            T_CATEGORY currentCategoryValue = entry.getKey(); // 예: ShapeType.TRIANGLE 또는 ColorType.RED
            int count = entry.getValue(); // 해당 타입의 유닛 수

            if (synergiesMap.containsKey(currentCategoryValue)) {
                Map<Integer, List<SynergyEffect>> availableBuffs = synergiesMap.get(currentCategoryValue);
                List<Integer> sortedConditions = new ArrayList<>(availableBuffs.keySet());
                sortedConditions.sort(Collections.reverseOrder()); // 높은 단계부터 적용하기 위해 내림차순 정렬

                for (int condition : sortedConditions) {
                    if (count >= condition) { // 시너지 발동 조건 충족
                        List<SynergyEffect> effectsToApply = availableBuffs.get(condition);
                        if (effectsToApply != null) {
                            for (SynergyEffect effect : effectsToApply) {
                                // 효과 적용 대상 결정
                                if (effect.applicateTeam() == BattleController.Team.ENEMY) {
                                    // 적에게 디버프 적용
                                    if (enemyUnits != null) {
                                        for (BattleUnit enemyUnit : enemyUnits) {
                                            enemyUnit.applySynergyBuff(effect);
                                        }
                                    }
                                } else { // 아군에게 버프 적용
                                    // 해당 카테고리 값을 가진 아군 유닛들에게만 버프 적용
                                    for (BattleUnit friendlyUnit : friendlyUnits) {
                                        if (categoryExtractor.apply(friendlyUnit) == currentCategoryValue) {
                                            friendlyUnit.applySynergyBuff(effect);
                                        }
                                    }
                                }
                                // 활성화된 시너지 목록에 추가 (recorder 사용)
                                recorder.record(currentCategoryValue, condition, count, effect);
                            }
                        }
                        break; // 가장 높은 단계의 시너지 하나만 적용
                    }
                }
            }
        }
    }

    // 모든 유닛으로부터 시너지 효과 제거 (아군과 적 모두 고려)
    public void removeAllSynergies(List<BattleUnit> friendlyUnits, List<BattleUnit> enemyUnits) {
        // 아군 유닛 시너지 보너스 초기화
        if (friendlyUnits != null) {
            for (BattleUnit unit : friendlyUnits) {
                unit.resetSynergyBonuses();
            }
        }
        // 적 유닛 시너지 보너스 초기화
        if (enemyUnits != null) {
            for (BattleUnit unit : enemyUnits) {
                unit.resetSynergyBonuses();
            }
        }
    }

    // 현재 활성화된 시너지 목록 초기화 메소드 추가 (BattleController에서 호출)
    public void resetActiveSynergies() {
        activeSynergies.clear();
    }


    // 현재 활성화된 시너지 정보를 저장하는 헬퍼 클래스
    public static class ActiveSynergy { // public으로 변경하여 BattleController에서 접근 가능하도록
        private final Polyman.ShapeType shapeType;
        private final Polyman.ColorType colorType;
        private final int condition;
        private final int count;
        private final SynergyEffect effect;

        public ActiveSynergy(Polyman.ShapeType shapeType, Polyman.ColorType colorType, int condition, int count, SynergyEffect effect) {
            this.shapeType = shapeType;
            this.colorType = colorType;
            this.condition = condition;
            this.count = count;
            this.effect = effect;
        }

        public Polyman.ShapeType getShapeType() { return shapeType; }
        public Polyman.ColorType getColorType() { return colorType; }
        public int getCondition() { return condition; }
        public SynergyEffect getEffect() { return effect; }

        public int getTier() {
            return effect.getTier();
        }

        public int getCount() {
            return count;
        }
    }

    // BattleController 등에서 사용할 수 있도록 현재 활성화된 시너지 목록을 반환하는 메소드
    public List<ActiveSynergy> getActiveSynergies() {
        return activeSynergies;
    }
}