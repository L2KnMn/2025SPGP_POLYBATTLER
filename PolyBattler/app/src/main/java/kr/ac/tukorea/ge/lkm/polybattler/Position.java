package kr.ac.tukorea.ge.lkm.polybattler;

public class Position{
    private float x;
    private float y;

    public Position(){
        this.x = 0;
        this.y = 0;
    }
    public Position(float x, float y){
        this.x = x;
        this.y = y;
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
    public float getX(){
        return this.x;
    }
    public float getY(){
        return this.y;
    }
    public void move(float x, float y){
        this.x += x;
        this.y += y;
    }
    public void move(Position pos){
        this.x += pos.getX();
        this.y += pos.getY();
    }
    public void moveTo(float x, float y){
        this.x = x;
        this.y = y;
    }
    public void moveTo(Position pos){
        this.x = pos.getX();
    }
    public float distance(Position pos) {
        this.x = Math.abs(this.x - pos.getX());
        this.y = Math.abs(this.y - pos.getY());
        return (float) Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }
}
