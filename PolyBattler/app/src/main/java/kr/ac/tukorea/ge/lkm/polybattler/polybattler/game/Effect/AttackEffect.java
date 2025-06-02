package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Layer;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;

public class AttackEffect extends EffectManager.Effect {
    BattleUnit attacker;
    BattleUnit target;
    Paint paint;
    static ArrayList<DashPathEffect> dashPathEffects;
    int currentDashEffect = 0;
    Path path;
    boolean areaAttack = false;
    boolean init = false;

    static {
        // 점선 패턴 설정: { 간격 }, 초기 그려질 길이
        dashPathEffects = new ArrayList<>();
        dashPathEffects.add(new DashPathEffect(new float[]{25f, 50f}, 0f));
        dashPathEffects.add(new DashPathEffect(new float[]{25f, 50f}, 12.5f));
        dashPathEffects.add(new DashPathEffect(new float[]{25f, 50f}, 25f));
    }

    public AttackEffect(){
        super();
        path = new Path();
        paint = new Paint();
        Resources res = GameView.view.getResources();
        paint.setColor(ResourcesCompat.getColor(res, R.color.TargetEffect, null));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
    }

    //
    public AttackEffect initAnime(BattleUnit attacker, BattleUnit target, int type){
        this.attacker = attacker;
        this.target = target;

        if(init) { // 이미 그려지고 있었던 건지 확인
            // 이미 그려지고 있었다면?
        }else{
            // 안 그려지던 중이니 새로 scene에 등록
            Scene.top().add(this);
            init = true;
        }

        // 효과 초기 정렬
        paint.setPathEffect(dashPathEffects.get(0));

        // 직선 위치 조정
        path.reset();
        Position pos1 = attacker.getTransform().getPosition();
        Position pos2 = target.getTransform().getPosition();
        path.moveTo(pos1.x, pos1.y);
        path.lineTo(pos2.x, pos2.y);

        // 범위 공격 여부 확인
        areaAttack = attacker.getShapeType() == Polyman.ShapeType.CIRCLE;

        // 이펙트 관련 변수 초기화
        elapsedTime = 0;
        elapsedTimeNext = elapsedTime + 0.25f;
        finished = false;
        remove = false;

        return this;
    }

    @Override
    public void update() {
        elapsedTime += GameView.frameTime;
        if(GameManager.getInstance(Scene.top()).getGameState() != GameState.BATTLE){
            // 혹시 전투 단계가 아니라면 삭제하기
            if(remove)
                Log.d("Attack Effect", "비전투 상황인데도 attack effect가 실행 중임");
            attacker.stopAttackEffect(); // 소유주에게 자신을 삭제하라고 전달
        }
        if(remove){ // 삭제 표기가 있다면 자신을 삭제
            Scene.top().remove(this);
            init = false;
        }
    }

    float elapsedTimeNext = 0;
    @Override
    public void draw(Canvas canvas){
        if(finished)
            return;
        if(elapsedTime > elapsedTimeNext) {
            // 선 위치를 실시간 조정 => 너무 자주 하기보단 일정 시간마다 하자 
            path.reset();
            Position pos1 = attacker.getTransform().getPosition();
            Position pos2 = target.getTransform().getPosition();
            path.moveTo(pos1.x, pos1.y);
            path.lineTo(pos2.x, pos2.y);

            // 점선 패턴 이동
            currentDashEffect = (currentDashEffect + 1) % dashPathEffects.size();
            paint.setPathEffect(dashPathEffects.get(currentDashEffect));
            elapsedTimeNext = elapsedTime + 0.2f;
        }
        if(areaAttack){
            // 범위 공격이면 공격 지점에 공격 범위만큼 표시
            canvas.drawCircle(target.getTransform().getPosition().x, target.getTransform().getPosition().y, attacker.getAreaRange(), paint);
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public void remove() {
        finished = true;
        super.remove();
    }

    @Override
    public Layer getLayer(){
        return Layer.effect_back;
    }
}
