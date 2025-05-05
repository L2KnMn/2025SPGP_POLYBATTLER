package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.util.Log;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MasterManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BehaviorTreeFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class BattleManager implements IGameManager {
    private final Scene master;
    GameState currentState;
    public enum Team {
        PLAYER,
        ENEMY
    }
    private final Map<Team, ArrayList<BattleUnit>> battlers;
    private final Map<Team, Integer> counts;
    private final String winMassage;
    private final String loseMassage;
    UiManager.Signage signage;
    private Team winner;

    public BattleManager(Scene master) {
        this.master = master;
        battlers = new HashMap<>();
        counts = new HashMap<>();
        currentState = GameState.PREPARE;
        winner = Team.PLAYER; // Player가 기본이고, 만약 enemy일 경우 패배한 것으로 보고 게임을 초기화

        winMassage = "You Win";
        loseMassage = "You Loose";

        signage = UiManager.getInstance(master).addSignage("", Metrics.width/2, Metrics.height/2 - Metrics.GRID_UNIT, Metrics.width, Metrics.height/2);
        signage.setVisibility(GameState.RESULT, true);
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

    @Override
    public boolean onTouch(MotionEvent event) {
        return false;
    }

    @Override
    public IGameManager setGameState(GameState state) {
        if (currentState != GameState.BATTLE && state == GameState.BATTLE) {
            // 전투 상태로 만들고, 행동 트리 삽입
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
        }else if(currentState == GameState.BATTLE) {
            // 중지 시키고 원복
            for(Team team : Team.values()) {
                ArrayList<BattleUnit> units = battlers.get(team);
                if(units == null)
                    continue;
                for(BattleUnit unit : units){
                    BehaviorTreeFactory.releaseTree(unit.getShapeType(), unit.getBehaviorTree());
                    unit.setBehaviorTree(null, null);
                    UiManager.getInstance(master).removeHpBar(unit);
                }
                units.clear();
            }
        }
        currentState = state;
        return this;
    }

    @Override
    public GameState getGameState() {
        return currentState;
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
            Log.d("BattleManager", unit.getTeam() + " killed " + target.getTeam() + " be left " + t);
            if (t <= 0) {
                MasterManager.getInstance(master).setGameState(GameState.RESULT);
                if(unit.getTeam() == Team.PLAYER){
                    signage.setText(winMassage);
                }else {
                    signage.setText(loseMassage);
                    winner = Team.ENEMY;
                }
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

    public Team getWinner() {
        return winner;
    }
}
