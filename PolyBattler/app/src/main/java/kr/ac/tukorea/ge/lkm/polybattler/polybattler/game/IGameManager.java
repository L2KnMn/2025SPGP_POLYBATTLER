package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

public interface IGameManager {
    // 게임 상태 관련 기능
    GameState getCurrentState();
    void setGameState(GameState newState);
    int getRound();
    void nextRound();

    // 자원 관리 기능 (예: 골드)
    int getGold();
    void addGold(int amount);
    boolean spendGold(int amount);

    // 기타 게임 관리 기능
    // ...
}