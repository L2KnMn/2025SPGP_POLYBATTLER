package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

    private final SparseArray<ArrayList<RoundData>> roundDatasMap; // 라운드 번호를 키로 사용
    private final Random random = new Random(); // 기본 적 생성 시 필요

    /**
     * EnemyGenerator 생성자. 라운드 데이터를 로드합니다.
     * @param context AssetManager 접근을 위한 Context
     */
    public EnemyGenerator(Context context) {
        this.roundDatasMap = new SparseArray<>();
        loadRoundData(context);
    }

    /**
     * rounds.json 파일에서 라운드별 적 구성 데이터를 로드합니다.
     * @param context AssetManager 접근을 위한 Context
     */
    private void loadRoundData(Context context) {
        String jsonString = null;
        boolean loadedFromInternal = false;

        // 1. 내부 저장소에서 파일 읽기 시도
        try {
            File internalFile = new File(context.getFilesDir(), ROUND_DATA_FILE);
            if (internalFile.exists()) {
                FileInputStream fis = context.openFileInput(ROUND_DATA_FILE);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                isr.close();
                fis.close();
                jsonString = builder.toString();
                Log.i(TAG, "Loaded round data from internal storage.");
            }
        } catch (FileNotFoundException e) {
            // 내부 저장소에 파일이 없는 것은 정상적인 경우일 수 있음
            Log.i(TAG, "No round data file found in internal storage.");
        } catch (Exception e) {
            Log.e(TAG, "Error reading round data from internal storage", e);
        }

        // 2. 내부 저장소에서 로드 실패 시 assets 에서 읽기 시도
        if (jsonString == null) {
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
                jsonString = builder.toString();
                Log.i(TAG, "Loaded round data from assets.");
            } catch (Exception e) {
                Log.e(TAG, "Error loading round data from assets", e);
                // assets에도 파일이 없으면 비어있는 상태로 시작
                return;
            }
        }

        // 3. JSON 파싱 (기존 로직과 동일)
        try {
            // 기존 Map 초기화 (덮어쓰기 위함)
            roundDatasMap.clear();

            JSONObject root = new JSONObject(jsonString);
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
                        Log.e(TAG, "Invalid shape type in loaded data: " + shapeStr);
                    }
                }
                ArrayList<RoundData> roundDatas = roundDatasMap.get(roundNum);
                if (roundDatas == null) {
                    roundDatas = new ArrayList<>();
                    roundDatasMap.put(roundNum, roundDatas);
                }
                roundDatas.add(roundData);
                // 로그 메시지는 로딩 완료 후 한 번만 찍거나 필요에 따라 조절
                // Log.d(TAG, "Parsed round " + roundNum + " pattern. Total enemies: " + roundData.getTotalEnemyCount());
            }
            Log.i(TAG, "Successfully parsed round data.");
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON data", e);
        }
    }

    int getRandomLevel(int round){
        int rand = random.nextInt(100);
        if(rand <= Math.max(0, round - 5)){
            return 3;
        }else if(rand <= Math.max(0, round * 2 - 5)){
            return 2;
        }else {
            return 1;
        }
    }

    public void saveRoundData(Context context, int round, ArrayList<Polyman> polymans){
        RoundData data = new RoundData(round);
        // player의 캐릭터들을 데이터에 추가
        for (Polyman character : polymans) {
            data.addData(character.getShape());
        }
        // roundDatasMap에 추가해서 재사용
        ArrayList<RoundData> roundDatas = roundDatasMap.get(round);
        if(roundDatas == null){ // 해당 라운드의 리스트가 없으면 하나 만듦
            roundDatas = new ArrayList<>();
            roundDatasMap.put(round, roundDatas);
        }
        roundDatas.add(data);
    }

    public void saveDataToJson(Context context) {
        JSONObject root = new JSONObject();
        JSONArray roundsArray = new JSONArray();
        try {
            // roundDatasMap의 모든 라운드 번호 순회
            for (int i = 0; i < roundDatasMap.size(); i++) {
                int roundNum = roundDatasMap.keyAt(i);
                ArrayList<RoundData> patternsForRound = roundDatasMap.valueAt(i);

                // 해당 라운드의 모든 패턴 순회
                for (RoundData roundData : patternsForRound) {
                    JSONObject roundPatternObject = new JSONObject();
                    roundPatternObject.put("round", roundData.round); // 라운드 번호 저장

                    JSONArray enemiesArray = new JSONArray();
                    // 해당 패턴의 모든 EnemyInfo 순회
                    for (EnemyInfo enemyInfo : roundData.enemies) {
                        JSONObject enemyObject = new JSONObject();
                        enemyObject.put("shape", enemyInfo.shape.name()); // ShapeType을 문자열로 저장
                        enemyObject.put("count", enemyInfo.count);
                        enemiesArray.put(enemyObject);
                    }
                    roundPatternObject.put("enemies", enemiesArray); // 적 정보 배열 저장
                    roundsArray.put(roundPatternObject); // 완성된 패턴 객체를 전체 배열에 추가
                }
            }
            root.put("rounds", roundsArray); // 최종 배열을 루트 객체에 추가

            // JSON 문자열 생성 (들여쓰기 4칸 적용)
            String jsonString = root.toString(4);

            // 내부 저장소에 파일 저장
            FileOutputStream fos = context.openFileOutput(ROUND_DATA_FILE, Context.MODE_PRIVATE);
            // MODE_PRIVATE: 덮어쓰기 모드 (기존 파일 있으면 삭제 후 새로 생성)
            // MODE_APPEND: 이어쓰기 모드 (필요 시 사용)

            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(jsonString);
            writer.close();
            fos.close();

            Log.i(TAG, "Successfully saved round data to internal storage: " + ROUND_DATA_FILE);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON data", e);
        } catch (Exception e) { // IOException 등 처리
            Log.e(TAG, "Error saving round data to internal storage", e);
        }
    }

    public RoundData getRoundData(int round) {
        if(roundDatasMap.get(round) == null || roundDatasMap.get(round).isEmpty()){
            return null;
        }
        return roundDatasMap.get(round).get(random.nextInt(roundDatasMap.get(round).size()));
    }

    /**
     * 지정된 라운드에 맞는 적 Polyman 객체 리스트에 담아줍니다.
     * 생성된 Polyman들은 전투 상태로 초기화되고 위치가 할당됩니다.
     * 생성된 Polymna들은 Scene에 추가된 상태입니다.
     * @param round 현재 라운드 번호
     * @param gameMap 적 배치 위치를 얻기 위한 GameMap 객체
     * @param scene Polyman 객체 재활용을 위한 Scene 객체
     * @param generatedEnemies 생성된 적 Polyman 객체 리스트
     */
    public void generateEnemiesForRound(int round, GameMap gameMap, Scene scene, ArrayList<Polyman> generatedEnemies) {
        RoundData data = getRoundData(round);
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

                Polyman enemy = scene.getRecyclable(Polyman.class);
                enemy.init(info.shape, Polyman.ColorType.BLACK, getRandomLevel(round));

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
     * @return 생성된 기본 RoundData 객체
     */
    private RoundData generateDefaultRoundData(int round) {
        // 예시: 라운드 번호만큼 랜덤 적 생성
        int numEnemy = Math.max(1, round); // 최소 1마리
        RoundData defaultData = new RoundData(round);
        // 간단하게 랜덤 적 생성
        int currNumEnemy = numEnemy;
        for(int i = 0; i < Polyman.ShapeType.values().length; i++){
            int tempNumEnemy = random.nextInt(currNumEnemy);
            currNumEnemy -= tempNumEnemy;
            defaultData.enemies.add(new EnemyInfo(Polyman.ShapeType.values()[i], tempNumEnemy));
        }
        // 생성된 기본 데이터를 맵에 저장해두면 다음 요청 시 재사용 가능 (선택적)
        // roundDataMap.put(round, defaultData);
        Log.d(TAG, "Generated default data for round " + round + ": " + numEnemy);
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

        public void addData(Polyman.ShapeType shape) {
            for (EnemyInfo enemy : enemies) {
                if(enemy.shape == shape){
                    enemy.count++;
                    return; // 원하는 shapeType의 enemyInfo를 찾았으니 종료
                }
            }
            // shapeType에 해당하는 enemyInfo가 없으면 새로 추가
            enemies.add(new EnemyInfo(shape, 1));
        }
    }
}