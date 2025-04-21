package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character;

import android.graphics.Canvas;
import android.graphics.Paint;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IRecyclable;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.Sprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class Polyman extends Sprite implements IRecyclable {
    public final Transform transform;
    private final Paint paint;
    private ShapeType shape;
    private ColorType color;

    @Override
    public void onRecycle() {
        
    }

    protected static class UnitData{
        int hp = 100;
        int hpMax = 100;
        int attack = 10;
        float attackPerSecond = 1;
        int defense = 0;
        int speed = 1; // 1초에 몇 칸 움직일 수 있는가

        protected void reset(){
            hp=hpMax;
            attack=10;
            attackPerSecond=1;
            defense=0;
            speed=1;
        }
    }
    private enum ObjectState {
        IDLE, BATTLE, WAIT
    }
    private enum BattleState {
        IDLE, MOVE, ATTACK, DEAD
    }
    private UnitData unitData;
    private ObjectState state = ObjectState.IDLE;
    private BattleState battleState = BattleState.IDLE;
    private int level;

    public Polyman(ShapeType shape, ColorType color) {
        super(0);
        transform = new Transform(this);
        transform.setSize(Metrics.GRID_UNIT);
        transform.setRigid(true);

        paint = new Paint();
        unitData = new UnitData();

        init(shape, color);
    }

    // ObjectPool에서 초기화 하기 위해 호출
    public void init(ShapeType shape, ColorType color){
        this.shape = shape;
        this.color = color;

        transform.set(0, 0);

        paint.setColor(getColor());
        paint.setStyle(Paint.Style.FILL);

        unitData.reset();
    }

    @Override
    public void update() {
        switch (state) {
            case IDLE:
                transform.turnLeft(30 * GameView.frameTime);
                break;
            case BATTLE:
                BattleAction();
                break;
            case WAIT:
                break;
        }
    }

    private void BattleAction() {
        switch (battleState){
            case IDLE:
//                // 타겟 찾기 (GameManager에게 위임)
//                currentTarget = findTarget();
//                if (currentTarget != null) {
//                    if (isInAttackRange(currentTarget)) {
//                        battleState = BattleState.ATTACK;
//                        attackCooldownTimer = 0f; // 바로 공격 가능하도록
//                    } else {
//                        battleState = BattleState.MOVE;
//                    }
//                    // Log.d("Polyman", this + " found target: " + currentTarget);
//                }
                break;
            case MOVE:
//                if (currentTarget == null || currentTarget.battleState == BattleState.DEAD) {
//                    battleState = BattleState.IDLE; // 타겟 없어짐/죽음
//                    currentTarget = null;
//                    break;
//                }
//                if (isInAttackRange(currentTarget)) {
//                    battleState = BattleState.ATTACK;
//                    attackCooldownTimer = 0f;
//                } else {
//                    // 이동 로직 (GameMap 정보 활용 가능)
//                    moveTowardsTarget(currentTarget);
//                }
                 break;
            case ATTACK:
//                if (currentTarget == null || currentTarget.battleState == BattleState.DEAD) {
//                    battleState = BattleState.IDLE; // 타겟 없어짐/죽음
//                    currentTarget = null;
//                    break;
//                }
//                if (!isInAttackRange(currentTarget)) {
//                    battleState = BattleState.MOVE; // 타겟이 범위 벗어남
//                    break;
//                }
//
//                // 공격 쿨다운 처리
//                attackCooldownTimer -= GameView.frameTime; // 프레임 시간 사용
//                if (attackCooldownTimer <= 0f) {
//                    performAttack(currentTarget);
//                    // 쿨다운 초기화 (공격 속도에 따라)
//                    if (unitData.attackPerSecond > 0) {
//                        attackCooldownTimer = 1.0f / unitData.attackPerSecond;
//                    } else {
//                        attackCooldownTimer = Float.MAX_VALUE; // 공격 불가
//                    }
//                }
                break;
            case DEAD: // 죽음
                return;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(transform.getAngle(), transform.getPosition().x, transform.getPosition().y);
        switch (shape){
            case RECTANGLE:
                canvas.drawRect(transform.getRect(), paint);
                break;
            case CIRCLE:
                canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, transform.getSize()/2, paint);
                break;
            case TRIANGLE:
                canvas.drawPath(transform.getTriangle(), paint);
                break;
            default:
                break;
        }
        canvas.restore();
        //canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, transform.getSize(), paint);
    }

    private int getColor(){
        switch (color) {
            case RED:
                return 0xFFFF0000;
            case GREEN:
                return 0xFF00FF00;
            case BLUE:
                return 0xFF0000FF;
            case BLACK:
                return 0xFF000000;
            default:
                return 0xFFFFFFFF;
        }
    }
    public boolean inPoint(Position point){
        switch (shape){
            case RECTANGLE:
                if(transform.getRect().contains(point.x, point.y)){
                    return true;
                }
            case CIRCLE:
                if(transform.distance(point.x, point.y) < transform.getSize()){
                    return true;
                }
            case TRIANGLE:
                if(transform.isPointInTriangle(point)){
                    return true;
                }
            default:
                return false;
        }
    }

    public void startBattle() {
        state = ObjectState.BATTLE;
        battleState = BattleState.IDLE;
        // IDLE 애니메이션 초기화
        transform.setAngle(0);
    }

    public void damage(int damage){
        if(state == ObjectState.BATTLE) {
            unitData.hp -= damage - unitData.defense;
            if (unitData.hp <= 0) {
                battleState = BattleState.DEAD;
            }
        }
    }

    public void resetBattleStatus(){
        unitData.hp = unitData.hpMax;
        state = ObjectState.IDLE;
        battleState = BattleState.IDLE;
    }

    public enum ShapeType {
        RECTANGLE, CIRCLE, TRIANGLE
    }
    public enum ColorType {
        RED, GREEN, BLUE, BLACK
    }
    public boolean isDead() {
        return battleState == BattleState.DEAD;
    }
}
