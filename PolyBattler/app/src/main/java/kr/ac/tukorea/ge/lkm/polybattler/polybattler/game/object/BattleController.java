package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.content.res.Resources;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.Effect.EffectManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MasterManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BehaviorTreeFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.SynergyFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class BattleController {
    private final Scene master;
    private Result result;

    public enum Team {
        PLAYER,
        ENEMY
    }
    public enum Result {
        WIN,
        LOSE,
        CONTINUE,
        NONE,
    }
    SynergyFactory synergyFactory;
    private final Map<Team, ArrayList<BattleUnit>> battlers;
    private final Map<Team, Integer> counts;
    private final String winMassage;
    private final String loseMassage;
    UiManager.Signage signage;


    public BattleController(Scene master) {
        this.master = master;
        battlers = new HashMap<>();
        counts = new HashMap<>();
        result = Result.NONE;

        winMassage = "You Win";
        loseMassage = "You Loose";

        signage = UiManager.getInstance(master).addSignage("", Metrics.width/2, Metrics.height/2 - Metrics.GRID_UNIT, Metrics.width, Metrics.height/2);
        Resources res = GameView.view.getResources();
        signage.setColors(ResourcesCompat.getColor(res, R.color.resultBoardbg, null), ResourcesCompat.getColor(res, R.color.resultBoardText, null));
        signage.setVisibility(GameState.RESULT, true);
        synergyFactory = new SynergyFactory(master);
    }

    public void resignPlayer() {
        result = Result.LOSE;
        signage.setText(loseMassage);
    }

    public void addBattler(Polyman polyman) {
        ArrayList<BattleUnit> units = battlers.computeIfAbsent(Team.PLAYER, k -> new ArrayList<>());
        units.add(polyman.getBattleUnit());
        polyman.getBattleUnit().setTeam(Team.PLAYER);
    }

    public void addEnemy(Polyman polyman) {
        ArrayList<BattleUnit> units = battlers.computeIfAbsent(Team.ENEMY, k -> new ArrayList<>());
        units.add(polyman.getBattleUnit());
        polyman.getBattleUnit().setTeam(Team.ENEMY);
    }

    public void updateSynergy() {
        // 전투 시작 전 시너지 효과 계산 및 적용
        ArrayList<BattleUnit> enemyunits = battlers.get(Team.ENEMY);
        ArrayList<BattleUnit> playerUnits = battlers.get(Team.PLAYER);
        if (playerUnits != null) {
            synergyFactory.calculateAndApplySynergies(playerUnits, enemyunits);
        }
    }

    public void startBattle(){
        // 전투 상태로 만들고, 행동 트리 삽입
        result = Result.CONTINUE;

        updateSynergy();

        // Log.d("BattleManager", "setGameState() called" + state.name());
        for(Team team : Team.values()) {
            ArrayList<BattleUnit> units = battlers.get(team);
            if(units == null)
                continue;
            for(BattleUnit unit : units){
                unit.setBehaviorTree(BehaviorTreeFactory.getTreeForShape(unit.getShapeType()), this);
                UiManager.getInstance(master).addHpBar(unit);
            }
            counts.put(team, units.size());
        }
    }

    public void endBattle(){
        // 중지 시키고 원복
        for(Team team : Team.values()) {
            ArrayList<BattleUnit> units = battlers.get(team);
            if(units == null)
                continue;
            for(BattleUnit unit : units){
                BehaviorTreeFactory.releaseTree(unit);
                unit.stopAttackEffect();
                UiManager.getInstance(master).removeHpBar(unit);
            }
            units.clear();
        }
    }

    public BattleUnit findClosestEnemy(BattleUnit unit) {
        //Log.d("BattleManager", "findClosestEnemy() called");
        ArrayList<BattleUnit> enemies = battlers.get(unit.getTeam() == Team.PLAYER ? Team.ENEMY : Team.PLAYER);

        if (enemies == null)
            return null;
        double minDistance = Double.MAX_VALUE;
        BattleUnit closestEnemy = null;

        for(int i =0; i< enemies.size(); ++i) {
            if(!enemies.get(i).isDead()) {
                double distance = unit.getTransform().distance(enemies.get(i).getTransform());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestEnemy = enemies.get(i);
                }
            }
        }
        //Log.d("BattleManager", "return target (" + closestEnemy.getTransform().getPosition().x + ", " + closestEnemy.getTransform().getPosition().y + ")");
        return closestEnemy;
    }

    public void killSign(BattleUnit unit, BattleUnit target){
        Integer t = counts.get(target.getTeam());
        if(t != null) {
            t = t - 1;
            counts.put(target.getTeam(), t);
            if(target.getTeam() == Team.ENEMY) {
                EffectManager.CoinEffect coinEffect = new EffectManager.CoinEffect()
                        .init(target.getTransform().getPosition().x, target.getTransform().getPosition().y);
                EffectManager.getInstance(master).addEffect(coinEffect);
                GameManager.getInstance(master).addGold(1);
            }
            Log.d("BattleManager", unit.getTeam() + " killed " + target.getTeam() + " be left " + t);
            if (t <= 0) {
                if(unit.getTeam() == Team.PLAYER){
                    signage.setText(winMassage);
                    result = Result.WIN;
                }else {
                    signage.setText(loseMassage);
                    result = Result.LOSE;
                }
                MasterManager.getInstance(master).setGameState(GameState.RESULT);
            }
        }
    }
    public void findEnemiesInArea(ArrayList<BattleUnit> result, Position position, Team attacker, float v) {
        ArrayList<BattleUnit> enemies = battlers.get(attacker == Team.ENEMY ? Team.PLAYER : Team.ENEMY);
        if (enemies != null)
            for (BattleUnit enemy : enemies){
                if(!enemy.isDead() && (enemy.getTransform().distanceSq(position) < v*v))
                    result.add(enemy);
            }
    }

    public Result getResult() {
        return result;
    }
}
