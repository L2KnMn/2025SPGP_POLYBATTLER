package kr.ac.tukorea.ge.lkm.polybattler;

import android.graphics.RectF;

public class Transform {
    private Position position;
    private float radian;
    private float size;

    private RectF body;
    private boolean rigid;

    public Transform(){
        position = new Position();
        init();
    }
    public Transform(float x, float y){
        position = new Position(x, y);
        init();
    }
    private void init(){
        body = new RectF();
        setAngle(0);
        setSize(1);
        setRigid(false);
    }
    public void setRigid(boolean rigid){
        this.rigid = rigid;
    }
    public void set(float x, float y){
        position.set(x, y);
    }
    public void setAngle(float radian){
        this.radian = radian;
    }
    public void turnLeft(float radian){
        this.radian -= radian;
    }
    public void turnRight(float radian){
        this.radian += radian;
    }
    public void setSize(float size){
        this.size = size;
        body.set(position.x-size, position.y-size,
                position.x+size, position.y+size);
    }
    public Position getPosition(){ return position; }
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
        body.set(position.x-size, position.y-size,
                position.x+size, position.y+size);
        return body;
    }
    public void move(float x, float y){
       position.x += x;
       position.y += y;
    }
    public void move(Transform pos){
        position.add(pos.position);
    }
    public void moveTo(float x, float y){
        position.set(x, y);
    }
    public void moveTo(Transform pos){
        position.set(pos.position);
    }
    public float distance(Transform pos) {
        this.position.x = Math.abs(this.position.x - pos.position.x);
        this.position.y = Math.abs(this.position.y - pos.position.y);
        return (float) Math.sqrt(Math.pow(this.position.x, 2) + Math.pow(this.position.y, 2));
    }
}
