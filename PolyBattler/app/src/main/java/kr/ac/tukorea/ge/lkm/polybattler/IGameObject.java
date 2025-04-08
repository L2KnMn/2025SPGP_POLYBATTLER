package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.Canvas;

public interface IGameObject {
    public Transform getTransform();
    public void SetActive(boolean active);
    public boolean isActive();
    public void update();
    public void draw(Canvas canvas);
}
