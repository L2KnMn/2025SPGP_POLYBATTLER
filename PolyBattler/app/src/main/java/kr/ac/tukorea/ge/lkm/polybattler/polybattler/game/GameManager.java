package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ColorType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ShapeType;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class GameManager implements IGameManager {
    private static final Map<Scene, GameManager> instances = new HashMap<>();
    private final Scene master;
    private GameState currentState;
    private int round;
    private int gold;
    private final GameMap gameMap;
    private GameManager(Scene master) {
        currentState = GameState.PREPARE;
        round = 1;
        gold = 10; // 초기 골드
        gameMap = new GameMap(4, 7, 5);
        this.master = master;
        master.add(gameMap);

        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
        if(gameMap.setObjectOnTile(polyman.transform, 1, 6))
            master.add(polyman);
        else
            Log.d("GameManager", "Failed to set object on tile");

        polyman = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
        if(gameMap.setObjectOnTile(polyman.transform, 2, 5))
            master.add(polyman);
        else
            Log.d("GameManager", "Failed to set object on tile");

        polyman = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
        if(gameMap.setObjectOnTile(polyman.transform, 3, 5)){
            master.add(polyman);
        }else
            Log.d("GameManager", "Failed to set object on tile");
    }

    public static GameManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, GameManager::new);
    }

    public GameMap getMap() {
        return gameMap;
    }
    public GameState getCurrentState() {
        return currentState;
    }

    public void setGameState(GameState newState) {
        this.currentState = newState;
    }

    public int getRound() {
        return round;
    }

    public void nextRound() {
        round++;
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public boolean spendGold(int amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    public boolean purchaseCharactor(int price, ShapeType shape, ColorType color) {
        if (spendGold(price)) {
            generateCharacterBench(shape, color);
            return true;
        }
        return false;
    }

    public void generateCharacterBench(ShapeType shape, ColorType color){
        int index = gameMap.getEmptyBenchIndex();
        if(index >= 0) {
            Polyman polyman = new Polyman(shape, color);
            gameMap.setObjectOnBench(polyman.transform, index);
            master.add(polyman);
        }
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        return true;
    }
    @Override
    public GameState getGameState(){
        return currentState;
    }
}