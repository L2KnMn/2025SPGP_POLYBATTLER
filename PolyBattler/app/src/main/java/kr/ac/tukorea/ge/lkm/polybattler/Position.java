package kr.ac.tukorea.ge.lkm.polybattler;

public class Position {
    public float x;
    public float y;

    Position(){
        this.x = 0;
        this.y = 0;
    }
    Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }

    public void set(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public void add(float x, float y){
        this.x += x;
        this.y += y;
    }

    public void add(Position pos){
        this.x += pos.x;
        this.y += pos.y;
    }
}
