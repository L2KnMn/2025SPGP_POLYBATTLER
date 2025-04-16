package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import java.util.HashMap;
import java.util.Map;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class UiManager {
    public static final Map<Scene, UiManager> instances = new HashMap<>();
    private final Scene master;
    private UiManager(Scene master) {
        this.master = master;
    }

    public static UiManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, UiManager::new);
    }
}
