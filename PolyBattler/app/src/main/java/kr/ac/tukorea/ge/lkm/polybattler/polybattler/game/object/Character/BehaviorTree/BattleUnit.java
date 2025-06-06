package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.AttackEffect;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IRecyclable;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class BattleUnit {
    final Transform transform;
    private BehaviorTree behaviorTree;
    private BattleController battleController;
    private final AttackEffect attackEffect;

    BattleController.Team team;
    Polyman.ShapeType shapeType;
    Polyman.ColorType colorType;
    private float currentHp;
    static class Status{ // 모든 스텟은 float를 기본으로
        float MaxHp = 0;
        float Attack = 0;
        float Defense = 0;
        float AttackRange = 0;
        float AttackPerSecond = 0;
        float AreaRange = 0;
        float Speed = 0;
    }


    // 기본 능력치
    Status base;
    Status synergy;
    Status current;

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

        base = new Status();
        synergy = new Status();
        current = new Status();

        attackEffect = Scene.top().getRecyclable(AttackEffect.class); // 이제 항상 AttackEffect를 가지고 있자

        reset(shapeType, colorType, level); // reset 호출 시 레벨 전달
    }

    public void reset(Polyman.ShapeType shapeType, Polyman.ColorType colorType, int level){ // 레벨 파라미터 추가
        this.shapeType = shapeType;
        this.colorType = colorType;
        this.level = level; // 레벨 초기화
        preset(shapeType, colorType, level); // preset 호출 시 레벨 전달
        fillHp(current.MaxHp); // 현재 최대 체력으로 체력 채움
        setCurrentTarget(null);
        // 시너지 보너스 초기화 (새로운 라운드 시작 시 필요할 수 있습니다)
        resetSynergyBonuses();
    }

    // 레벨 및 타입에 따른 기본 능력치 설정
    public void preset(Polyman.ShapeType shapeType, Polyman.ColorType colorType, int level) { // 레벨 파라미터 추가
        // TODO: 실제 게임 밸런스에 맞춰 능력치 테이블 구현 필요
        float levelMultiplier = 1.0f + (level - 1) * 0.5f; // 일단 레벨업당 능력치 50% 증가

        this.base.Defense = 0;
        this.base.Speed = 1;
        this.base.AreaRange = 0; // 기본값

        switch (shapeType){
            case CIRCLE:
                this.base.AttackRange = 3;
                this.base.AttackPerSecond = 2.0f;
                this.base.Attack = 9;
                this.base.MaxHp = 80; // 체력
                this.base.AreaRange = 1.0f;
                break;
            case RECTANGLE:
                this.base.Defense = 1;
                this.base.AttackRange = 1; // 1칸 거리까지 공격
                this.base.AttackPerSecond = 1.8f;
                this.base.Attack = 11;
                this.base.MaxHp = 120; // 체력
                break;
            case TRIANGLE:
                this.base.AttackRange = 5;
                this.base.AttackPerSecond = 1.8f;
                this.base.Attack = 11;
                this.base.MaxHp = 90; // 체력
                break;
        }

        // 레벨에 따른 기본 능력치 조정
        this.base.MaxHp = (int)(this.base.MaxHp * levelMultiplier);
        this.base.Attack = (int)(this.base.Attack * levelMultiplier);
        this.base.Defense = (int)(this.base.Defense * levelMultiplier);

        // 현재 능력치를 기본 능력치로 초기화
        updateCurrentStats();

        lastAttackTime = 0;
    }

    // 시너지 보너스를 초기화하는 메소드
    public void resetSynergyBonuses() {
        synergy.MaxHp = 0;
        synergy.Attack = 0;
        synergy.Defense = 0;
        synergy.AttackRange = 0;
        synergy.AttackPerSecond = 0;
        synergy.AreaRange = 0;
        synergy.Speed = 0;
        updateCurrentStats(); // 능력치 갱신
    }

    // 시너지 버프를 적용하는 메소드 (SynergyEffect 객체 필요)
    public void applySynergyBuff(SynergyEffect effect) {
        // TODO: SynergyEffect 타입에 따라 해당 보너스 변수에 값을 더하는 로직 구현
        // 예시:
        switch (effect.getType()) {
            case ATTACK_BONUS:
                synergy.Attack += effect.getValue();
                break;
            case DEFENSE_BONUS:
                synergy.Defense += effect.getValue();
                break;
            case MAX_HP_BONUS:
                synergy.MaxHp += effect.getValue();
                // 최대 체력 증가 시 현재 체력도 비례하여 증가시키거나 그대로 두는 정책 결정 필요
                float oldMaxHp = current.MaxHp;
                updateCurrentStats(); // 먼저 최대 체력 갱신
                currentHp += (current.MaxHp - oldMaxHp); // 늘어난 최대 체력만큼 현재 체력도 증가
                break;
            case ATTACK_SPEED_BONUS:
                synergy.AttackPerSecond += effect.getValue();
                break;
            case ATTACK_RANGE_BONUS:
                synergy.AttackRange += effect.getValue();
                break;
            case AREA_RANGE_BONUS:
                synergy.AreaRange += effect.getValue();
                break;
            case SPEED_BONUS:
                synergy.Speed += effect.getValue();
                break;
            // TODO: 체력 회복, 보호막, 특수 효과 등은 별도의 로직 필요
        }
        updateCurrentStats(); // 능력치 갱신
    }

    // 기본 능력치와 시너지 보너스를 합산하여 현재 능력치를 갱신하는 메소드
    private void updateCurrentStats() {
        if(this.current.MaxHp < this.base.MaxHp + this.synergy.MaxHp){
            // 최대 체력이 늘어나면 늘어난만큼 현재 hp 회복
            currentHp += this.base.MaxHp + this.synergy.MaxHp - this.current.MaxHp;;
            this.current.MaxHp = this.base.MaxHp + this.synergy.MaxHp;
        }else {
            this.current.MaxHp = this.base.MaxHp + this.synergy.MaxHp;
            // 현재 체력이 최대 체력을 초과하지 않도록 보정
            if (currentHp > current.MaxHp) {
                currentHp = current.MaxHp;
            }
        }
        this.current.Attack = this.base.Attack + this.synergy.Attack;
        this.current.Defense = this.base.Defense + this.synergy.Defense;
        this.current.AttackRange = this.base.AttackRange + this.synergy.AttackRange;
        this.current.AttackPerSecond = this.base.AttackPerSecond + this.synergy.AttackPerSecond;
        this.current.AreaRange = this.base.AreaRange + this.synergy.AreaRange;
        this.current.Speed = this.base.Speed + this.synergy.Speed;
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    public float getMaxHp(){
        return current.MaxHp; // 현재 최대 체력 반환
    }
    public float getHp(){
        return currentHp; // 현재 체력 반환
    }

    public void damage(float damage){
        float actualDamage = Math.max(0, damage - current.Defense); // 현재 방어력 적용
        currentHp -= actualDamage;
        EffectManager.getInstance(Scene.top()).createDamagEffects(
                transform.getPosition().x + (float)Math.random() * 25.0f,
                transform.getPosition().y,
                this, (int) Math.ceil(actualDamage)); // 데미지는 소수점으로 존재하지만 출력은 정수 단위로 올림 해서 출력
        if(isDead()){ // 이번 공격을 받고 죽었다면
            stopAttackEffect();
        }
    }

    public void fillHp(float hp){
        this.currentHp = Math.min(this.currentHp + hp, current.MaxHp); // 현재 최대 체력까지만 회복
    }

    public void setCurrentTarget(BattleUnit target) {
        BattleUnit prevTarget = this.target; // 이전 타겟 저장
        this.target = target; // 새 타겟 저장
        if(target != null && prevTarget != target) { // 새 타겟이 null이 아니고, 이전 타겟과 다르면
            // 공격 효과 새로 시작
            initAttackEffect();
        }else if(target == null) { // 타겟이 없는 상태가 되면 
            // 공격 효과 종료
            stopAttackEffect();
        }
    }

    public BattleUnit getCurrentTarget() {
        return target;
    }

    public Transform getTransform() {
        return transform;
    }

    public float getAttackRange(){
        return this.current.AttackRange;
    }

    public boolean isTargetInRange() {
        float dist = Math.abs(target.getTransform().position.x - transform.position.x);
        dist += Math.abs(target.getTransform().position.y - transform.position.y);
        dist = dist / GameManager.getInstance(Scene.top()).getGameMap().getTileSize();
        return (target != null) && (dist <= current.AttackRange); // 현재 공격 범위 적용
    }

    public float getAttackPercent() {
        // 현재 공격 속도 적용
        return Math.clamp((System.currentTimeMillis() - lastAttackTime) / (1000.0f / current.AttackPerSecond), 0.0f, 1.0f);
    }

    public boolean isAttackReady() {
        // 현재 공격 속도 적용
        if(lastAttackTime != 0) {
            return (System.currentTimeMillis() - lastAttackTime) >= (1000 / current.AttackPerSecond);
        } else {
            lastAttackTime = System.currentTimeMillis();
            return false;
        }
    }

    public void attackTarget(BattleUnit target) {
        target.damage(current.Attack); // 현재 공격력 적용
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
        // 초당 이동 칸 수 * 프레임 당 시간
        return current.Speed * Metrics.GRID_UNIT * GameView.frameTime; // 현재 이동 속도 적용
    }

    public void moveTo(Position pos){
        if(this.transform.distance(pos) <= getSpeed()){
            // complete move to destination
            this.transform.goTo(pos.x, pos.y);
            isMovementComplete = true;
        }
        else{
            velocity.makeVector(this.transform.position, pos, getSpeed());
            this.transform.move(velocity.x, velocity.y);
        }
    }

    public void moveToDestination() {
        moveTo(destination);
    }

    private boolean isMovementSetted = false;
    private boolean isMovementComplete = false;
    private Position destination = new Position();

    public void setDestination(Position pos){
        isMovementSetted = true;
        isMovementComplete = false;
        destination.set(pos);
    }
    public void resetDestination() {
        isMovementSetted = false;
        isMovementComplete = false;
    }

    public Position getDestination() {
        return destination;
    }
    public boolean isSettingMovement() {
        return isMovementSetted;
    }
    public boolean isCompleteMovement() {
        return isMovementComplete;
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

    public void tick(){ // Battle unit의 소유주가 전투 상황에서 update마다 호출하는 일종의 update 루틴
        if(behaviorTree != null && battleController != null) {
            behaviorTree.tick(this, battleController); // 행동 트리 한 틱 실행
            if(target != null) {
                transform.lookAt(target.getTransform().getPosition());
            }
        }
    }

    public void initAttackEffect(){
        attackEffect.initAnime(this, target, 0);
    }

    public void stopAttackEffect() {
        attackEffect.remove();
    }

    public float getAreaRange() {
        return current.AreaRange * Metrics.GRID_UNIT; // 현재 범위 공격 범위 적용
    }

    public void setShapeType(Polyman.ShapeType shape) {
        this.shapeType = shape;
    }
    public void setColorType(Polyman.ColorType color) {
        this.colorType = color;
    }

    // 시너지 효과 정의를 위한 간단한 클래스 (별도의 파일로 분리 가능)
    public static class SynergyEffect implements IRecyclable {
        public enum EffectType {
            ATTACK_BONUS, DEFENSE_BONUS, MAX_HP_BONUS,
            ATTACK_SPEED_BONUS, ATTACK_RANGE_BONUS, AREA_RANGE_BONUS, SPEED_BONUS
            // TODO: 다른 효과 타입 추가 가능서 (치명타, 흡혈, 보호막 등)
        }

        private BattleController.Team application;
        private EffectType type;
        private float value; // 능력치 보너스 (정수형이더라도 float로 받아서 int에 더하기)
        private int tier;

        public SynergyEffect(BattleController.Team application, EffectType type, float value, int tier) {
            this.type = type;
            this.value = value;
            this.tier = tier;
            this.application = application;
        }

        public EffectType getType() { return type; }
        public float getValue() { return value; }
        public int getTier(){ return tier; }
        public BattleController.Team applicateTeam() {
            return application;
        }

        @Override
        public void onRecycle() {
            type = null;
            value = 0;
            tier = 0;
            application = null;
        }
    }
}