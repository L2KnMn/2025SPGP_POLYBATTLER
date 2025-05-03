package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene; // Polyman 재활용 위해 필요

public class EnemyGenerator {
    private static final String TAG = EnemyGenerator.class.getSimpleName();
    private static final String ROUND_DATA_FILE = "rounds.json";

    private final SparseArray<RoundData> roundDataMap; // 라운드 번호를 키로 사용
    private final Random random = new Random(); // 기본 적 생성 시 필요

    /**
     * EnemyGenerator 생성자. 라운드 데이터를 로드합니다.
     * @param context AssetManager 접근을 위한 Context
     */
    public EnemyGenerator(Context context) {
        this.roundDataMap = new SparseArray<>();
        loadRoundData(context);
    }

    /**
     * rounds.json 파일에서 라운드별 적 구성 데이터를 로드합니다.
     * @param context AssetManager 접근을 위한 Context
     */
    private void loadRoundData(Context context) {
        AssetManager assets = context.getAssets();
        try {
            InputStream is = assets.open(ROUND_DATA_FILE);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            JSONObject root = new JSONObject(builder.toString());
            JSONArray rounds = root.getJSONArray("rounds");

            for (int i = 0; i < rounds.length(); i++) {
                JSONObject roundJson = rounds.getJSONObject(i);
                int roundNum = roundJson.getInt("round");
                RoundData roundData = new RoundData(roundNum);

                JSONArray enemiesJson = roundJson.getJSONArray("enemies");
                for (int j = 0; j < enemiesJson.length(); j++) {
                    JSONObject enemyJson = enemiesJson.getJSONObject(j);
                    String shapeStr = enemyJson.getString("shape");
                    int count = enemyJson.getInt("count");
                    try {
                        Polyman.ShapeType shapeType = Polyman.ShapeType.valueOf(shapeStr);
                        roundData.enemies.add(new EnemyInfo(shapeType, count));
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid shape type in " + ROUND_DATA_FILE + ": " + shapeStr);
                    }
                }
                roundDataMap.put(roundNum, roundData);
                Log.d(TAG, "Loaded round " + roundNum + " data. Total enemies: " + roundData.getTotalEnemyCount());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading round data from " + ROUND_DATA_FILE, e);
            // 파일 로딩 실패 시 에러 처리 로직 추가 가능
        }
    }

    public RoundData getRoundData(int round) {
        return roundDataMap.get(round);
    }

    /**
     * 지정된 라운드에 맞는 적 Polyman 객체 리스트를 생성하여 반환합니다.
     * 생성된 Polyman들은 전투 상태로 초기화되고 위치가 할당됩니다.
     * @param round 현재 라운드 번호
     * @param gameMap 적 배치 위치를 얻기 위한 GameMap 객체
     * @param scene Polyman 객체 재활용을 위한 Scene 객체
     * @return 생성된 적 Polyman 객체 리스트
     */
    public void generateEnemiesForRound(int round, GameMap gameMap, Scene scene, ArrayList<Polyman> generatedEnemies) {
        RoundData data = roundDataMap.get(round);
        if (data == null) {
            Log.w(TAG, "No enemy data found for round: " + round + ". Generating default enemies.");
            data = generateDefaultRoundData(round); // 기본 데이터 생성
        }

        int totalEnemies = data.getTotalEnemyCount();
        if (totalEnemies <= 0) {
            return; // 생성할 적이 없으면 빈 리스트 반환
        }

        ArrayList<Position> enemyPositions = new ArrayList<>();
        gameMap.getEnemyPostions(enemyPositions, totalEnemies); // 필요한 만큼 위치 확보

        if (enemyPositions.isEmpty()) {
            Log.w(TAG, "No spawn positions available for round " + round);
            return; // 배치할 위치가 없으면 빈 리스트 반환
        }

        int positionIndex = 0;
        for (EnemyInfo info : data.enemies) {
            for (int i = 0; i < info.count; i++) {
                if (positionIndex >= enemyPositions.size()) {
                    Log.w(TAG, "Not enough spawn positions. Stopping enemy generation.");
                    break; // 할당 가능한 위치가 부족하면 중단
                }

                // Scene의 재활용 시스템 사용
                Polyman enemy = scene.getRecyclable(Polyman.class);
                if (enemy == null) {
                    enemy = new Polyman(info.shape, Polyman.ColorType.BLACK); // 색상은 BLACK 고정
                } else {
                    enemy.init(info.shape, Polyman.ColorType.BLACK);
                }

                Position spawnPos = enemyPositions.get(positionIndex++);
                enemy.transform.set(spawnPos); // 위치 설정
                enemy.startBattle(); // 전투 상태로 전환 (GameManager가 Scene에 추가 후 호출해도 됨)
                scene.add(enemy); // Scene에 추가

                generatedEnemies.add(enemy); // 결과 리스트에 추가
                Log.d(TAG, "Prepared enemy: " + info.shape + " at (" + spawnPos.x + ", " + spawnPos.y + ")");
            }
            if (positionIndex >= enemyPositions.size()) break; // 바깥 루프도 중단
        }
    }

    /**
     * 지정된 라운드에 대한 데이터가 없을 경우 기본 적 구성을 생성합니다.
     * @param round 현재 라운드 번호
     * @return 생성된 기본 RoundData 객체 (실패 시 null 반환 가능)
     */
    private RoundData generateDefaultRoundData(int round) {
        // 예시: 라운드 번호만큼 랜덤 사각형 적 생성
        int numEnemy = Math.max(1, round); // 최소 1마리
        RoundData defaultData = new RoundData(round);
        // 간단하게 랜덤 사각형만 생성하도록 예시 구현
        Polyman.ShapeType randomShape = Polyman.ShapeType.values()[random.nextInt(Polyman.ShapeType.values().length)];
        defaultData.enemies.add(new EnemyInfo(randomShape, numEnemy));
        // 생성된 기본 데이터를 맵에 저장해두면 다음 요청 시 재사용 가능 (선택적)
        // roundDataMap.put(round, defaultData);
        Log.d(TAG, "Generated default data for round " + round + ": " + numEnemy + " " + randomShape);
        return defaultData;
    }

    public static class EnemyInfo {
        public Polyman.ShapeType shape;
        public int count;

        public EnemyInfo(Polyman.ShapeType shape, int count) {
            this.shape = shape;
            this.count = count;
        }
    }

    public static class RoundData {
        public int round;
        public List<EnemyInfo> enemies;

        public RoundData(int round) {
            this.round = round;
            this.enemies = new ArrayList<>();
        }

        public int getTotalEnemyCount() {
            int total = 0;
            for (EnemyInfo info : enemies) {
                total += info.count;
            }
            return total;
        }
    }
}