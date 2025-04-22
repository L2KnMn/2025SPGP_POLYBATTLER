package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.util.Log;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.IGameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BattleUnit;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree.BehaviorTreeFactory;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
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
    private final ArrayList<IGameObject> enemies;

    public BattleManager(Scene master) {
        this.master = master;
        battlers = new HashMap<>();
        enemies = new ArrayList<>();
        currentState = GameState.PREPARE;
    }

    public void addBattler(Polyman polyman) {
        ArrayList<BattleUnit> units = battlers.computeIfAbsent(Team.PLAYER, k -> new ArrayList<>());
        units.add(polyman.getBattleUnit());
        polyman.getBattleUnit().setTeam(Team.PLAYER);
    }

    public void addEnemy() {
        ArrayList<BattleUnit> units = battlers.computeIfAbsent(Team.ENEMY, k -> new ArrayList<>());
        Polyman enemy = (Polyman) master.getRecyclable(Polyman.class);
        if (enemy == null){
            enemy = new Polyman(Polyman.ShapeType.CIRCLE, Polyman.ColorType.BLACK);
        }
        enemy.init(Polyman.ShapeType.CIRCLE, Polyman.ColorType.BLACK);
        enemy.transform.set(Metrics.width/2, 400);
        units.add(enemy.getBattleUnit());
        Log.d("BattleManager", "addEnemy() called");
        enemies.add(enemy);
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        return false;
    }

    @Override
    public void setGameState(GameState state) {
        if (state == GameState.BATTLE) {
            // 전투 상태로 만들고, 행동 트리 삽입
            // Log.d("BattleManager", "setGameState() called" + state.name());
            for(Team team : Team.values()) {
                ArrayList<BattleUnit> units = battlers.get(team);
                if(units == null)
                    continue;
                for(BattleUnit unit : units){
                   unit.setBehaviorTree(BehaviorTreeFactory.getTreeForShape(unit.getShapeType()), this);
                }
            }
            // 에너미 master에 포함시키기
            for (IGameObject enemyIGameObject : enemies) {
                Polyman enemy = (Polyman)enemyIGameObject;
                enemy.startBattle();
                master.add(enemy);
            }
        }else {
            // 중지 시키고 원복
            for(Team team : Team.values()) {
                ArrayList<BattleUnit> units = battlers.get(team);
                if(units == null)
                    continue;
                for(BattleUnit unit : units){
                    BehaviorTreeFactory.releaseTree(unit.getShapeType(), unit.getBehaviorTree());
                    unit.setBehaviorTree(null, null);
                }
            }
            for (IGameObject enemy : enemies) {
                master.remove(enemy);
            }
        }
        currentState = state;
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
}
