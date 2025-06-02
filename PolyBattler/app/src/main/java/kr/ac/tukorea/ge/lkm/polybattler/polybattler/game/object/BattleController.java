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
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.Gravity;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class BattleController {
    private final Scene master;
    private Result result;
    private static String TAG = "BattleController";

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
    BattleUnit[][] unitsMap;

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
        unitsMap = null;
    }

    public void resignPlayer() {
        result = Result.LOSE;
        signage.setText(loseMassage);
   }

    public void addFriendlyBattler(Polyman polyman) {
        ArrayList<BattleUnit> units = battlers.computeIfAbsent(Team.PLAYER, k -> new ArrayList<>());
        units.add(polyman.getBattleUnit());
        polyman.getBattleUnit().setTeam(Team.PLAYER);
    }

    public void emtpyBattler(Team team){
        ArrayList<BattleUnit> units = battlers.get(team);
        if(units != null)
            units.clear();
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

        unitsMap = new BattleUnit[GameManager.getInstance(master).height][GameManager.getInstance(master).width];

        for(Team team : Team.values()) {
            ArrayList<BattleUnit> units = battlers.get(team);
            if(units == null)
                continue;
            for(BattleUnit unit : units){
                unit.setBehaviorTree(BehaviorTreeFactory.getTreeForShape(unit.getShapeType()), this);
                UiManager.getInstance(master).addHpBar(unit);
                // unit map에 기록하고 움직일 때 이걸 참조
                int width = GameManager.getInstance(master).getGameMap().getWidth(unit.getTransform().getPosition().x);
                int height = GameManager.getInstance(master).getGameMap().getHeight(unit.getTransform().getPosition().y);
                unitsMap[height][width] = unit;
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
                BattleUnit enemy = enemies.get(i);
                double distance = Math.abs(enemy.getTransform().position.x - unit.getTransform().position.x)
                        + Math.abs(enemy.getTransform().position.y - unit.getTransform().position.y);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestEnemy = enemies.get(i);
                }
            }
        }
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

    public Position getCloseTileToTarget(BattleUnit unit, BattleUnit target) {
        if(unit == null){
            Log.d(TAG, "get close tile to target : unit is null");
            return null;
        }
        if(target == null){
            Log.d(TAG, "get close tile to target : target is null");
            return null;
        }

        GameMap gameMap = GameManager.getInstance(master).getGameMap();

        if(gameMap == null){
            Log.d(TAG, "get close tile to target : gameMap is null");
            return null;
        }

        int unitGridX = gameMap.getWidth(unit.getTransform().getPosition().x);
        int unitGridY = gameMap.getHeight(unit.getTransform().getPosition().y);
        int targetGridX = gameMap.getWidth(target.getTransform().getPosition().x);
        int targetGridY = gameMap.getHeight(target.getTransform().getPosition().y);

        int attackRange = (int)unit.getAttackRange(); // 사거리 가져오기 status는 모두 float이므로 int로 임의 변환

        Position closestAvailableTileTransform = null;
        double minDistance = Double.MAX_VALUE;

        // 타겟 주변의 모든 그리드 셀을 순회하며 유닛의 사거리 내에 있는지,
        // 그리고 비어있는지 (null) 확인합니다.
        // 사거리 내 타일 범위를 설정 (타겟 중심으로 attackRange만큼 확장)
        // 타겟으로 벗어난 거리 x칸과 y칸의 합은 사거리와 같아야 한다
        // abs(i) + abs(j) <= r
        // abs(j) <= r - abs(i)
        // abs(i) - r <= j <= r - abs(i)
        for (int i = -attackRange; i <= attackRange; i++) {
            // j의 범위는 abs(i) + abs(j) <= R 에서 유도: abs(j) <= R - abs(i)
            // 따라서 j는 -(R - abs(i)) 부터 (R - abs(i)) 까지
            int jRange = attackRange - Math.abs(i);
            for (int j = -jRange; j <= jRange; j++) {
                int x = targetGridX + i;
                int y = targetGridY + j;
                // 맵 범위를 벗어나는지 확인
                if (x < 0 || x >= unitsMap[0].length ||
                    y < 0 || y >= unitsMap.length) {
                    continue;
                }
                // 타겟의 현재 위치는 공격 가능 사거리여도 다른 유닛이 점유하는 것을 막기 위해 제외
                if (x == targetGridX && y == targetGridY) {
                    continue;
                }

                // 해당 타일이 비어있는지 확인 (null은 비어있음을 의미)
                if (unitsMap[y][x] == null) {
                    // 현재 유닛 위치로부터 이 잠재적 목표 타일까지의 거리 계산
                    int distanceToUnit = Math.abs(x - unitGridX) + Math.abs(y - unitGridY);
                    if (distanceToUnit < minDistance) {
                        minDistance = distanceToUnit;
                        if(closestAvailableTileTransform == null)
                            closestAvailableTileTransform = new Position();
                        closestAvailableTileTransform.x = gameMap.getTileX(x, Gravity.CENTER);
                        closestAvailableTileTransform.y = gameMap.getTileY(y, Gravity.CENTER);;
                    }
                }
            }
        }
        return closestAvailableTileTransform;
    }

    public boolean requestTileOccupation(BattleUnit unit, float currentWorldX, float currentWorldY,
                                         float targetWorldX, float targetWorldY) {
        GameMap gameMap = GameManager.getInstance(master).getGameMap();
        if (unit == null || gameMap == null || unitsMap == null) {
            Log.e(TAG, "requestTileOccupation: unit, gameMap, or unitsMap is null.");
            return false;
        }

        int currentGridX = gameMap.getWidth(currentWorldX);
        int currentGridY = gameMap.getHeight(currentWorldY);
        int targetGridX = gameMap.getWidth(targetWorldX);
        int targetGridY = gameMap.getHeight(targetWorldY);

        // 맵 범위 유효성 검사
        if (targetGridX < 0 || targetGridX >= unitsMap[0].length ||
                targetGridY < 0 || targetGridY >= unitsMap.length) {
            Log.e(TAG, "requestTileOccupation: Target grid coordinates are out of bounds.");
            return false;
        }

        // 1. 목표 타일이 현재 비어있는지, 혹은 현재 유닛이 이미 점유하고 있는지 확인
        // (다른 유닛이 점유했거나 예약했으면 실패)
        if (unitsMap[targetGridY][targetGridX] != null && unitsMap[targetGridY][targetGridX] != unit) { //
            Log.w(TAG, "requestTileOccupation: Target tile (" + targetGridX + "," + targetGridY + ") is already occupied by another unit."); //
            return false; //
        }
        // 2. 현재 유닛의 기존 위치 점유 해제
        // 유닛이 현재 그리드 맵 상에 위치하고 있고, 그 위치가 올바른지 확인 후 해제
        if (currentGridX >= 0 && currentGridX < unitsMap[0].length && //
                currentGridY >= 0 && currentGridY < unitsMap.length) { //
            // 현재 위치에 이 유닛이 점유하고 있었다면 해제
            if (unitsMap[currentGridY][currentGridX] == unit) { //
                unitsMap[currentGridY][currentGridX] = null; //
            } else if (unitsMap[currentGridY][currentGridX] != null) {
                // 다른 유닛이 현재 위치를 점유하고 있는 경우 (경고)
                Log.w(TAG, "requestTileOccupation: Unit " + unit + " expected at (" + currentGridX + "," + currentGridY + ") but found " + unitsMap[currentGridY][currentGridX]);
            }
        } else {
            // 현재 그리드 좌표가 맵 범위를 벗어나는 경우 (초기 배치 등)
            Log.d(TAG, "requestTileOccupation: Current unit position (" + currentGridX + "," + currentGridY + ") is out of bounds or not yet on map.");
        }

        // 3. 목표 타일 점유 (또는 예약)
        // 목표 타일을 해당 유닛으로 점유합니다.
        unitsMap[targetGridY][targetGridX] = unit; //
        Log.d(TAG, "requestTileOccupation: Unit " + unit + " occupied target tile (" + targetGridX + "," + targetGridY + ")."); //
        return true; //
    }
}

