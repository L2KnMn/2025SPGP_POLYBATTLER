package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object; // 동일한 패키지 사용 가정

import android.graphics.Canvas;
import android.graphics.Color; // 직접 색상 코드를 사용하기 위해 임포트
import android.graphics.Paint;

// Polyman에서 사용했던 필요한 클래스들을 임포트합니다.
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager; // AI를 위해 필요
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.GameMap;   // AI를 위해 필요
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.Sprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

// 클래스 이름을 Enemy로 변경
public class Enemy extends Sprite {
    public final Transform transform;
    private final Paint paint;
    private ShapeType shape;
    // ColorType은 사용하지 않음 (검은색 고정)

    // 유닛 데이터 구조는 Polyman과 동일하게 유지 (추후 적 전용 스탯으로 수정 가능)
    protected static class UnitData {
        int hp = 80; // 예시: 적 체력
        int hpMax = 80;
        int attack = 8; // 예시: 적 공격력
        float attackPerSecond = 0.9f; // 예시: 적 공격 속도
        int defense = 0; // 예시: 적 방어력
        int speed = 1;
        float attackRange = 1.8f; // 예시: 적 공격 사거리

        protected void reset() {
            hp = hpMax;
            // 필요하다면 여기에 적 종류별 기본 스탯 설정
        }
    }

    // 상태 머신 구조 유지
    private enum ObjectState {
        IDLE, BATTLE, WAIT
    }
    private enum BattleState {
        IDLE, MOVE, ATTACK, DEAD
    }

    private UnitData unitData;
    private ObjectState state = ObjectState.IDLE;
    private BattleState battleState = BattleState.IDLE;
    private int level = 1; // 적 레벨 (추후 활용)

    // AI 구현을 위한 참조 변수 (의존성 주입 방식 사용 예시)
    private final GameManager gameManager;
    private final GameMap gameMap;
    private Polyman currentTarget = null; // 적은 플레이어(Polyman)를 타겟으로 함
    private float attackCooldownTimer = 0f;
    private float detectionRange = 6.0f; // 적의 플레이어 탐지 범위 (예시)

    /**
     * Enemy 생성자
     * @param shape 적의 모양
     * @param gameManager 게임 정보 접근을 위한 GameManager 참조
     * @param gameMap 맵 정보 접근을 위한 GameMap 참조
     */
    public Enemy(ShapeType shape, GameManager gameManager, GameMap gameMap) {
        super(0); // 리소스 ID가 없으면 0
        this.shape = shape;
        this.gameManager = gameManager;
        this.gameMap = gameMap;

        transform = new Transform(this);
        transform.setSize(Metrics.GRID_UNIT); // 크기는 Polyman과 동일하게 설정
        transform.setRigid(true); // 적도 충돌 판정 가짐

        paint = new Paint();
        unitData = new UnitData();

        init(); // 초기화 메서드 호출
    }

    /**
     * Enemy 상태 초기화 (객체 풀링 등에서 재사용 시 호출 가능)
     */
    public void init() {
        // 색상은 검은색으로 고정
        paint.setColor(Color.BLACK); // android.graphics.Color.BLACK 사용
        paint.setStyle(Paint.Style.FILL);

        // 위치 초기화 (생성 후 외부에서 설정)
        transform.set(0, 0);
        transform.setAngle(0); // 각도 초기화

        unitData.reset(); // 스탯 초기화
        state = ObjectState.IDLE; // 상태 초기화
        battleState = BattleState.IDLE;
        currentTarget = null; // 타겟 초기화
        attackCooldownTimer = 0f;
    }

    @Override
    public void update() {
        // Polyman과 유사한 업데이트 로직
        switch (state) {
            case IDLE:
                // 적의 기본 상태 애니메이션 (예: 제자리에서 약간 회전)
                transform.turnRight(20 * GameView.frameTime); // 플레이어와 반대 방향?
                break;
            case BATTLE:
                BattleAction(); // 전투 상태일 경우 AI 로직 실행
                break;
            case WAIT:
                // 대기 상태 로직 (필요시 구현)
                break;
        }
    }

    /**
     * 적의 전투 AI 로직
     */
    private void BattleAction() {
        if (battleState == BattleState.DEAD) return; // 죽었으면 아무것도 안 함

        // 여기에 적의 AI 로직 구현 (Polyman의 AI 로직과 유사하게 시작)
        switch (battleState) {
            case IDLE: // 타겟(Polyman) 탐색
                currentTarget = findTarget(); // 플레이어 유닛 찾는 로직 필요
                if (currentTarget != null) {
                    if (isInAttackRange(currentTarget)) {
                        battleState = BattleState.ATTACK;
                        attackCooldownTimer = 0f;
                    } else {
                        battleState = BattleState.MOVE;
                    }
                }
                break;
            case MOVE: // 타겟에게 이동
                if (currentTarget == null || currentTarget.isDead()) { // Polyman에 isDead() 필요
                    battleState = BattleState.IDLE;
                    currentTarget = null;
                    break;
                }
                if (isInAttackRange(currentTarget)) {
                    battleState = BattleState.ATTACK;
                    attackCooldownTimer = 0f;
                } else {
                    moveTowardsTarget(currentTarget); // 이동 로직
                }
                break;
            case ATTACK: // 타겟 공격
                if (currentTarget == null || currentTarget.isDead()) {
                    battleState = BattleState.IDLE;
                    currentTarget = null;
                    break;
                }
                if (!isInAttackRange(currentTarget)) {
                    battleState = BattleState.MOVE;
                    break;
                }
                attackCooldownTimer -= GameView.frameTime;
                if (attackCooldownTimer <= 0f) {
                    performAttack(currentTarget);
                    if (unitData.attackPerSecond > 0) {
                        attackCooldownTimer = 1.0f / unitData.attackPerSecond;
                    } else {
                        attackCooldownTimer = Float.MAX_VALUE; // 공격 불가
                    }
                }
                break;
            case DEAD:
                // 죽었을 때 처리 (예: GameManager에 제거 요청)
                // gameManager.removeEnemy(this); // GameManager에 이런 메서드 필요
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Polyman과 동일한 그리기 로직 사용 (paint 객체가 검은색을 가짐)
        canvas.save();
        canvas.rotate(transform.getAngle(), transform.getPosition().x, transform.getPosition().y);
        switch (shape) {
            case RECTANGLE:
                canvas.drawRect(transform.getRect(), paint);
                break;
            case CIRCLE:
                canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, transform.getSize() / 2, paint);
                break;
            case TRIANGLE:
                // transform.getTriangle() 메서드가 구현되어 있어야 함
                canvas.drawPath(transform.getTriangle(), paint);
                break;
            default:
                break;
        }
        canvas.restore();
    }

    // Polyman의 inPoint 메서드와 동일 (버그 수정된 버전)
    public boolean inPoint(Position point) {
        switch (shape) {
            case RECTANGLE:
                if (transform.getRect().contains(point.x, point.y)) {
                    return true;
                }
                break; // 빠졌던 break 추가
            case CIRCLE:
                // 거리 제곱으로 비교하는 것이 더 효율적
                float dx = point.x - transform.getPosition().x;
                float dy = point.y - transform.getPosition().y;
                float radius = transform.getSize() / 2;
                if ((dx * dx + dy * dy) < (radius * radius)) {
                    return true;
                }
                break; // 빠졌던 break 추가
            case TRIANGLE:
                if (transform.isPointInTriangle(point)) { // 이 메서드가 Transform에 구현되어 있다고 가정
                    return true;
                }
                break; // 일관성을 위해 break 추가
            default:
                return false;
        }
        // 로직상 여기까지 오면 안 되지만, 만약을 위해 false 반환
        return false;
    }

    // 전투 시작/종료 및 상태 관리 메서드 유지
    public void startBattle() {
        state = ObjectState.BATTLE;
        battleState = BattleState.IDLE;
        transform.setAngle(0); // 전투 시작 시 각도 초기화
        // 전투 시작 시 체력을 최대로 할지, 현재 체력으로 할지 결정 필요
        // unitData.hp = unitData.hpMax;
    }

    public void damage(int damage) {
        if (state == ObjectState.BATTLE && battleState != BattleState.DEAD) {
            int finalDamage = Math.max(1, damage - unitData.defense); // 방어력 적용
            unitData.hp -= finalDamage;
            if (unitData.hp <= 0) {
                unitData.hp = 0; // 체력이 음수가 되지 않도록
                battleState = BattleState.DEAD;
                // 죽었음을 GameManager 등에 알리는 로직 추가 가능
                // gameManager.notifyEnemyDeath(this);
            }
        }
    }

    public void resetBattleStatus() {
        unitData.reset(); // UnitData의 reset 메서드 사용
        state = ObjectState.IDLE;
        battleState = BattleState.IDLE;
        currentTarget = null;
        attackCooldownTimer = 0f;
    }

    // 외부에서 사망 여부 확인 가능하도록 메서드 추가
    public boolean isDead() {
        return battleState == BattleState.DEAD;
    }

    // 모양 Enum (Polyman과 공유 가능하면 별도 파일로 분리 고려)
    public enum ShapeType {
        RECTANGLE, CIRCLE, TRIANGLE
    }

    // --- AI Helper Methods (적 AI에 맞게 수정 필요) ---

    // 가장 가까운 Polyman 찾기 (GameManager에 해당 기능 필요)
    private Polyman findTarget() {
        if (gameManager != null) {
            // GameManager에 플레이어 유닛(Polyman) 목록을 검색하는 메서드 필요
            return gameManager.findClosestPlayerUnit(this.transform.getPosition());
        }
        return null;
    }

    // 공격 범위 확인 (Polyman 대상)
    private boolean isInAttackRange(Polyman target) {
        if (target == null || unitData.attackRange <= 0) return false;
        float distanceSq = this.transform.distanceSq(target.transform.getPosition());
        float rangeSq = unitData.attackRange * unitData.attackRange;
        return distanceSq <= rangeSq;
    }

    // 타겟(Polyman)에게 이동
    private void moveTowardsTarget(Polyman target) {
        if (target == null) return;
        float moveSpeed = unitData.speed * GameView.frameTime;
        if (moveSpeed <= 0) return;

        Position currentPos = this.transform.getPosition();
        Position targetPos = target.transform.getPosition();
        float dx = targetPos.x - currentPos.x;
        float dy = targetPos.y - currentPos.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > moveSpeed) { // 거리가 이동량보다 클 때만 이동
            float moveX = (dx / dist) * moveSpeed;
            float moveY = (dy / dist) * moveSpeed;
            // TODO: GameMap을 이용한 경로 탐색 또는 장애물 확인 로직 추가 가능
            transform.move(moveX, moveY);
        }
    }

    // 타겟(Polyman) 공격
    private void performAttack(Polyman target) {
        if (target == null) return;
        // Log.d("Enemy", this + " attacks " + target);
        target.damage(this.unitData.attack); // Polyman의 damage 메서드 호출
        // TODO: 공격 이펙트/사운드 재생
    }

    // 디버깅용 toString
    @Override
    public String toString() {
        return "Enemy[" + shape + "@" + Integer.toHexString(hashCode()) + "]";
    }
}