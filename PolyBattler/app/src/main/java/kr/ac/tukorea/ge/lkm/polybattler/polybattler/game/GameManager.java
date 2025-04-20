package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.view.MotionEvent;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.DragAndDropManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class GameManager implements IGameManager {
    private static final Map<Scene, GameManager> instances = new HashMap<>();
    private final Scene master;
    private final DragAndDropManager dragAndDropManager;
    private GameState currentState;
    private int round;
    private int gold;
    private final int width = 4, height = 7, benchSize = 5;
    private final GameMap gameMap;
    private final ArrayList<Polyman> charactersPool;
    private final ArrayList<Polyman> battlers;
    private final ArrayList<UiManager.Button> cellButtons;
    private UiManager.Signage goldSignage;

    private GameManager(Scene master) {
        currentState = GameState.PREPARE;
        round = 1;
        gold = 100; // 초기 골드
        gameMap = new GameMap(width, height, benchSize);
        this.master = master;
        master.add(gameMap);

        dragAndDropManager = new DragAndDropManager(gameMap);
        dragAndDropManager.setGameState(currentState);

        Polyman polyman = new Polyman(Polyman.ShapeType.CIRCLE, Polyman.ColorType.RED);
        if(gameMap.setObjectOnTile(polyman.transform, 1, 6))
            master.add(polyman);
        else
            Log.d("GameManager", "Failed to set object on tile");

        polyman = new Polyman(Polyman.ShapeType.RECTANGLE, Polyman.ColorType.BLUE);
        if(gameMap.setObjectOnTile(polyman.transform, 2, 5))
            master.add(polyman);
        else
            Log.d("GameManager", "Failed to set object on tile");

        polyman = new Polyman(Polyman.ShapeType.TRIANGLE, Polyman.ColorType.GREEN);
        if(gameMap.setObjectOnTile(polyman.transform, 3, 5)){
            master.add(polyman);
        }else
            Log.d("GameManager", "Failed to set object on tile");

        battlers = new ArrayList<Polyman>();
        // 미리 10개 생성해두기 (벤치 사이즈 + 필드 배치 최대 개수)
        charactersPool = new ArrayList<Polyman>();
        for (int i = 0; i < benchSize + gameMap.getCountMax(); ++i) {
            charactersPool.add(new Polyman(Polyman.ShapeType.CIRCLE, Polyman.ColorType.BLACK));
        }

        cellButtons = new ArrayList<UiManager.Button>();
        AddUI();
    }

    private void AddUI(){
        goldSignage = UiManager.getInstance(master).addSignage("GOLD:"+gold, Metrics.width/2, 50, 100, 100);
        goldSignage.setColors(0x00000000, 0xFFFFFFFF);
        goldSignage.setVisibility(GameState.PREPARE, true);
        goldSignage.setVisibility(GameState.SHOPPING, true);


        // 1. "전투 시작" 버튼 생성
        // 버튼 위치 및 크기 정의
        float screenWidth = Metrics.width;
        float buttonWidth = 250;
        float buttonHeight = 100;
        float padding = 50; // 화면 가장자리로부터의 여백
        float battleButtonX = screenWidth - buttonWidth - padding;
        float battleButtonY = gameMap.getButtonLine();

        // UiManager를 사용하여 버튼 추가
        UiManager.getInstance(master).addButton("전투 시작", battleButtonX, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작
                    if (currentState == GameState.PREPARE) {
                        UiManager.getInstance(master).showToast("전투를 시작합니다."); // 사용자 피드백
                        UiManager.getInstance(master).setGameState(GameState.BATTLE); // 게임 상태 변경
                        // 버튼은 UI Manager의 이벤트 처리 함수에서 실행되기 때문에 여기서 게임 상태를 변경 해야지
                        // 모든 Manager들의 상태도 일괄적으로 변경될 수 있음
                    } else {
                        UiManager.getInstance(master).showToast("지금은 전투를 시작할 수 없습니다."); // 사용자 피드백
                    }
                }
        ).setVisibility(GameState.PREPARE, true);
        Log.d("GameManager", "전투 시작 버튼 생성 완료 at (" + battleButtonX + ", " + battleButtonY + ")");

        UiManager.getInstance(master).addButton("항복", battleButtonX, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작
                    Log.d("BATTLE SURRENDER BUTTON", "전투 종료 버튼 클릭됨!");
                    if (currentState == GameState.BATTLE) {
                        UiManager.getInstance(master).showToast("전투를 종료합니다."); // 사용자 피드백
                        UiManager.getInstance(master).setGameState(GameState.RESULT); // 게임 상태 변경
                    } else {
                        Log.d("BATTLE SURRENDER BUTTON", "현재 상태(" + currentState + ")에서는 전투를 종료할 수 없습니다.");
                        UiManager.getInstance(master).showToast("지금은 전투를 종료할 수 없습니다."); // 사용자 피드백
                    }
                }
        ).setVisibility(GameState.BATTLE, true);

        UiManager.getInstance(master).addButton("확인", battleButtonX, battleButtonY,
                buttonWidth, buttonHeight,
                () -> {            // 버튼 클릭 시 실행될 동작
                    Log.d("PREPARE BUTTON", "준비 시작 버튼 클릭됨!");
                    if (currentState == GameState.RESULT) {
                        UiManager.getInstance(master).showToast("전투를 준비합니다."); // 사용자 피드백
                        UiManager.getInstance(master).setGameState(GameState.PREPARE); // 게임 상태 변경
                    } else {
                        Log.d("PREPARE BUTTON", "현재 상태(" + currentState + ")에서는 RESULT 상태를 종료할 수 없습니다.");
                        UiManager.getInstance(master).showToast("지금은 준비 단계를 시작할 수 없습니다."); // 사용자 피드백
                    }
                }
        ).setVisibility(GameState.RESULT, true);

        for(int i = 0; i < benchSize; ++i){
            final int benchIndex = i;
            final float centerX = gameMap.getBenchX(benchIndex);
            final float centerY = gameMap.getBenchY();
            buttonWidth = gameMap.getTileSize();
            buttonHeight = gameMap.getTileSize() / 2;
            cellButtons.add(
            UiManager.getInstance(master).addButton("판매", centerX, centerY+gameMap.getTileSize(),
                    buttonWidth, buttonHeight,
                    () -> {            // 버튼 클릭 시 실행될 동작
                        if (currentState == GameState.SHOPPING) {
                            boolean result = cellCharacter(benchIndex);
                            if(result) {
                                UiManager.getInstance(master).showToast("판매되었습니다."); // 사용자 피드백
                                cellButtons.get(benchIndex).setVisibility(GameState.SHOPPING, false);
                            }
                            else
                                UiManager.getInstance(master).showToast("판매할 수 없습니다."); // 사용자 피드백
                        }else {
                            Log.d("판매 버튼", "제거될 수 없는 오브젝트를 가져와서 판매 하려 함");
                        }
                    }
            ));
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
        round++;
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        this.gold += amount;
        goldSignage.setText("GOLD:"+gold);
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
        }
        return false;
    }

    public boolean addCharacter(Polyman.ShapeType shape, Polyman.ColorType color) {
        return generateCharacterBench(shape, color);
    }

    public boolean cellCharacter(int index){
        Transform transform = gameMap.getBenchTransform(index);
        if(transform.getInstance() instanceof Polyman) {
            if(removeCharacter(transform.getInstance())) {
                addGold(1);
                return true;
            }
        }
        return false;
    }

    public boolean removeCharacter(IGameObject gameObject){
        if(gameObject instanceof Polyman) {
            master.remove(gameObject);
            Polyman polyman = (Polyman) gameObject;
            Transform temp = gameMap.findTransform(polyman.transform.getPosition().x, polyman.transform.getPosition().y);
            if(temp == polyman.transform) {
                gameMap.removeObject(polyman.transform.getPosition().x, polyman.transform.getPosition().y);
            }
            charactersPool.add(polyman);
            return true;
        }
        return false;
    }

    private Polyman getCharacterFromPool(Polyman.ShapeType shape, Polyman.ColorType color) {
        if (!charactersPool.isEmpty()) {
            charactersPool.get(0).init(shape, color);
            return charactersPool.remove(0);
        }
        return null;
    }

    public boolean generateCharacterBench(Polyman.ShapeType shape, Polyman.ColorType color){
        int index = gameMap.getEmptyBenchIndex();
        if(index >= 0) {
            Polyman polyman = getCharacterFromPool(shape, color);
            if(polyman != null) {
                gameMap.setObjectOnBench(polyman.transform, index);
                cellButtons.get(index).setVisibility(GameState.SHOPPING, true);
                master.add(polyman);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if(currentState == GameState.PREPARE)
            return dragAndDropManager.onTouch(event);
        return false;
    }

    @Override
    public GameState getGameState(){
        return currentState;
    }

    @Override
    public void setGameState(GameState newState) {
        if(currentState == GameState.SHOPPING) {
            for (int i = 0; i < benchSize; ++i) {
                cellButtons.get(i).setVisibility(GameState.SHOPPING, false);
            }
        }

        switch (newState){
            case PREPARE:
                // 전투로 인한 위치 변경 및 상태 변경을 초기화
                if(currentState == GameState.RESULT || currentState == GameState.BATTLE) {
                    gameMap.restore();
                    for (Polyman polyman : battlers) {
                        polyman.resetBattleStatus();
                    }
                    battlers.clear();
                }
                break;
            case SHOPPING:
                for(int i = 0; i < benchSize; ++i){
                    if(gameMap.getBenchTransform(i) != null)
                        cellButtons.get(i).setVisibility(GameState.SHOPPING, true);
                }
                break;
            case BATTLE:
                startBattlePhase();
                break;
            case RESULT:
                break;
            case POST_GAME:
                break;
        }
        dragAndDropManager.setGameState(newState);
        this.currentState = newState;
    }

    private void startBattlePhase(){
        for(int i = 0; i < width; ++i){
            for(int j = 0; j < height; ++j) {
                Transform transform = gameMap.findTransform(i, j);
                if(transform != null && transform.getInstance() instanceof Polyman){
                    Polyman polyman = (Polyman) transform.getInstance();
                    polyman.startBattle();
                    battlers.add(polyman);
                }
            }
        }
    }
}