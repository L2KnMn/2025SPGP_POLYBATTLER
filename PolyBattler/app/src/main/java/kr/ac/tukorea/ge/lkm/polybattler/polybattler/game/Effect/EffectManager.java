package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MainScene;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.ILayerProvider;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IRecyclable;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class EffectManager implements IGameManager {
    private static final String TAG = "EffectManager";
    private static final Map<Scene, EffectManager> instances = new HashMap<>();
    private final Scene master;
    private final List<Effect> effects;
    private final Paint textPaint;
    private final Random random = new Random();
    private GameState currentState = GameState.PREPARE;

    private EffectManager(Scene master) {
        this.master = master;
        this.effects = new ArrayList<>();
        this.textPaint = new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
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
            damageText = new DamageTextEffect(x, y, damage);
        }else{
            damageText.set(x, y, damage);
        }
        addEffect(damageText);
    }

    // 공격 범위 표시 이펙트 (예시)
    public void createAttackRangeEffect(float x, float y, float range, float duration) {
        CircleEffect attackRange = new CircleEffect(x, y, range, Color.YELLOW, duration);
        addEffect(attackRange);
    }

    // 타격 효과 이펙트 (예시)
    public void createHitEffect(float x, float y, float size, float duration) {
        CircleEffect hitEffect = new CircleEffect(x, y, size, Color.RED, duration);
        addEffect(hitEffect);
    }

    // 터치 효과 이펙트 (예시)
    public void createTouchEffect(float x, float y) {
        CircleEffect touchEffect = new CircleEffect(x, y, 20, Color.BLUE, 0.3f); // 짧은 지속 시간
        addEffect(touchEffect);
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (currentState == GameState.BATTLE) {
            createTouchEffect(event.getX(), event.getY()); // 터치 이펙트 생성
            return true; // 이벤트 소비
        }
        return false;
    }

    @Override
    public void setGameState(GameState state) {
        clearAllEffects();
        this.currentState = state;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }

    public void clearAllEffects() {
        for (Effect effect : effects) {
            master.remove(effect);
        }
        effects.clear();
    }


    // 이펙트 추상 클래스
    public abstract class Effect implements IGameObject, IRecyclable, ILayerProvider {
        protected float duration;
        protected float elapsedTime;
        protected boolean finished;
        protected Transform transform;

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
                master.remove(this);
            }
        }

        @Override
        public void draw(Canvas canvas){}

        public Transform getTransform() {
            return transform;
        }

        @Override
        public void onRecycle(){
            Log.d("Effect", "onRecycle() called");
            elapsedTime = 0;
            finished = false;
        }

        @Override
        public MainScene.Layer getLayer() {
            return MainScene.Layer.effect_front;
        }
    }

    // 데미지 텍스트 이펙트 (이너 클래스)
    public class DamageTextEffect extends Effect {
        private int damage;

        public DamageTextEffect(float x, float y, int damage) {
            super(x, y, 1.0f); // 지속 시간 1초
            this.damage = damage;
        }

        public void set(float x, float y, int damage){
            transform.set(x, y);
            this.damage = damage;
        }

        @Override
        public void update() {
            super.update();
            transform.move(0, -Metrics.height/20 * GameView.frameTime);
        }

        @Override
        public void draw(Canvas canvas) {
            textPaint.setColor(Color.RED);
            canvas.drawText(String.valueOf(damage), transform.getPosition().x, transform.getPosition().y, textPaint);
        }
    }

    // 원 모양 이펙트 (공격 범위, 타격 효과 등에 사용)
    public class CircleEffect extends Effect {
        private float radius;
        private final int color;

        public CircleEffect(float x, float y, float radius, int color, float duration) {
            super(x, y, duration);
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void update() {
            super.update();
            this.radius += GameView.frameTime * 20;
        }

        @Override
        public void draw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(255 - (int) (255 * (elapsedTime / duration))); // 투명도 조절
            canvas.drawCircle(transform.getPosition().x, transform.getPosition().y, radius, paint);
        }
    }
    // 추가적인 이펙트 클래스들 (필요에 따라 구현)
}