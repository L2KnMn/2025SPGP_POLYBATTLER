package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ColorType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Map;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.ShapeType;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Shop;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.ShopManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class GameManager implements IGameManager {
    private static GameManager instance;
    private Scene master;
    private GameState currentState;
    private int round;
    private int gold;
    private final Map map;
    private GameManager(Scene master) {
        currentState = GameState.PREPARE;
        round = 1;
        gold = 10; // 초기 골드
        map = new Map(4, 7, 5);
        this.master = master;
        master.add(map);

        Polyman polyman = new Polyman(ShapeType.CIRCLE, ColorType.RED);
        map.setObjectOnTile(polyman.transform, 1, 6);
        master.add(polyman);

        Polyman polyman2 = new Polyman(ShapeType.RECTANGLE, ColorType.BLUE);
        map.setObjectOnTile(polyman2.transform, 2, 5);
        master.add(polyman2);

        Polyman polyman3 = new Polyman(ShapeType.TRIANGLE, ColorType.GREEN);
        map.setObjectOnTile(polyman3.transform, 3, 5);
        master.add(polyman3);
    }

    public static GameManager getInstance(Scene master) {
        if (instance == null) {
            instance = new GameManager(master);
        }
        return instance;
    }

    public Map getMap() {
        return map;
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
        int index = map.getEmptyBenchIndex();
        if(index >= 0) {
            Polyman polyman = new Polyman(shape, color);
            map.setObjectOnBench(polyman.transform, index);
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