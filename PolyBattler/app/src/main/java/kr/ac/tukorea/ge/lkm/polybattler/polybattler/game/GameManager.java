package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import android.view.MotionEvent;

public class GameManager implements IGameManager {
    private static GameManager instance;
    private GameState currentState;
    private int round;
    private int gold;

    private GameManager() {
        currentState = GameState.PREPARE;
        round = 1;
        gold = 10; // 초기 골드
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
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

    @Override
    public boolean onTouch(MotionEvent event) {
        return true;
    }
    @Override
    public GameState getGameState(){
        return currentState;
    }
}