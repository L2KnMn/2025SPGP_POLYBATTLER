package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;

import androidx.core.content.res.ResourcesCompat;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.BattleController;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.EnemyGenerator;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.Background;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.shop.ShopManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.DragAndDropEventController;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class GameManager implements IGameManager {

    static Random random = new Random();
    private static final Map<Scene, GameManager> instances = new HashMap<>();
    private final Scene master;
    private final DragAndDropEventController dragAndDropEventController;
    private GameState currentState;
    private int round;
    private int gold;
    private final int width = 4, height = 7, benchSize = 5;
    private final GameMap gameMap;
    private final ArrayList<UiManager.Button> cellButtons;
    private UiManager.ScoreBoard goldBoard;
    private final BattleController battleController;
    private final EnemyGenerator enemyGenerator;
    private final ArrayList<Polyman> players;
    private final ArrayList<Polyman> enemies;

    private GameManager(Scene master) {
        currentState = GameState.PREPARE;
        round = 1;
        gold = 100; // 초기 골드
        gameMap = new GameMap(width, height, benchSize);
        this.master = master;
        master.add(Layer.map, gameMap);

        dragAndDropEventController = new DragAndDropEventController();

        cellButtons = new ArrayList<>();

        battleController = new BattleController(master);
        players = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyGenerator = new EnemyGenerator(GameView.view.getContext());
        AddUI();
    }

    private void AddUI() {
        goldBoard = UiManager.getInstance(master).addScoreBoard("GOLD:", gold, Metrics.width / 2, 50, 100, 100);
        Resources res =  GameView.view.getResources();
        goldBoard.setColors(ResourcesCompat.getColor(res, R.color.goldSignageBg, null), ResourcesCompat.getColor(res,R.color.goldSignageText, null));
        goldBoard.setTextSize(100);
        goldBoard.setVisibility(GameState.PREPARE, true);
        goldBoard.setVisibility(GameState.SHOPPING, true);


        // 1. "전투 시작" 버튼 생성
        // 버튼 위치 및 크기 정의
        float screenWidth = Metrics.width;
        float buttonWidth = 250;
        float buttonHeight = 100;
        float battleButtonX = Metrics.width / 2;
        float battleButtonY = gameMap.getButtonLine();

        // UiManager를 사용하여 버튼 추가
        UiManager.getInstance(master).addButton("전투 시작", battleButtonX, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작
                    if (currentState == GameState.PREPARE && gameMap.getFieldCount() > 0) {
                        UiManager.getInstance(master).showToast("전투를 시작합니다."); // 사용자 피드백
                        MasterManager.getInstance(master).setGameState(GameState.BATTLE); // 게임 상태 변경
                        // 버튼은 UI Manager의 이벤트 처리 함수에서 실행되기 때문에 여기서 게임 상태를 변경 해야지
                        // 모든 Manager들의 상태도 일괄적으로 변경될 수 있음
                    } else {
                        UiManager.getInstance(master).showToast("지금은 전투를 시작할 수 없습니다."); // 사용자 피드백
                    }
                }
        ).setVisibility(GameState.PREPARE, true);
//        Log.d("GameManager", "전투 시작 버튼 생성 완료 at (" + battleButtonX + ", " + battleButtonY + ")");

        UiManager.getInstance(master).addButton("항복", battleButtonX, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작
                    Log.d("BATTLE SURRENDER BUTTON", "전투 종료 버튼 클릭됨!");
                    if (currentState == GameState.BATTLE) {
                        UiManager.getInstance(master).showToast("전투를 종료합니다."); // 사용자 피드백
                        battleController.resignPlayer();
                        MasterManager.getInstance(master).setGameState(GameState.RESULT); // 게임 상태 변경
                    } else {
                        //Log.d("BATTLE SURRENDER BUTTON", "현재 상태(" + currentState + ")에서는 전투를 종료할 수 없습니다.");
                        UiManager.getInstance(master).showToast("지금은 전투를 종료할 수 없습니다."); // 사용자 피드백
                    }
                }
        ).setVisibility(GameState.BATTLE, true);

        UiManager.getInstance(master).addButton("확인", battleButtonX, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작
                    Log.d("CONFIRM BUTTON", "전투 결과 확인 버튼 클릭됨!");
                    if (currentState == GameState.RESULT) {
                        if(battleController.getResult() == BattleController.Result.WIN) {
                            nextRound();
                            UiManager.getInstance(master).showToast("전투를 다시 준비합니다."); // 사용자 피드백
                            MasterManager.getInstance(master).setGameState(GameState.PREPARE); // 게임 상태 변경
                        }else if(battleController.getResult() == BattleController.Result.LOSE){
                            UiManager.getInstance(master).showToast("게임을 마무리합니다."); // 사용자 피드백
                            MasterManager.getInstance(master).setGameState(GameState.POST_GAME); // 게임 상태 변경
                        }
                    } else {
//                        Log.d("PREPARE BUTTON", "현재 상태(" + currentState + ")에서는 RESULT 상태를 종료할 수 없습니다.");
                        UiManager.getInstance(master).showToast("지금은 준비 단계를 시작할 수 없습니다."); // 사용자 피드백
                    }
                }
        ).setVisibility(GameState.RESULT, true);

        UiManager.getInstance(master).addSignage("저장하고 종료하시겠습니까?", battleButtonX, Metrics.height/2,
                Metrics.width, Metrics.height).setVisibility(GameState.POST_GAME, true)
                .setColors(R.color.resultBoardbg, R.color.resultBoardText);

        UiManager.getInstance(master).addButton("아니요", battleButtonX - buttonWidth/3*2, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작)
                    cleanGamePhase(false);
                }
        ).setVisibility(GameState.POST_GAME, true);

        UiManager.getInstance(master).addButton("예", battleButtonX + buttonWidth/3*2, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작)
                    cleanGamePhase(true);
                }
        ).setVisibility(GameState.POST_GAME, true);

        for (int i = 0; i < benchSize; ++i) {
            final int benchIndex = i;
            final float centerX = gameMap.getBenchX(benchIndex);
            final float centerY = gameMap.getBenchY();
            buttonWidth = gameMap.getTileSize();
            buttonHeight = gameMap.getTileSize() / 2;
            cellButtons.add(
                    UiManager.getInstance(master).addButton("판매", centerX, centerY + gameMap.getTileSize(),
                            buttonWidth, buttonHeight,
                            () -> {            // 버튼 클릭 시 실행될 동작
                                if (currentState == GameState.SHOPPING) {
                                    boolean result = cellCharacter(benchIndex);
                                    if (result) {
                                        UiManager.getInstance(master).showToast("판매되었습니다."); // 사용자 피드백
                                        cellButtons.get(benchIndex).setVisibility(GameState.SHOPPING, false);
                                    } else
                                        UiManager.getInstance(master).showToast("판매할 수 없습니다."); // 사용자 피드백
                                }
                            }
                    )
            );
//            cellButtons.get(i).setVisibility(GameState.PREPARE, true);
        }
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

    public int getRound() {
        return round;
    }

    public void nextRound() {
        ShopManager.getInstance(master).reRoll();
        addGold(10);
        round++;
    }

    public int getGold() {
        return gold;
    }

    StringBuilder stringBuilder = new StringBuilder();
    public void addGold(int amount) {
        this.gold += amount;
        goldBoard.setScore(gold);
    }

    public boolean spendGold(int amount) {
        if (this.gold >= amount) {
            addGold(-amount);
            return true;
        }
        return false;
    }

    public boolean purchaseCharacter(int price, Polyman.ShapeType shape, Polyman.ColorType color) {
        if (spendGold(price)) {
            return addCharacter(shape, color);
        }else
            return false;
    }

    public boolean addCharacter(Polyman.ShapeType shape, Polyman.ColorType color) {
        return generateCharacterBench(shape, color);
    }

    public boolean cellCharacter(int index) {
        Transform transform = gameMap.getBenchTransform(index);
        if (transform != null) {
            boolean result = removeCharacter(transform.getInstance());
            if(result) {
                addGold(1);
                return true;
            }
        }
        return false;
    }
    public boolean removeCharacter(IGameObject gameObject) {
        if (gameObject instanceof Polyman) {
            Polyman polyman = (Polyman) gameObject;
            polyman.remove(); // Event 처리에서  직접 없애는 게 아니라 update에서 자삭 맡김
            Transform temp = gameMap.findTransform(polyman.transform.getPosition().x, polyman.transform.getPosition().y);
            if (temp == polyman.transform) {
                gameMap.removeObject(polyman.transform.getPosition().x, polyman.transform.getPosition().y);
            }
            return true;
        }
        return false;
    }
    private Polyman getCharacterFromPool(Polyman.ShapeType shape, Polyman.ColorType color) {
        Polyman polyman = master.getRecyclable(Polyman.class);
        if(polyman == null){
            polyman = new Polyman(shape, color);
        } else {
            polyman.init(shape, color, 1);
        }
        return polyman;
    }
    private boolean generateCharacterBench(Polyman.ShapeType shape, Polyman.ColorType color) {
        int index = gameMap.getEmptyBenchIndex();
        if (index >= 0) {
            Polyman polyman = getCharacterFromPool(shape, color);
            gameMap.setObjectOnBench(polyman.transform, index);
            cellButtons.get(index).setVisibility(GameState.SHOPPING, true);
            master.add(polyman);
            return true;
        }
        return false;
    }
    @Override
    public boolean onTouch(MotionEvent event) {
        if (currentState == GameState.PREPARE) {
            boolean result = dragAndDropEventController.onTouch(event, gameMap);
            if(result && event.getAction() == MotionEvent.ACTION_UP){
                addBattlePlayers();
                battleController.updateSynergy();
            }
            return result;
        }
        return false;
    }
    @Override
    public GameState getGameState() {
        return currentState;
    }
    @Override
    public IGameManager setGameState(GameState newState) {
        if (currentState == GameState.SHOPPING && newState != GameState.SHOPPING) {
            for (int i = 0; i < benchSize; ++i) {
                cellButtons.get(i).setVisibility(GameState.SHOPPING, false);
            }
        }
        switch (newState) {
            case PREPARE:
                // 전투로 인한 위치 변경 및 상태 변경을 초기화
                gameMap.setDrawBlocked(true);
                break;
            case SHOPPING:
                for (int i = 0; i < benchSize; ++i) {
                    if (gameMap.getBenchTransform(i) != null)
                        cellButtons.get(i).setVisibility(GameState.SHOPPING, true);
                }
                break;
            case BATTLE: // 플레이어가 전투 준비를 마치면 진입하는 단계, 전투를 진행함
                startBattlePhase();
                break;
            case RESULT: // 플레이어가 전투를 마치면 진입하는 단계, 전투 결과를 확인시켜주고 플레이어가 확인할 때까지 대기
                endBattlePhase();
                break;
            case POST_GAME: // 플레이어가 게임 오버되면 진입하는 단계, 전투 결과 저장 후 게임 종료 -> MainActivity로 돌아가기
                break;
        }
        this.currentState = newState;
        return this;
    }

    private void cleanGamePhase(boolean saveData) {
        Context context = GameView.view.getContext();
        if (context instanceof Activity) {
            if(saveData) {
                enemyGenerator.saveRoundData(context, getRound(), players);
                enemyGenerator.saveDataToJson(context);
            }
            ((Activity) context).finish();
            Log.i("Game Manager", "Activity finished by GameView request.");
        } else {
            Log.e("Game Manager", "Context is not an Activity, cannot finish.");
        }
    }

    private void endBattlePhase() {
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                Transform transform = gameMap.findTransform(i, j);
                if (transform != null && transform.getInstance() instanceof Polyman) {
                    Polyman polyman = (Polyman) transform.getInstance();
                    polyman.resetBattleStatus();
                }
            }
        }
        battleController.endBattle();
        gameMap.restore();
        for(Polyman enemy : enemies){
            enemy.resetBattleStatus();
            enemy.remove();
        }
        enemies.clear();
    }
    private void startBattlePhase() {
        gameMap.setDrawBlocked(false);
        addBattlePlayers();
        addBattleEnemy();
        battleController.startBattle();
    }

    private void addBattlePlayers(){
        players.clear();
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                Transform transform = gameMap.findTransform(i, j);
                if (transform != null && transform.getInstance() instanceof Polyman) {
                    Polyman polyman = (Polyman) transform.getInstance();
                    polyman.startBattle();
                    players.add(polyman);
                    battleController.addBattler(polyman);
                }
            }
        }
    }

    private Polyman.ShapeType getRandomShape(){
        return Polyman.ShapeType.values()[random.nextInt(Polyman.ShapeType.values().length)];
    }
    private Polyman.ColorType getRandomColor(){
        return Polyman.ColorType.values()[random.nextInt(Polyman.ColorType.values().length)];
    }

    private void addBattleEnemy(){
        enemyGenerator.generateEnemiesForRound(round, gameMap, master, enemies);

        int numEnemy = enemies.size();
        for(Polyman enemy : enemies) {
            battleController.addEnemy(enemy);
        }
    }
}