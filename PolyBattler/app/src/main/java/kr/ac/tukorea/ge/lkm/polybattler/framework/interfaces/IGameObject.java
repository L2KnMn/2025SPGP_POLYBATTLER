package kr.ac.tukorea.ge.lkm.polybattler.framework.interfaces;

import android.graphics.Canvas;

import kr.ac.tukorea.ge.lkm.polybattler.framework.util.Transform;

public interface IGameObject {
    public Transform getTransform();
    public void SetActive(boolean active);
    public boolean isActive();
    public void update();
    public void draw(Canvas canvas);
}
