package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

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

    static {
        // 점선 패턴 설정: [그려질 길이, 간격, 그려질 길이, 간격, ...]
        // 예: 10px 그리고, 20px 건너뛰고, 10px 그리고, 20px 건너뛰는 패턴
        dashPathEffects = new ArrayList<>();
        dashPathEffects.add(new DashPathEffect(new float[]{25f, 25f}, 0f));
        dashPathEffects.add(new DashPathEffect(new float[]{25f, 25f}, 12.5f));
        dashPathEffects.add(new DashPathEffect(new float[]{25f, 25f}, 25f));
    }

    public AttackEffect(){
        super();
        path = new Path();
    }

    public AttackEffect init(BattleUnit attacker, BattleUnit target){
        this.attacker = attacker;
        this.target = target;

        paint = new Paint();
        Resources res = GameView.view.getResources();
        paint.setColor(ResourcesCompat.getColor(res, R.color.TargetEffect, null));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);

        paint.setPathEffect(dashPathEffects.get(0));

        path.reset();
        Position pos1 = attacker.getTransform().getPosition();
        Position pos2 = target.getTransform().getPosition();
        path.moveTo(pos1.x, pos1.y);
        path.lineTo(pos2.x, pos2.y);

        EffectManager.getInstance(Scene.top()).addEffect(this);

        areaAttack = attacker.getShapeType() == Polyman.ShapeType.CIRCLE;

        elapsedTime = 0;
        elapsedTimeNext = elapsedTime + 0.25f;

        return this;
    }

    @Override
    public void update() {
        elapsedTime += GameView.frameTime;
        if(GameManager.getInstance(Scene.top()).getGameState() != GameState.BATTLE){
            remove();
            return;
        }
        if(remove){
            Scene.top().remove(this);
        }
    }

    float elapsedTimeNext = 0;
    public void draw(Canvas canvas){
        if(finished)
            return;
        if(elapsedTime > elapsedTimeNext) {
            path.reset();
            if(!attacker.isDead() && !target.isDead()) {
                Position pos1 = attacker.getTransform().getPosition();
                Position pos2 = target.getTransform().getPosition();
                path.moveTo(pos1.x, pos1.y);
                path.lineTo(pos2.x, pos2.y);

                currentDashEffect = (currentDashEffect + 1) % dashPathEffects.size();
                paint.setPathEffect(dashPathEffects.get(currentDashEffect));
                elapsedTimeNext = elapsedTime + 0.2f;
            }
        }
        if(areaAttack){
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
