package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object;

import java.util.ArrayList;
import java.util.List;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.Polyman;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap.GameMap;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class PolymanGenerator {

    private static final int fusion_condition = 3;
    private final Scene master;
    private final GameMap gameMap;

    public PolymanGenerator(Scene master, GameMap gameMap) {
        this.master = master;
        this.gameMap = gameMap;
    }

    // 오브젝트 풀에서 Polyman을 가져오거나 새로 생성하는 메소드
    private Polyman getCharacterFromPool(Polyman.ShapeType shape, Polyman.ColorType color, int level) {
        Polyman polyman = master.getRecyclable(Polyman.class);
        if (polyman == null) {
            polyman = new Polyman(shape, color, level);
        } else {
            polyman.init(shape, color, level);
        }
        return polyman;
    }

    // 벤치에 Polyman을 생성하는 메소드
    public Polyman generateCharacterBench(Polyman.ShapeType shape, Polyman.ColorType color, int level) {
        int index = gameMap.getEmptyBenchIndex();
        if (index >= 0) {
            Polyman polyman = getCharacterFromPool(shape, color, level);
            gameMap.setObjectOnBench(polyman.transform, index);
            master.add(polyman);
            return polyman;
        }
        return null;
    }

    // 맵 위에 3개의 동일한 Polyman이 있는지 확인하고 합성하는 메소드
    public boolean checkAndSynthesize(Polyman justPlacedPolyman) {
        // 방금 놓여진 Polyman 주변을 중심으로 합성 가능한 3개를 찾습니다.
        // 이 부분은 구현체의 디테일에 따라 달라질 수 있습니다.
        // 여기서는 간단하게 맵 전체를 스캔하는 방식으로 예시를 들겠습니다.

        List<Polyman> candidates = new ArrayList<>();
        Polyman.ShapeType targetShape = justPlacedPolyman.getShape();
        Polyman.ColorType targetColor = justPlacedPolyman.getColorType();
        int targetLevel = justPlacedPolyman.getLevel();

        List<Transform> polymans = gameMap.getAllObjects();

        for (Transform transform : polymans) {
            if (transform.getInstance() instanceof Polyman) {
                Polyman p = (Polyman) transform.getInstance();
                // 동일한 모양, 색상, 레벨의 Polyman을 찾습니다.
                if (p.getShape() == targetShape && p.getColorType() == targetColor && p.getLevel() == targetLevel) {
                    candidates.add(p);
                }
            }
        }

        // 3개 이상이 모이면 합성
        if (candidates.size() >= fusion_condition) {
            // 합성할 3개의 Polyman을 선택 -> 처음 찾은 2개 삭제, 남은 하나를 레벨 업
            List<Polyman> toSynthesize = candidates.subList(1, fusion_condition);

            // 새로운 레벨의 Polyman 생성
            Polyman synthesizedPolyman = candidates.get(0);

            // 기존 2개의 Polyman 제거
            for (Polyman p : toSynthesize) {
                // Polyman을 맵에서 제거
                gameMap.removeObject(p.transform.getPosition().x, p.transform.getPosition().y);
                p.remove(); // Polyman 자체를 Scene에서 제거 대기 상태로 만듭니다.
            }

            synthesizedPolyman.levelUp(); // Polyman 자체 레벨 업데이트 메소드 호출

            // 레벨 업 된 객체가 다시 레벨업 가능하므로 한 번 더 확인
            checkAndSynthesize(synthesizedPolyman);
            return true;
        }
        return false;
    }
}
