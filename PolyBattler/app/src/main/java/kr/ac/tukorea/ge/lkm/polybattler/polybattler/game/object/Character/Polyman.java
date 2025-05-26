package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.res.ResourcesCompat;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IBoxCollidable;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.ILayerProvider;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IRecyclable;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.Sprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.util.Gauge;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class Polyman extends Sprite implements IRecyclable, ILayerProvider, IRemovable, IBoxCollidable {
    static Paint debugPaint;
    static{
        debugPaint = new Paint();
        debugPaint.setColor(0xFFFF0000);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(20);
    }

    public enum ShapeType {
        RECTANGLE, CIRCLE, TRIANGLE,
    }
    public enum ColorType {
        RED, GREEN, BLUE, BLACK
    }
    public final Transform transform;
    private final Paint paint;
    private static final Paint levelPaint;
    private enum ObjectState {
        IDLE, // 비전투 대기 상태
        BATTLE, // 전투 상태
        DEAD, // 전투 중 사망
        REMOVE, // 삭제 대기 혹은 삭제 된 상태
        WAIT // 업데이트는 안 하고 그리기만
    }
    private final BattleUnit unit;
    private ObjectState state = ObjectState.IDLE;
    private int level; // 레벨 정보

    static {
        levelPaint = new Paint();
        levelPaint.setColor(ResourcesCompat.getColor(GameView.view.getResources(), R.color.white, null));
        levelPaint.setTextSize(Metrics.GRID_UNIT * 0.3f);
        levelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public Polyman(){
        super(0);
        transform = new Transform(this);
        transform.setSize(Metrics.GRID_UNIT);
        transform.setRigid(true);

        paint = new Paint();

        this.level = 1; // 기본 레벨 1
        unit = new BattleUnit(transform, ShapeType.CIRCLE, ColorType.BLACK, this.level);
    }

    public Polyman(ShapeType shape, ColorType color){
        super(0);
        transform = new Transform(this);
        transform.setSize(Metrics.GRID_UNIT);
        transform.setRigid(true);

        paint = new Paint();
        this.level = 1; // 레벨 초기화
        unit = new BattleUnit(transform, shape, color, this.level);

        init(shape, color, level); // init 호출 시 레벨 전달
    }

    public Polyman(ShapeType shape, ColorType color, int level) { // 레벨 파라미터 추가
        super(0);
        transform = new Transform(this);
        transform.setSize(Metrics.GRID_UNIT);
        transform.setRigid(true);

        paint = new Paint();
        this.level = level; // 레벨 초기화
        unit = new BattleUnit(transform, shape, color, this.level);

        init(shape, color, level); // init 호출 시 레벨 전달
    }

    // ObjectPool에서 초기화 하기 위해 호출 (레벨 파라미터 추가)
    public void init(ShapeType shape, ColorType color, int level){
        state = ObjectState.IDLE;
        setShape(shape);
        setColorType(color);
        this.level = level; // 레벨 초기화

        transform.set(0, 0);

        paint.setColor(getColor(color));
        paint.setStyle(Paint.Style.FILL);

        resetBattleStatus(); // resetBattleStatus 내부에서 unit.reset 호출
    }

    @Override
    public void update() {
        switch (state) {
            case IDLE:
                break;
            case BATTLE:
                unit.tick();
                if(unit.isDead())
                    state = ObjectState.DEAD;
                break;
            case REMOVE:
                Scene.top().remove(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if(state == ObjectState.DEAD || state == ObjectState.REMOVE)
            return;
        canvas.save();
        canvas.rotate(transform.getAngle(), transform.getPosition().x, transform.getPosition().y);
        drawShape(canvas, transform, paint, getShape());
        canvas.restore();
        drawLevel(canvas, transform, level);
        if(state == ObjectState.BATTLE){
            float attackPercent = unit.getAttackPercent();
            Gauge gauge = new Gauge(0.1f, R.color.attackPercent, R.color.attackPercentBg);
            gauge.draw(canvas, transform.getPosition().x - 50, transform.getPosition().y + 100, 100, attackPercent);
        }

    }
    public static void drawShape(Canvas canvas, Transform transform, Paint paint, ShapeType shape) {
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
    }


    public static void drawLevel(Canvas canvas, Transform transform, int level) {
        canvas.drawText("★" + level, transform.getPosition().x, transform.getPosition().y - transform.getSize()/2 - 10, levelPaint); // 레벨 텍스트 그리기 예시
    }

    public static int getColor(ColorType color){
        Resources res = GameView.view.getResources();
        switch (color) {
            case RED:
                return ResourcesCompat.getColor(res, R.color.PolymanColorRed, null);
            case BLUE:
                return ResourcesCompat.getColor(res, R.color.PolymanColorBlue, null);
            case GREEN:
                return ResourcesCompat.getColor(res, R.color.PolymanColorGreen, null);
            case BLACK:
                return ResourcesCompat.getColor(res, R.color.PolymanColorBlack, null);
        }
        return ResourcesCompat.getColor(res, R.color.PolymanColorBlack, null);
    }

    public void startBattle() {
        state = ObjectState.BATTLE;
        transform.setAngle(0);
    }

    public void resetBattleStatus(){
        // BattleUnit reset 호출 시 레벨 정보 전달 (능력치 재설정을 위해)
        unit.reset(getShape(), getColorType(), this.level);
        state = ObjectState.IDLE;
        transform.lookAt(transform.getPosition().x, transform.getPosition().y - 1); // 시선도 초기화
    }

    public boolean isDead() {
        return unit.isDead();
    }

    public BattleUnit getBattleUnit() {
        return unit;
    }

    public int getLevel() { // Polyman에서도 레벨을 얻을 수 있도록 추가
        return level;
    }

    // 레벨업 처리 메소드
    public void levelUp() {
        this.level++;
        // BattleUnit의 능력치를 새로운 레벨에 맞게 업데이트
        unit.preset(getShape(), getColorType(), this.level);
        unit.fillHp(unit.getMaxHp()); // 레벨업 시 체력 완전 회복 등 처리
        // TODO: 레벨업 시 이펙트나 사운드 추가
    }


    @Override
    public void onRecycle() {
        state = ObjectState.REMOVE;
    }

    @Override
    public Layer getLayer() {
        return Layer.charater;
    }

    @Override
    public void remove() {
        state = ObjectState.REMOVE;
    }

    public void setShape(ShapeType shape) {
        getBattleUnit().setShapeType(shape);
    }
    public ShapeType getShape() {
        return getBattleUnit().getShapeType();
    }

    public void setColorType(ColorType color){
        getBattleUnit().setColorType(color);
    }
    public ColorType getColorType() {
        return getBattleUnit().getColorType();
    }

    @Override
    public RectF getCollisionRect() {
        return transform.getRect();
    }
}