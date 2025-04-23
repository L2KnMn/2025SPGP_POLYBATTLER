package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

import android.util.Log;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
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
    float attackPerSecond = 1;
    float attackRange = Metrics.GRID_UNIT;
    private float lastAttackTime;
    int defense = 0;
    int speed = 1; // 1초에 몇 칸 움직일 수 있는가
    private BattleUnit target = null;
    private final Position velocity;

    public BattleUnit(Transform transform){
        this.transform = transform;
        behaviorTree = null;
        battleManager = null;
        velocity = new Position();
    }

    public void reset(Polyman.ShapeType shapeType, Polyman.ColorType colorType){
        this.shapeType = shapeType;
        this.colorType = colorType;
        hp = maxHp;
        fillHp(maxHp);
        attack=10;
        attackPerSecond=1;
        defense=0;
        speed=1;
        lastAttackTime = 0;
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
        boolean result = target != null && target.getTransform().distance(transform.getPosition()) <= attackRange;
        Log.d(System.identityHashCode(this) + "isTargetInRange", "target:" + result);
        return result;
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
    }

    public void resetAttackCooldown() {
        lastAttackTime = System.currentTimeMillis();
    }

    public void stopMovement() {
        velocity.set(0, 0);
    }

    public float getSpeed() {
        return speed * Metrics.GRID_UNIT;

    }
    public void moveTo(Transform transform) {
        if(this.transform.distance(transform) <= getSpeed()){
            this.transform.goTo(transform);
        }
        else{
            velocity.makeVector(this.transform.position, transform.position, getSpeed());
            transform.move(velocity.x * GameView.frameTime, velocity.y * GameView.frameTime);
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
