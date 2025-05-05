package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class BattleUnit {
    final Transform transform;
    private BehaviorTree behaviorTree;
    private BattleManager battleManager;

    BattleManager.Team team;
    Polyman.ShapeType shapeType;
    Polyman.ColorType colorType;
    int hp = 100;
    int maxHp = 100;
    int attack = 10;
    float attackPerSecond;
    float attackRange = Metrics.GRID_UNIT;
    private long lastAttackTime;
    int defense = 0;
    int speed = 1; // 1초에 몇 칸 움직일 수 있는가
    private BattleUnit target;
    private final Position velocity;

    public BattleUnit(Transform transform, Polyman.ShapeType shapeType, Polyman.ColorType colorType){
        this.transform = transform;
        behaviorTree = null;
        battleManager = null;
        velocity = new Position();
        reset(shapeType, colorType);
    }

    public void reset(Polyman.ShapeType shapeType, Polyman.ColorType colorType){
        this.shapeType = shapeType;
        this.colorType = colorType;
        preset(shapeType, colorType);
        fillHp(maxHp);
        target = null;
    }

    public void preset(Polyman.ShapeType shapeType, Polyman.ColorType colorType) {
        defense=0;
        speed=1;
        lastAttackTime = 0;
        switch (shapeType){
            case CIRCLE:
                attackRange = Metrics.GRID_UNIT * 5;
                attackPerSecond = 5;
                attack=3;
                break;
            case RECTANGLE:
                defense=1;
                attackRange = Metrics.GRID_UNIT * 1;
                attackPerSecond = 3;
                attack=4;
                break;
            case TRIANGLE:
                attackRange = Metrics.GRID_UNIT * 6;
                attackPerSecond = 4;
                attack=4;
                break;
        }
    }
    public boolean isDead() {
        return hp <= 0;
    }

    public int getMaxHp(){
        return maxHp;
    }
    public int getHp(){
        return hp;
    }

    public void damage(int damage){
        hp -= damage - defense;
        EffectManager.getInstance(Scene.top()).createDamageTextEffect(transform.getPosition().x + (float)Math.random() * 25.0f, transform.getPosition().y, damage);
    }

    public void fillHp(int hp){
        this.hp = Math.min(this.hp + hp, maxHp);
    }

    public void setCurrentTarget(BattleUnit target) {
        this.target = target;
    }

    public BattleUnit getCurrentTarget() {
        return target;
    }

    public Transform getTransform() {
        return transform;
    }

    public boolean isTargetInRange() {
        return (target != null) && (target.getTransform().distance(transform) <= attackRange);
    }

    public float getAttackPercent() {
        return Math.clamp((System.currentTimeMillis() - lastAttackTime) / (1000.0f / attackPerSecond), 0.0f, 1.0f);
    }

    public boolean isAttackReady() {
        if(lastAttackTime != 0) {
            return (System.currentTimeMillis() - lastAttackTime) >= (1000 / attackPerSecond);
        } else {
            lastAttackTime = System.currentTimeMillis();
            return false;
        }
    }

    public void attackTarget(BattleUnit target) {
        target.damage(attack);
        if(target.isDead()) {
            battleManager.killSign(this, target);
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
        return speed * Metrics.GRID_UNIT * GameView.frameTime;
    }

    public void moveTo(Transform transform) {
//        velocity.makeVector(this.transform.position, transform.position, getSpeed());
//        this.transform.move(velocity.x, velocity.y);
        if(this.transform.distance(transform) <= getSpeed()){
            this.transform.goTo(transform);
        }
        else{
            velocity.makeVector(this.transform.position, transform.position, getSpeed());
            this.transform.move(velocity.x, velocity.y);
        }
    }

    public boolean isMovementComplete() {
        // 나중에 장애물, 혹은 다른 유닛이 차지하고 있는 칸으로 이동 불가 등을 구현했을 때를 대비해 미리 만듦
        return isTargetInRange();
    }

    public void setTeam(BattleManager.Team team) {
        this.team = team;
    }
    public BattleManager.Team getTeam() {
        return team;
    }

    public Polyman.ShapeType getShapeType() {
        return shapeType;
    }
    public void setBehaviorTree(BehaviorTree behaviorTree, BattleManager battleManager) {
        this.behaviorTree = behaviorTree;
        this.battleManager = battleManager;
    }
    public BehaviorTree getBehaviorTree() {
        return behaviorTree;
    }

    public void tick(){
        if(behaviorTree != null && battleManager != null) {
            behaviorTree.tick(this, battleManager);
        }
    }
}
