package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class Shop implements IGameObject {
    boolean active;
    public Shop() {
        active = true;
    }
    @Override
    public boolean isActive() {
        return active;
    }
    @Override
    public void SetActive(boolean active) {
        this.active = active;
    }
    @Override
    public void update() {
        // 업데이트 로직
        if (active) {

        }
    }
    @Override
    public void draw(Canvas canvas) {
        // 드로잉 로직
        if (active) {

        }else{

        }
    }
    @Override
    public Transform getTransform() {
        Log.d("Shop", "call Shop's getTransform() it is error");
        return null;
    }
}
