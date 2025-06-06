package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform;

public class Position {
    public float x;
    public float y;

    public Position(){
        this.x = 0;
        this.y = 0;
    }
    public Position(float x, float y) {
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

    public void makeVector(Position pos1, Position pos2){
           x = pos2.x - pos1.x;
           y = pos2.y - pos1.y;
    }

    public void makeVector(Position pos1, Position pos2, float length){
        makeVector(pos1, pos2);
        float scale = length / (float) Math.sqrt(x * x + y * y);
        x *= scale;
        y *= scale;
    }

    public Position normalize(){
        float scale = 1.0f / (float) Math.sqrt(x * x + y * y);
        x *= scale;
        y *= scale;
        return this;
    }
}
