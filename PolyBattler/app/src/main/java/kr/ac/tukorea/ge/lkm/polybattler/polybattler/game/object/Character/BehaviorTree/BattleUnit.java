package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Character.BehaviorTree;

public class BattleUnit {
    int hp = 100;
    int maxHp = 100;
    int attack = 10;
    float attackPerSecond = 1;
    int defense = 0;
    int speed = 1; // 1초에 몇 칸 움직일 수 있는가

    public void reset(){
        hp=maxHp;
        attack=10;
        attackPerSecond=1;
        defense=0;
        speed=1;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getMaxHp(){
        return maxHp;
    }
    public int getHp(){
        return hp;
    }

    public void damage(int damage){
        hp -= damage - defense;
    }

    public void fillHp(int hp){
        this.hp = Math.min(this.hp + hp, maxHp);
    }
}
