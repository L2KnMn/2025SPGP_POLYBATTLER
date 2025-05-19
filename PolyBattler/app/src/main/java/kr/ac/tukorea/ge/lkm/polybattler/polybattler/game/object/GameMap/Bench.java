package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;

import java.util.ArrayList;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameManager;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui.UiManager;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class Bench extends MapPart{
    private final ArrayList<UiManager.Button> cellButtons;
    protected Bench(Scene master, int width, int height) {
        super(width, height);
        cellButtons = new ArrayList<>();
    }

    protected void addCellButton(UiManager.Button button) {
        cellButtons.add(button);
    }

    public UiManager.Button getCellButton(int benchIndex) {
        return cellButtons.get(benchIndex);
    }

    @Override
    protected void set(int width, Transform transform){
        super.set(width, 0, transform);
        if(isCorrectWidth(width))
            cellButtons.get(width).setVisibility(GameState.SHOPPING,transform != null);
    }
}
