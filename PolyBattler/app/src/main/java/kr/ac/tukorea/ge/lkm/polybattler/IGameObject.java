package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;

public interface IGameObject {
    public Transform getTransform();
    public void update();
    public void draw(Canvas canvas);
}
