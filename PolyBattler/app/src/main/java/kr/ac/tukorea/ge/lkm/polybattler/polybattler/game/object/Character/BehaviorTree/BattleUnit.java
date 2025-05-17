package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import android.os.Build;
import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.AttackEffect;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

import java.util.ArrayList;
import java.util.List; // List Import

public class BattleUnit {
    final Transform transform;
    private BehaviorTree behaviorTree;
    private BattleController battleController;
    private AttackEffect attackEffect;

    BattleController.Team team;
    Polyman.ShapeType shapeType;
    Polyman.ColorType colorType;

    // 기본 능력치
    private int baseMaxHp;
    private int baseAttack;
    private int baseDefense;
    private float baseAttackRange;
    private float baseAttackPerSecond;
    private float baseAreaRange;
    private int baseSpeed;

    // 시너지/버프 적용으로 인한 추가 능력치 (초기값 0)
    private int synergyMaxHpBonus = 0;
    private int synergyAttackBonus = 0;
    private int synergyDefenseBonus = 0;
    private float synergyAttackRangeBonus = 0;
    private float synergyAttackPerSecondBonus = 0;
    private float synergyAreaRangeBonus = 0;
    private int synergySpeedBonus = 0;

    // 현재 능력치 (기본 + 시너지 + 기타 버프/디버프 합산)
    private int currentHp; // 현재 체력
    private int currentMaxHp;
    private int currentAttack;
    private int currentDefense;
    private float currentAttackRange;
    private float currentAttackPerSecond;
    private float currentAreaRange;
    private int currentSpeed;

    private long lastAttackTime;
    private BattleUnit target;
    private final Position velocity;

    // 레벨 정보 추가
    private int level;


    public BattleUnit(Transform transform, Polyman.ShapeType shapeType, Polyman.ColorType colorType, int level){ // 레벨 파라미터 추가
        this.transform = transform;
        behaviorTree = null;
        battleController = null;
        velocity = new Position();
        this.level = level; // 레벨 초기화
        reset(shapeType, colorType, level); // reset 호출 시 레벨 전달
    }

    public void reset(Polyman.ShapeType shapeType, Polyman.ColorType colorType, int level){ // 레벨 파라미터 추가
        this.shapeType = shapeType;
        this.colorType = colorType;
        this.level = level; // 레벨 초기화
        preset(shapeType, colorType, level); // preset 호출 시 레벨 전달
        fillHp(currentMaxHp); // 현재 최대 체력으로 체력 채움
        target = null;
        // 시너지 보너스 초기화 (새로운 라운드 시작 시 필요할 수 있습니다)
        resetSynergyBonuses();
    }

    // 레벨 및 타입에 따른 기본 능력치 설정
    public void preset(Polyman.ShapeType shapeType, Polyman.ColorType colorType, int level) { // 레벨 파라미터 추가
        // TODO: 실제 게임 밸런스에 맞춰 능력치 테이블 구현 필요
        // 여기서는 레벨에 따른 간단한 능력치 증가 로직 예시를 보여줍니다.
        float levelMultiplier = 1.0f + (level - 1) * 0.5f; // 레벨업당 능력치 50% 증가 예시

        this.baseDefense = 0;
        this.baseSpeed = 1;
        this.baseAreaRange = 0; // 기본값

        switch (shapeType){
            case CIRCLE:
                this.baseAttackRange = Metrics.GRID_UNIT * 5;
                this.baseAttackPerSecond = 3f/5f;
                this.baseAttack = 9;
                this.baseMaxHp = 80; // 예시 체력
                this.baseAreaRange = Metrics.GRID_UNIT;
                break;
            case RECTANGLE:
                this.baseDefense = 1;
                this.baseAttackRange = Metrics.GRID_UNIT * 1;
                this.baseAttackPerSecond = 3f/3f;
                this.baseAttack = 11;
                this.baseMaxHp = 120; // 예시 체력
                break;
            case TRIANGLE:
                this.baseAttackRange = Metrics.GRID_UNIT * 6;
                this.baseAttackPerSecond = 3f/4f;
                this.baseAttack = 11;
                this.baseMaxHp = 90; // 예시 체력
                break;
        }

        // 레벨에 따른 기본 능력치 조정
        this.baseMaxHp = (int)(this.baseMaxHp * levelMultiplier);
        this.baseAttack = (int)(this.baseAttack * levelMultiplier);
        this.baseDefense = (int)(this.baseDefense * levelMultiplier);
        // 공격 범위, 공격 속도, 범위 범위는 레벨에 따라 크게 바뀌지 않는 경우가 많지만,
        // 필요하다면 여기서 레벨에 따라 조정할 수 있습니다.
        // this.baseAttackRange *= levelMultiplier;
        // this.baseAttackPerSecond *= levelMultiplier;
        // this.baseAreaRange *= levelMultiplier;

        // 현재 능력치를 기본 능력치로 초기화
        updateCurrentStats();

        lastAttackTime = 0;
        attackEffect = null;
    }

    // 시너지 보너스를 초기화하는 메소드
    public void resetSynergyBonuses() {
        synergyMaxHpBonus = 0;
        synergyAttackBonus = 0;
        synergyDefenseBonus = 0;
        synergyAttackRangeBonus = 0;
        synergyAttackPerSecondBonus = 0;
        synergyAreaRangeBonus = 0;
        synergySpeedBonus = 0;
        updateCurrentStats(); // 능력치 갱신
    }

    // 시너지 버프를 적용하는 메소드 (SynergyEffect 객체 필요)
    public void applySynergyBuff(SynergyEffect effect) {
        // TODO: SynergyEffect 타입에 따라 해당 보너스 변수에 값을 더하는 로직 구현
        // 예시:
        switch (effect.getType()) {
            case ATTACK_BONUS:
                synergyAttackBonus += effect.getValue();
                break;
            case DEFENSE_BONUS:
                synergyDefenseBonus += effect.getValue();
                break;
            case MAX_HP_BONUS:
                synergyMaxHpBonus += effect.getValue();
                // 최대 체력 증가 시 현재 체력도 비례하여 증가시키거나 그대로 두는 정책 결정 필요
                int oldMaxHp = currentMaxHp;
                updateCurrentStats(); // 먼저 최대 체력 갱신
                currentHp += (currentMaxHp - oldMaxHp); // 늘어난 최대 체력만큼 현재 체력도 증가
                break;
            // 다른 시너지 효과 타입에 대한 처리 추가
            case ATTACK_SPEED_BONUS:
                synergyAttackPerSecondBonus += effect.getValue();
                break;
            case ATTACK_RANGE_BONUS:
                synergyAttackRangeBonus += effect.getValue();
                break;
            case AREA_RANGE_BONUS:
                synergyAreaRangeBonus += effect.getValue();
                break;
            case SPEED_BONUS:
                synergySpeedBonus += (int)effect.getValue(); // 속도는 int일 경우 캐스팅
                break;
            // TODO: 체력 회복, 보호막, 특수 효과 등은 별도의 로직 필요
        }
        updateCurrentStats(); // 능력치 갱신
    }

    // 시너지 버프를 제거하는 메소드 (시너지 조건 미충족 시)
    public void removeSynergyBuff(SynergyEffect effect) {
        // TODO: SynergyEffect 타입에 따라 해당 보너스 변수에서 값을 빼는 로직 구현
        // 예시:
        switch (effect.getType()) {
            case ATTACK_BONUS:
                synergyAttackBonus -= effect.getValue();
                break;
            case DEFENSE_BONUS:
                synergyDefenseBonus -= effect.getValue();
                break;
            case MAX_HP_BONUS:
                // 최대 체력 감소 시 현재 체력이 최대 체력보다 높으면 최대 체력으로 맞춤
                synergyMaxHpBonus -= effect.getValue();
                break;
            // 다른 시너지 효과 타입에 대한 처리 추가
            case ATTACK_SPEED_BONUS:
                synergyAttackPerSecondBonus -= effect.getValue();
                break;
            case ATTACK_RANGE_BONUS:
                synergyAttackRangeBonus -= effect.getValue();
                break;
            case AREA_RANGE_BONUS:
                synergyAreaRangeBonus -= effect.getValue();
                break;
            case SPEED_BONUS:
                synergySpeedBonus -= (int)effect.getValue();
                break;
        }
        updateCurrentStats(); // 능력치 갱신
    }

    // 기본 능력치와 시너지 보너스를 합산하여 현재 능력치를 갱신하는 메소드
    private void updateCurrentStats() {
        if(this.currentMaxHp < this.baseMaxHp + this.synergyMaxHpBonus){
            // 최대 체력이 늘어나면 늘어난만큼 현재 hp 회복
            currentHp += this.baseMaxHp + this.synergyMaxHpBonus - this.currentMaxHp;;
            this.currentMaxHp = this.baseMaxHp + this.synergyMaxHpBonus;
        }else {
            this.currentMaxHp = this.baseMaxHp + this.synergyMaxHpBonus;
            // 현재 체력이 최대 체력을 초과하지 않도록 보정
            if (currentHp > currentMaxHp) {
                currentHp = currentMaxHp;
            }
        }
        this.currentAttack = this.baseAttack + this.synergyAttackBonus;
        this.currentDefense = this.baseDefense + this.synergyDefenseBonus;
        this.currentAttackRange = this.baseAttackRange + this.synergyAttackRangeBonus;
        this.currentAttackPerSecond = this.baseAttackPerSecond + this.synergyAttackPerSecondBonus;
        this.currentAreaRange = this.baseAreaRange + this.synergyAreaRangeBonus;
        this.currentSpeed = this.baseSpeed + this.synergySpeedBonus;
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    public int getMaxHp(){
        return currentMaxHp; // 현재 최대 체력 반환
    }
    public int getHp(){
        return currentHp; // 현재 체력 반환
    }

    public void damage(int damage){
        int actualDamage = Math.max(0, damage - currentDefense); // 현재 방어력 적용
        currentHp -= actualDamage;
        EffectManager.getInstance(Scene.top()).createDamageTextEffect(transform.getPosition().x + (float)Math.random() * 25.0f, transform.getPosition().y, actualDamage);
        if(isDead()){
            stopAttackEffect();
        }
    }

    public void fillHp(int hp){
        this.currentHp = Math.min(this.currentHp + hp, currentMaxHp); // 현재 최대 체력까지만 회복
    }

    public void setCurrentTarget(BattleUnit target) {
        BattleUnit prevTarget = this.target;
        this.target = target;
        if(target != null && prevTarget != target) {
            initAttackEffect();
        }else {
            stopAttackEffect();
        }
    }

    public BattleUnit getCurrentTarget() {
        return target;
    }

    public Transform getTransform() {
        return transform;
    }

    public boolean isTargetInRange() {
        return (target != null) && (target.getTransform().distance(transform) <= currentAttackRange); // 현재 공격 범위 적용
    }

    public float getAttackPercent() {
        // 현재 공격 속도 적용
        return Math.clamp((System.currentTimeMillis() - lastAttackTime) / (1000.0f / currentAttackPerSecond), 0.0f, 1.0f);
    }

    public boolean isAttackReady() {
        // 현재 공격 속도 적용
        if(lastAttackTime != 0) {
            return (System.currentTimeMillis() - lastAttackTime) >= (1000 / currentAttackPerSecond);
        } else {
            lastAttackTime = System.currentTimeMillis();
            return false;
        }
    }

    public void attackTarget(BattleUnit target) {
        target.damage(currentAttack); // 현재 공격력 적용
        if(target.isDead()) {
            battleController.killSign(this, target);
        }
        resetAttackCooldown();
    }

    public void resetAttackCooldown() {
        lastAttackTime = 0;
    }

    public void stopMovement() {
        velocity.set(0, 0);
    }

    public float getSpeed() {
        return currentSpeed * Metrics.GRID_UNIT * GameView.frameTime; // 현재 이동 속도 적용
    }

    public void moveTo(Transform transform) {
        if(this.transform.distance(transform) <= getSpeed()){
            this.transform.goTo(transform);
        }
        else{
            velocity.makeVector(this.transform.position, transform.position, getSpeed());
            this.transform.move(velocity.x, velocity.y);
        }
    }

    public boolean isMovementComplete() {
        return isTargetInRange();
    }

    public void setTeam(BattleController.Team team) {
        this.team = team;
    }
    public BattleController.Team getTeam() {
        return team;
    }

    public Polyman.ShapeType getShapeType() {
        return shapeType;
    }
    public Polyman.ColorType getColorType() { return colorType; }

    public int getLevel() { return level; } // 레벨 반환 메소드 추가

    public void setBehaviorTree(BehaviorTree behaviorTree, BattleController battleController) {
        this.behaviorTree = behaviorTree;
        this.battleController = battleController;
    }
    public BehaviorTree getBehaviorTree() {
        return behaviorTree;
    }

    public void tick(){
        if(behaviorTree != null && battleController != null) {
            behaviorTree.tick(this, battleController);
            if(target != null) {
                transform.lookAt(target.getTransform().getPosition());
            }
        }
    }

    public void initAttackEffect(){
        if(attackEffect == null){
            attackEffect = Scene.top().getRecyclable(AttackEffect.class);
        }
        attackEffect.init(this, target);
    }

    public void stopAttackEffect() {
        if(attackEffect != null) {
            attackEffect.remove();
            attackEffect = null;
        }
    }

    public float getAreaRange() {
        return currentAreaRange; // 현재 범위 공격 범위 적용
    }

    public void setShapeType(Polyman.ShapeType shape) {
        this.shapeType = shape;
    }

    public void setColorType(Polyman.ColorType color) {
        this.colorType = color;
    }

    // 시너지 효과 정의를 위한 간단한 클래스 (별도의 파일로 분리 가능)
    public static class SynergyEffect {
        public enum EffectType {
            ATTACK_BONUS, DEFENSE_BONUS, MAX_HP_BONUS,
            ATTACK_SPEED_BONUS, ATTACK_RANGE_BONUS, AREA_RANGE_BONUS, SPEED_BONUS
            // TODO: 다른 효과 타입 추가 가능서 (치명타, 흡혈, 보호막 등)
        }

        private final EffectType type;
        private final float value; // 정수형 능력치 보너스

        public SynergyEffect(EffectType type, float value) {
            this.type = type;
            this.value = value;
        }

        public EffectType getType() { return type; }
        public float getValue() { return value; }
    }
}