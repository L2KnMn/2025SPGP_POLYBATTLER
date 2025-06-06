package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform;

import android.graphics.Path;
import android.graphics.RectF;

import kr.ac.tukorea.ge.spgp2025.a2dg.framework.interfaces.IGameObject;

public class Transform {
    private final IGameObject instance;
    public final Position position;
    private float radian;
    private float width, height;
    private boolean active;
    private Path path;
    private RectF body;
    private boolean rigid;

    public Transform(IGameObject parent){
        instance = parent;
        position = new Position(0, 0);
        init();
    }
    public Transform(IGameObject parent, float x, float y){
        instance = parent;
        position = new Position(x, y);
        init();
    }
    public Transform(IGameObject parent, Position position){
        instance = parent;
        this.position = position;
        init();
    }
    private void init(){
        body = new RectF();
        path = new Path();
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
    public void set(Position pos){
        position.set(pos);
    }
    public void setAngle(float angle){
        this.radian = angle * (float) Math.PI / 180.0f;
    }
    public void turnLeft(float radian){
        this.radian -= radian;
    }
    public void turnRight(float radian){
        this.radian += radian;
    }
    public void setSize(float size){
        width = size;
        height = size;
    }
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
    public Position getPosition(){ return position; }
    public boolean isRigid(){
        return this.rigid;
    }
    public float getAngle(){
        return this.radian * 180.0f / (float) Math.PI;
    }
    public float getSize(){
        return Math.min(this.width, this.height);
    }
    public float getWidth(){
        return this.width;
    }
    public float getHeight(){
        return this.height;
    }
    public RectF getRect(){
        body.set(position.x-width/2, position.y-height/2,
                position.x+width/2, position.y+height/2);
        return body;
    }

    public float crossProduct(float p1x, float p1y, float p2x, float p2y, Position p3) {
        return (p2x - p1x) * (p3.y - p1y) - (p2y - p1y) * (p3.x - p1x);
    }

    final static double startangle = 150.0f / 360.0f * 2 * Math.PI;
    final static double unit_angle = 2 * Math.PI / 3; // 정삼각형의 각 점 사이의 각도 (120도 라디안)
    public boolean isPointInTriangle(Position p) {
        float ax = position.x + getSize() * (float)Math.cos(startangle);
        float ay = position.y + getSize() * (float)Math.sin(startangle);

        float bx = position.x + getSize() * (float)Math.cos(unit_angle + startangle);
        float by = position.y + getSize() * (float)Math.sin(unit_angle + startangle);

        float cx = position.x + getSize() * (float)Math.cos(unit_angle * 2 + startangle);
        float cy = position.y + getSize() * (float)Math.sin(unit_angle * 2 + startangle);

        float abp = crossProduct(ax, ay, bx, by, p);
        float bcp = crossProduct(bx, by, cx, cy, p);
        float cap = crossProduct(cx, cy, ax, ay, p);
        boolean b1 = abp >= 0;
        boolean b2 = bcp >= 0;
        boolean b3 = cap >= 0;
        return (b1 == b2) && (b2 == b3);
    }
    public Path getTriangle(){
        float size = this.getSize() * 0.7f;
//        double startangle = this.radian + Transform.startangle;
        path.reset();
        path.moveTo(position.x + size * (float)Math.cos(startangle), position.y + size * (float)Math.sin(startangle));
        path.lineTo(position.x + size * (float)Math.cos(unit_angle + startangle), position.y + size * (float)Math.sin(unit_angle + startangle));
        path.lineTo(position.x + size * (float)Math.cos(unit_angle * 2 + startangle), position.y + size * (float)Math.sin(unit_angle * 2 + startangle));

        return path;
    }
    public void move(float x, float y){
       position.x += x;
       position.y += y;
    }
    public void move(Transform pos){
        position.add(pos.position);
    }
    public void goTo(float x, float y){
        position.set(x, y);
    }
    public void goTo(Transform pos){
        position.set(pos.position);
    }
    public float distance(Transform pos) {
        float dx = Math.abs(this.position.x - pos.position.x);
        float dy = Math.abs(this.position.y - pos.position.y);
        return (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
    public float distance(Position pos){
        return distance(pos.x, pos.y);
    }
    public float distance(float x, float y) {
        float dx = Math.abs(this.position.x - x);
        float dy = Math.abs(this.position.y - y);
        return (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
    public float distanceSq(Position position) {
        float dx = Math.abs(this.position.x - position.x);
        float dy = Math.abs(this.position.y - position.y);
        return (float) Math.pow(dx, 2) + (float) Math.pow(dy, 2);
    }
    public IGameObject getInstance() {
        return instance;
    }
    public void sleep(){
        active = false;
    }
    public void wakeUp(){
        active = true;
    }
    public boolean isActive(){
        return active;
    }

    public void lookAt(float x, float y){
        float dx = x - position.x;
        float dy = y - position.y;
        radian = (float) (Math.atan2(dy, dx) + Math.PI/2.0f);
    }
    public void lookAt(Position pos){
        lookAt(pos.x, pos.y);
    }
}
