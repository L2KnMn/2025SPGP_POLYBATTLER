package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character;

import android.util.Log;
import android.view.MotionEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Collectors Import

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit.SynergyEffect; // SynergyEffect Import
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.IEventHandle;
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
        // ShapeType 시너지 예시 (2/4마리)
        Map<Integer, List<SynergyEffect>> rectangleSynergy = new HashMap<>();
        rectangleSynergy.put(2, List.of(new SynergyEffect(SynergyEffect.EffectType.DEFENSE_BONUS, 3)));
        rectangleSynergy.put(4, List.of(new SynergyEffect(SynergyEffect.EffectType.DEFENSE_BONUS, 6),
                new SynergyEffect(SynergyEffect.EffectType.MAX_HP_BONUS, 50))); // 방어력 및 체력 보너스
        shapeSynergies.put(Polyman.ShapeType.RECTANGLE, rectangleSynergy);

        Map<Integer, List<SynergyEffect>> circleSynergy = new HashMap<>();
        circleSynergy.put(2, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_SPEED_BONUS, 0.2f))); // 공격 속도 20% 증가
        circleSynergy.put(4, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_SPEED_BONUS, 0.5f),
                new SynergyEffect(SynergyEffect.EffectType.AREA_RANGE_BONUS, Metrics.GRID_UNIT * 0.5f))); // 공격 속도 및 범위 범위 증가
        shapeSynergies.put(Polyman.ShapeType.CIRCLE, circleSynergy);

        Map<Integer, List<SynergyEffect>> triangleSynergy = new HashMap<>();
        triangleSynergy.put(2, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_BONUS, 5))); // 공격력 증가 (예시)
        triangleSynergy.put(4, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_BONUS, 10),
                new SynergyEffect(SynergyEffect.EffectType.ATTACK_RANGE_BONUS, Metrics.GRID_UNIT))); // 공격력 및 공격 범위 증가 (예시)
        shapeSynergies.put(Polyman.ShapeType.TRIANGLE, triangleSynergy);


        // ColorType 시너지 예시 (2/4/6마리)
        Map<Integer, List<SynergyEffect>> redSynergy = new HashMap<>();
        redSynergy.put(2, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_BONUS, 4)));
        redSynergy.put(4, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_BONUS, 10), new SynergyEffect(SynergyEffect.EffectType.SPEED_BONUS, 1)));
        redSynergy.put(6, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_BONUS, 20), new SynergyEffect(SynergyEffect.EffectType.SPEED_BONUS, 2)));
        colorSynergies.put(Polyman.ColorType.RED, redSynergy);

        Map<Integer, List<SynergyEffect>> greenSynergy = new HashMap<>();
        greenSynergy.put(2, List.of(new SynergyEffect(SynergyEffect.EffectType.MAX_HP_BONUS, 20)));
        greenSynergy.put(4, List.of(new SynergyEffect(SynergyEffect.EffectType.MAX_HP_BONUS, 50), new SynergyEffect(SynergyEffect.EffectType.DEFENSE_BONUS, 5)));
        greenSynergy.put(6, List.of(new SynergyEffect(SynergyEffect.EffectType.MAX_HP_BONUS, 100), new SynergyEffect(SynergyEffect.EffectType.DEFENSE_BONUS, 10)));
        colorSynergies.put(Polyman.ColorType.GREEN, greenSynergy);

        Map<Integer, List<SynergyEffect>> blueSynergy = new HashMap<>();
        // 파랑 시너지는 적에게 공격 속도 감소 디버프를 적용
        blueSynergy.put(2, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_SPEED_BONUS, -0.1f)));
        blueSynergy.put(4, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_SPEED_BONUS, -0.25f)));
        blueSynergy.put(6, List.of(new SynergyEffect(SynergyEffect.EffectType.ATTACK_SPEED_BONUS, -0.5f)));
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
        synergyDisplay.updateSynergies(this.getActiveSynergies());
        synergyDisplay.setVisibility(false); // 시너지 패널은 항상 보이도록 설정 (또는 게임 상태에 따라 제어)
        UiManager.Button button = UiManager.getInstance(master).addButton("시너지 확인", Metrics.GRID_UNIT, Metrics.GRID_UNIT, Metrics.GRID_UNIT, Metrics.GRID_UNIT,
                synergyDisplay::open
        ).setVisibility(GameState.PREPARE, true).setVisibility(GameState.BATTLE, true);
        synergyDisplay.addOpener(button);
    }

    // 시너지 계산 및 적용 (아군 유닛 목록과 적 유닛 목록 모두 필요)
    public void calculateAndApplySynergies(List<BattleUnit> friendlyUnits, List<BattleUnit> enemyUnits) {
        // 이전 시너지 효과 모두 제거 (아군과 적 모두)
        removeAllSynergies(friendlyUnits, enemyUnits);
        activeSynergies.clear();

        if (friendlyUnits == null || friendlyUnits.isEmpty()) {
            return;
        }

        // ShapeType별 유닛 수 계산 (아군만 해당)
        Map<Polyman.ShapeType, Integer> shapeCounts = new HashMap<>();
        for (BattleUnit unit : friendlyUnits) {
            shapeCounts.put(unit.getShapeType(), shapeCounts.getOrDefault(unit.getShapeType(), 0) + 1);
        }

        // ColorType별 유닛 수 계산 (아군만 해당)
        Map<Polyman.ColorType, Integer> colorCounts = new HashMap<>();
        for (BattleUnit unit : friendlyUnits) {
            colorCounts.put(unit.getColorType(), colorCounts.getOrDefault(unit.getColorType(), 0) + 1);
        }

        // ShapeType 시너지 적용 (아군에게만 적용)
        for (Map.Entry<Polyman.ShapeType, Integer> entry : shapeCounts.entrySet()) {
            Polyman.ShapeType shapeType = entry.getKey();
            int count = entry.getValue();

            if (shapeSynergies.containsKey(shapeType)) {
                Map<Integer, List<SynergyEffect>> tiers = shapeSynergies.get(shapeType);
                List<Integer> sortedTiers = new ArrayList<>(tiers.keySet());
                sortedTiers.sort(Integer::compareTo); // 낮은 단계부터 정렬

                for (int tier : sortedTiers) {
                    if (count >= tier) {
                        List<SynergyEffect> effects = tiers.get(tier);
                        if (effects != null) {
                            for (SynergyEffect effect : effects) {
                                // 해당 ShapeType 유닛들에게 버프 적용
                                for (BattleUnit unit : friendlyUnits) {
                                    if (unit.getShapeType() == shapeType) {
                                        unit.applySynergyBuff(effect);
                                    }
                                }
                                activeSynergies.add(new ActiveSynergy(shapeType, null, tier, effect)); // 활성화된 시너지 목록에 추가
                            }
                        }
                    }
                }
            }
        }

        // ColorType 시너지 적용 (아군 및 적에게 적용될 수 있음)
        for (Map.Entry<Polyman.ColorType, Integer> entry : colorCounts.entrySet()) {
            Polyman.ColorType colorType = entry.getKey();
            int count = entry.getValue();

            if (colorSynergies.containsKey(colorType)) {
                Map<Integer, List<SynergyEffect>> tiers = colorSynergies.get(colorType);
                List<Integer> sortedTiers = new ArrayList<>(tiers.keySet());
                sortedTiers.sort(Integer::compareTo); // 낮은 단계부터 정렬

                for (int tier : sortedTiers) {
                    if (count >= tier) {
                        List<SynergyEffect> effects = tiers.get(tier);
                        if (effects != null) {
                            for (SynergyEffect effect : effects) {
                                // 효과 타입에 따라 아군 또는 적에게 적용
                                if (effect.getType() == SynergyEffect.EffectType.ATTACK_SPEED_BONUS && effect.getValue() < 0) {
                                    // 적에게 적용되는 디버프
                                    if (enemyUnits != null) {
                                        for (BattleUnit enemyUnit : enemyUnits) {
                                            enemyUnit.applySynergyBuff(effect);
                                        }
                                    }
                                } else {
                                    // 아군에게 적용되는 버프
                                    for (BattleUnit unit : friendlyUnits) {
                                        if (unit.getColorType() == colorType) {
                                            unit.applySynergyBuff(effect);
                                        }
                                    }
                                }
                                activeSynergies.add(new ActiveSynergy(null, colorType, tier, effect)); // 활성화된 시너지 목록에 추가
                            }
                        }
                    }
                }
            }
        }

        // TODO: UI에 현재 활성화된 시너지 목록 및 다음 단계 시너지 조건 표시
        for(ActiveSynergy activeSynergy : activeSynergies) {
            if(activeSynergy.shapeType == null){
                Log.i("ActiveSynergy", "ColorType: " + activeSynergy.colorType + ", Tier: " + activeSynergy.tier + ", Effect: " + activeSynergy.effect.toString());
            }else{
                Log.i("ActiveSynergy", "ShapeType: " + activeSynergy.shapeType + ", Tier: " + activeSynergy.tier + ", Effect: " + activeSynergy.effect.toString());
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
        private final int tier;
        private final SynergyEffect effect;

        public ActiveSynergy(Polyman.ShapeType shapeType, Polyman.ColorType colorType, int tier, SynergyEffect effect) {
            this.shapeType = shapeType;
            this.colorType = colorType;
            this.tier = tier;
            this.effect = effect;
        }

        public Polyman.ShapeType getShapeType() { return shapeType; }
        public Polyman.ColorType getColorType() { return colorType; }
        public int getTier() { return tier; }
        public SynergyEffect getEffect() { return effect; }
    }

    // BattleController 등에서 사용할 수 있도록 현재 활성화된 시너지 목록을 반환하는 메소드
    public List<ActiveSynergy> getActiveSynergies() {
        return activeSynergies;
    }
}