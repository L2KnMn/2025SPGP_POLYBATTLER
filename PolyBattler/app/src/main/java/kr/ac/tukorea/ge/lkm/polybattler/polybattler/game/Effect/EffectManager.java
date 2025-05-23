package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
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
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
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
    public void createDamageTextEffect(float x, float y, int damage) {
        DamageTextEffect damageText = master.getRecyclable(DamageTextEffect.class);
        if (damageText == null) {
            damageText = new DamageTextEffect(x, y, damage, damageTextPaint);
        }else{
            damageText.init(x, y, damage, damageTextPaint);
        }
        addEffect(damageText);
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
        BattleUnit victim;
        Paint paint;
        ArrayList<Position> effectPoints;
        boolean areaAttack = false;
        public HitEffect(){
            super();
            paint = new Paint();
            Resources res = GameView.view.getResources();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);



        }

        public HitEffect init(BattleUnit victim, int damage){
            Resources res = GameView.view.getResources();
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
            }
            paint.setAlpha(128);

            duration = 3.0f;

            return this;
        }

        @Override
        public void update() {
            super.update();
        }

        public void draw(Canvas canvas){
        }
    }
}