package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.ui;

import android.graphics.Canvas;

import java.util.HashMap;
import java.util.Map;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.GameState;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.scene.Scene;

public class UiManager {
    public static final Map<Scene, UiManager> instances = new HashMap<>();
    private final Scene master;

    protected static class ToastMessenger implements IGameObject {

        ToastMessenger(){

        }

        void show(String string){

        }

        @Override
        public void update() {

        }

        @Override
        public void draw(Canvas canvas) {

        }
    }
    ToastMessenger toast;

    protected class Signage implements IGameObject {
        // 특정 게임 상태 및 게임 Scene에서 사용할 TextBoard
        // 게임 내의 위치, 크기, 색상 등을 설정할 수 있다.
        String text;
        Transform transform;

        boolean[] visible;

        Signage(String string){
            text = string;
            transform = new Transform(this);
            visible = new boolean[GameState.values().length];
            master.add(this);
        }

        @Override
        public void update() {

        }

        @Override
        public void draw(Canvas canvas) {

        }
    }

    private UiManager(Scene master) {
        this.master = master;
        toast = new ToastMessenger();
    }

    public static UiManager getInstance(Scene master) {
        return instances.computeIfAbsent(master, UiManager::new);
    }
}
