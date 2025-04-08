package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.RectF;

public class Transform {
    private float x;
    private float y;
    private float radian;
    private float size;

    private RectF body;
    private boolean rigid;

    public Transform(){
        this.x = 0;
        this.y = 0;
        init();
    }
    public Transform(float x, float y){
        this.x = x;
        this.y = y;
        init();
    }

    private void init(){
        this.radian = 0;
        this.size = 1;
        this.rigid = false;
        body = new RectF(x-size/2, y-size/2, x+size/2, y+size/2);
    }
    public Transform(float x, float y, float radian, float size){
        this.x = x;
        this.y = y;
    }
    public void setRigid(boolean rigid){
        this.rigid = rigid;
    }
    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }
    public void setX(float x){
        this.x = x;
    }
    public void setY(float y){
        this.y = y;
    }
    public void setAngle(float radian){
        this.radian = radian;
    }
    public void setSize(float size){
        this.size = size;
        body.set(x-size/2, y-size/2, x+size/2, y+size/2);
    }
    public float getX(){
        return this.x;
    }
    public float getY(){
        return this.y;
    }
    public boolean isRigid(){
        return this.rigid;
    }
    public float getAngle(){
        return this.radian;
    }
    public float getSize(){
        return this.size;
    }
    public RectF getRect(){
        return body;
    }
    public void move(float x, float y){
        this.x += x;
        this.y += y;
    }
    public void move(Transform pos){
        this.x += pos.getX();
        this.y += pos.getY();
    }
    public void moveTo(float x, float y){
        this.x = x;
        this.y = y;
    }
    public void moveTo(Transform pos){
        this.x = pos.getX();
    }
    public float distance(Transform pos) {
        this.x = Math.abs(this.x - pos.getX());
        this.y = Math.abs(this.y - pos.getY());
        return (float) Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }
}
