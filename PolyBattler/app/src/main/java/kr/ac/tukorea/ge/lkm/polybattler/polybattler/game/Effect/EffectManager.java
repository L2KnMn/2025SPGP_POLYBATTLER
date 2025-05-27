package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.IRemovable;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Coin;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.ILayerProvider;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IRecyclable;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.content.res.ResourcesCompat;

import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class EffectManager implements IGameManager {
    private static final String TAG = "EffectManager";
    private static final Map<Scene, EffectManager> instances = new HashMap<>();
    private final Scene master;
    private final List<Effect> effects;
    private final Paint textPaint;
    private final Paint damageTextPaint;
    private final Random random = new Random();
    private GameState currentState = GameState.PREPARE;

    private EffectManager(Scene master) {
        this.master = master;
        this.effects = new ArrayList<>();
        this.textPaint = new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);

        damageTextPaint = new Paint();
        damageTextPaint.setTextSize(50);
        damageTextPaint.setColor(Color.RED);
    }

    public static EffectManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, EffectManager::new);
    }

    // 이펙트 추가
    public void addEffect(Effect effect) {
        effects.add(effect);
        master.add(effect); // Scene에 추가
    }

    // 특정 위치에 데미지 텍스트 이펙트 생성 및 추가
    public void createDamagEffects(float x, float y, BattleUnit victim, int damage) {
        DamageTextEffect damageText = master.getRecyclable(DamageTextEffect.class);
        if (damageText == null) {
            damageText = new DamageTextEffect(x, y, damage, damageTextPaint);
        }else{
            damageText.init(x, y, damage, damageTextPaint);
        }
        addEffect(damageText);

        HitEffect hitEffect = master.getRecyclable(HitEffect.class);
        if (hitEffect == null) {
            hitEffect = new HitEffect();
        }
        hitEffect.init(victim, damage); // victim은 BattleUnit
        addEffect(hitEffect);
    }

    // 공격 범위 표시 이펙트
    public void createAttackRangeEffect(float x, float y, float range, float duration) {
        CircleEffect attackRange = new CircleEffect().init(x, y, range, Color.YELLOW, duration);
        addEffect(attackRange);
    }

//    // 타격 효과 이펙트
//    public void createHitEffect(float x, float y, float size, float duration) {
//        CircleEffect hitEffect = new CircleEffect(x, y, size, Color.RED, duration);
//        addEffect(hitEffect);
//    }

    // 터치 효과 이펙트
    public void createTouchEffect(float x, float y) {
        CircleEffect touchEffect = new CircleEffect().init(x, y, 20, Color.BLUE, 0.3f); // 짧은 지속 시간
        addEffect(touchEffect);
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (currentState == GameState.BATTLE) {
            createTouchEffect(event.getX(), event.getY()); // 터치 이펙트 생성
            return false; // 이벤트 소비
        }
        return false;
    }

    @Override
    public IGameManager setGameState(GameState state) {
        clearAllEffects();
        this.currentState = state;
        return this;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }

    public void clearAllEffects() {
        for (Effect effect : effects) {
            effect.remove();
        }
        effects.clear();
    }

    // 이펙트 추상 클래스
    public static abstract class Effect implements IGameObject, IRecyclable, ILayerProvider, IRemovable {
        protected float duration;
        protected float elapsedTime;
        protected boolean finished;
        protected Transform transform;
        protected boolean remove = false;

        public Effect(){
            this.duration = 1;
            elapsedTime = 0;
            finished = false;
            transform = new Transform(this, -100, -100);
        }

        public Effect(float x, float y, float duration) {
            this.duration = duration;
            elapsedTime = 0;
            finished = false;
            transform = new Transform(this, x, y);
        }

        public boolean isFinished() {
            return finished;
        }

        @Override
        public void update() {
            elapsedTime += GameView.frameTime;
            if (elapsedTime >= duration) {
                finished = true;
                remove();
            }
            if(remove){
                Scene.top().remove(this);
            }
        }

        @Override
        public void draw(Canvas canvas){}

        public Transform getTransform() {
            return transform;
        }

        @Override
        public void onRecycle(){
            elapsedTime = 0;
            finished = false;
            remove = false;
        }

        @Override
        public Layer getLayer() {
            return Layer.effect_front;
        }

        @Override
        public void remove(){
            remove = true;
        }
    }

    // 데미지 텍스트 이펙트
    public static class DamageTextEffect extends Effect {
        private int damage;
        private Paint damageTextPaint;
        private ValueAnimator fadeOutAnimator;

        public DamageTextEffect(){
            super();
            damage = 0;
            damageTextPaint = null;
        }

        public DamageTextEffect(float x, float y, int damage, Paint paint) {
            super();
            init(x, y, damage, paint);
        }

        public void init(float x, float y, int damage, Paint paint){
            finished = false;
            elapsedTime = 0;
            transform.set(x, y);
            this.damage = damage;
            this.remove = false;
            this.damageTextPaint = paint;

            if (fadeOutAnimator != null && fadeOutAnimator.isRunning()) {
                fadeOutAnimator.cancel();
            }

            // 사라지는 애니메이션 (투명도)
            fadeOutAnimator = ValueAnimator.ofInt(255, 0);
            fadeOutAnimator.setDuration((long) (duration * 1000 * 0.5)); // 움직임이 끝날 때쯤부터 사라지기 시작
            fadeOutAnimator.setStartDelay((long) (duration * 1000 * 0.5)); // 전체 지속시간의 50% 이후부터 시작
            fadeOutAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            fadeOutAnimator.addUpdateListener(animation -> {
                int alphaValue = (int) animation.getAnimatedValue();
                damageTextPaint.setAlpha(alphaValue);
            });

            fadeOutAnimator.start();
        }

        @Override
        public void update() {
            super.update();
            transform.move(0, -Metrics.height/20 * GameView.frameTime);
        }

        @Override
        public void draw(Canvas canvas) {
            if(!finished && damageTextPaint != null)
                canvas.drawText(String.valueOf(damage), transform.getPosition().x, transform.getPosition().y, damageTextPaint);
        }

        @Override
        public void onRecycle(){
            super.onRecycle();
            if (fadeOutAnimator != null) {
                fadeOutAnimator.removeAllUpdateListeners();
                fadeOutAnimator.cancel();
            }
        }
    }

    // 원 모양 이펙트 (공격 범위, 타격 효과 등에 사용)
    public static class CircleEffect extends Effect {
        private float finalRadius;
        private float radius;
        private int color;
        private final Paint paint;

        public CircleEffect(){
            super();
            color = Color.WHITE;
            radius = Metrics.GRID_UNIT;
            paint = new Paint();
        }

        public CircleEffect init(float x, float y, float radius, int color, float duration) {
            transform.set(x, y);
            super.duration = duration;
            super.elapsedTime = 0;
            this.finalRadius = radius;
            this.radius = 0;
            this.color = color;
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            return this;
        }

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void draw(Canvas canvas) {
            this.radius = finalRadius * elapsedTime / duration;
            paint.setAlpha(100 - (int) (100 * (elapsedTime / duration)));
            canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, radius, paint);
        }

        @Override
        public Layer getLayer() {
            return Layer.effect_back;
        }
    }

    public static class CoinEffect extends Effect {
        Coin coin;
        private ValueAnimator popAnimator; // 코인 튀어나오는 애니메이터
        private ValueAnimator fadeOutAnimator; // 코인 사라지는 애니메이터 (투명도 조절용)
        private float initialX, initialY; // 코인 초기 위치
        private float targetY; // 코인 최종 Y 위치 (살짝 아래로)
        private static final float JUMP_HEIGHT = Metrics.GRID_UNIT * 1.5f; // 점프 높이
        private static final float HORIZONTAL_SPREAD = Metrics.GRID_UNIT * 0.5f; // 좌우로 퍼지는 정도


        public CoinEffect() {
            super();
            coin = null;
        }

        public CoinEffect init(float x, float y){
            if(coin == null)
                coin = Scene.top().getRecyclable(Coin.class);
            finished = false;
            duration = 1.5f;
            elapsedTime = 0.0f;
            transform.set(x, y);
            coin.setPosition(x, y, Metrics.GRID_UNIT/3);
            this.remove = false;

            initialX = x;
            initialY = y;
            // 최종 Y 위치를 초기 Y에서 살짝 아래로 설정 (튀어나왔다가 떨어지는 느낌)
            targetY = initialY + Metrics.GRID_UNIT * 0.5f;

            // 코인 초기 위치 및 크기 설정
            coin.setPosition(initialX, initialY, Metrics.GRID_UNIT / 2.5f); // 크기 살짝 조절
            coin.setAlpha(255); // 초기 알파값 (불투명)

            // 기존 애니메이터가 있다면 취소
            if (popAnimator != null && popAnimator.isRunning()) {
                popAnimator.cancel();
            }
            if (fadeOutAnimator != null && fadeOutAnimator.isRunning()) {
                fadeOutAnimator.cancel();
            }

            // 뿅 튀어나오는 애니메이션 설정 (포물선 운동)
            // X 좌표는 초기 위치에서 랜덤하게 살짝 좌우로 퍼지도록 합니다.
            // Y 좌표는 위로 점프했다가 targetY로 떨어지도록 합니다.
            float randomSpread = ((float)Math.random() - 0.5f) * 2 * HORIZONTAL_SPREAD; // -HORIZONTAL_SPREAD ~ +HORIZONTAL_SPREAD
            final float finalX = initialX + randomSpread;

            popAnimator = ValueAnimator.ofFloat(0f, 1f);
            popAnimator.setDuration((long) (duration * 1000 * 0.7)); // 전체 지속시간의 70% 동안 움직임
            popAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // 부드러운 가감속

            popAnimator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();

                // X 좌표: 초기 위치에서 최종 위치로 직선 이동
                float currentX = initialX + (finalX - initialX) * progress;

                // Y 좌표: 포물선 운동 (위로 점프했다가 떨어짐)
                // progress가 0일 때 0, 0.5일 때 1, 1일 때 0이 되는 2차 함수: -4 * (x - 0.5)^2 + 1
                float verticalProgress = -4 * (progress - 0.5f) * (progress - 0.5f) + 1;
                float currentY = initialY - verticalProgress * JUMP_HEIGHT + (targetY - initialY) * progress;

                coin.setPosition(currentX, currentY);
            });

            // 사라지는 애니메이션 (투명도)
            fadeOutAnimator = ValueAnimator.ofInt(255, 0);
            fadeOutAnimator.setDuration((long) (duration * 1000 * 0.5)); // 움직임이 끝날 때쯤부터 사라지기 시작
            fadeOutAnimator.setStartDelay((long) (duration * 1000 * 0.5)); // 전체 지속시간의 50% 이후부터 시작
            fadeOutAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            fadeOutAnimator.addUpdateListener(animation -> {
                int alphaValue = (int) animation.getAnimatedValue();
                coin.setAlpha(alphaValue);
            });

            popAnimator.start();
            fadeOutAnimator.start();

            return this;
        }

        @Override
        public void update() {
            coin.update();
            super.update();
        }

        @Override
        public void draw(Canvas canvas) {
            coin.draw(canvas);
        }

        @Override
        public void onRecycle() {
            super.onRecycle();
            if (popAnimator != null) {
                popAnimator.removeAllUpdateListeners(); // 리스너 제거
                popAnimator.cancel(); // 혹시 실행 중이면 취소
            }
            if (fadeOutAnimator != null) {
                fadeOutAnimator.removeAllUpdateListeners();
                fadeOutAnimator.cancel();
            }
            Scene.top().remove(coin);
            coin = null;
        }

        @Override
        public Layer getLayer(){
            return Layer.effect_back;
        }
    }

    public static class HitEffect extends Effect {
        Paint paint; // 파티클을 그릴 때 사용할 Paint 객체
        ArrayList<Position> effectPoints; // 각 파티클의 현재 위치
        ArrayList<Position> effectVelocities; // 각 파티클의 현재 속도
        boolean areaAttack = false; // 광역 공격 여부 (현재 코드에서는 사용되지 않음)

        private Random random = new Random(); // 랜덤 값 생성을 위한 객체
        private static final float PARTICLE_RADIUS = 5f; // 파티클 반지름
        private static final float INITIAL_SPEED_MIN = 50f; // 파티클 초기 최소 속도 (픽셀/초)
        private static final float INITIAL_SPEED_MAX = 200f; // 파티클 초기 최대 속도 (픽셀/초)
        private static final float GRAVITY = 100f; // 중력 가속도 (픽셀/초^2)
        private float initialDuration; // 초기 설정된 duration 값 저장

        public HitEffect() {
            super(); // 부모 클래스 생성자 호출
            paint = new Paint();
            // Resources res = GameView.view.getResources(); // init에서 호출하도록 변경
            paint.setStyle(Paint.Style.FILL); // 파티클을 채워진 원으로 그림
            // paint.setStrokeWidth(8); // FILL 스타일에서는 strokeWidth가 크게 중요하지 않음

            effectPoints = new ArrayList<>();
            effectVelocities = new ArrayList<>();
        }

        /**
         * 피격 이펙트를 초기화합니다.
         * @param victim 피격당한 유닛
         * @param damage 데미지 양 (파티클 수에 영향)
         * @return 초기화된 HitEffect 객체
         */
        public HitEffect init(BattleUnit victim, int damage) {
            this.elapsedTime = 0f; // 경과 시간 초기화
            this.finished = false; // 활성화 상태로 설정
            this.remove = false;

            Resources res = GameView.view.getResources();
            // GameView.view가 null이거나 getResources()가 null을 반환하면 NullPointerException 발생 가능
            // 실제 앱에서는 GameView.view가 적절히 초기화되어야 합니다.
            if (res == null) {
                // 리소스가 없는 경우 기본 색상 사용 또는 에러 처리
                paint.setColor(Color.RED); // 예: 기본 빨간색
                System.err.println("Warning: Resources not available for HitEffect paint color.");
            } else {
                switch (victim.getColorType()) {
                    case RED:
                        paint.setColor(ResourcesCompat.getColor(res, R.color.PolymanColorRed, null));
                        break;
                    case BLUE:
                        paint.setColor(ResourcesCompat.getColor(res, R.color.PolymanColorBlue, null));
                        break;
                    case GREEN:
                        paint.setColor(ResourcesCompat.getColor(res, R.color.PolymanColorGreen, null));
                        break;
                    case BLACK:
                        paint.setColor(ResourcesCompat.getColor(res, R.color.PolymanColorBlack, null));
                        break;
                    default:
                        paint.setColor(Color.MAGENTA); // 정의되지 않은 타입의 경우
                }
            }
            // paint.setAlpha(255); // 초기 알파는 불투명하게 시작해서 update에서 조절

            effectPoints.clear();
            effectVelocities.clear();

            // 데미지 수치에 비례하여 파티클 수를 결정 (최소 5개, 최대 50개 등으로 제한하는 것이 좋음)
            int particleCount = Math.max(5, Math.min(damage, 50)); // 예: 최소 5개, 최대 50개

            for (int i = 0; i < particleCount; ++i) {
                // 파티클 생성 위치는 victim의 중앙
                effectPoints.add(new Position(victim.getTransform().getPosition().x, victim.getTransform().getPosition().y));

                // 파티클 초기 속도를 랜덤하게 설정 (사방으로 튀도록)
                float angle = random.nextFloat() * 2 * (float) Math.PI; // 0 ~ 2PI 라디안 (360도)
                float speed = INITIAL_SPEED_MIN + random.nextFloat() * (INITIAL_SPEED_MAX - INITIAL_SPEED_MIN);
                float velX = (float) Math.cos(angle) * speed;
                float velY = (float) Math.sin(angle) * speed;
                effectVelocities.add(new Position(velX, velY));
            }
            this.duration = 1.0f; // 이펙트 지속 시간 (예: 1초)
            this.initialDuration = this.duration; // 초기 duration 저장

            return this;
        }

        /**
         * 이펙트의 상태를 업데이트합니다. (매 프레임 호출)
         */
        @Override
        public void update() {
            super.update(); // 부모 클래스의 update 호출 (elapsedTime, isActive 업데이트)

            if (finished || remove) {
                return;
            }

            // 파티클 위치 업데이트
            for (int i = 0; i < effectPoints.size(); ++i) {
                Position point = effectPoints.get(i);
                Position velocity = effectVelocities.get(i);

                // 속도에 따른 위치 변경
                point.x += velocity.x * GameView.frameTime;
                point.y += velocity.y * GameView.frameTime;
            }

            // 시간이 지남에 따라 파티클이 투명해지도록 알파값 조절
            float lifeRatio = elapsedTime / initialDuration; // 0.0 (시작) ~ 1.0 (끝)
            int currentAlpha = (int) (255 * (1 - lifeRatio)); // 점점 투명하게
            currentAlpha = Math.max(0, Math.min(255, currentAlpha)); // 0~255 범위 유지
            paint.setAlpha(currentAlpha);
        }

        /**
         * 이펙트를 화면에 그립니다.
         * @param canvas 그림을 그릴 Canvas 객체
         */
        @Override
        public void draw(Canvas canvas) {
            if (finished || canvas == null) { // 비활성화 상태이거나 canvas가 null이면 그리지 않음
                return;
            }

            // 모든 파티클을 그림
            for (Position point : effectPoints) {
                canvas.drawCircle(point.x, point.y, PARTICLE_RADIUS, paint);
            }
        }
    }
}