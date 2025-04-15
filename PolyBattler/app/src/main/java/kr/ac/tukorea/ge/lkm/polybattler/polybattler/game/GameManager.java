package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

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

    @Override
    public GameState getCurrentState() {
        return currentState;
    }

    @Override
    public void setGameState(GameState newState) {
        this.currentState = newState;
    }

    @Override
    public int getRound() {
        return round;
    }

    @Override
    public void nextRound() {
        round++;
    }

    @Override
    public int getGold() {
        return gold;
    }

    @Override
    public void addGold(int amount) {
        this.gold += amount;
    }

    @Override
    public boolean spendGold(int amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }
}