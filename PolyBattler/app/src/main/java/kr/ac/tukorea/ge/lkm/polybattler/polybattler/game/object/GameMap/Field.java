package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;

class Field extends MapPart {
    private final boolean[][] blocked;
    protected int countMax;

    Field(int width, int height) {
        super(width, height);
        countMax = 5;
        blocked = new boolean[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height - 3; j++) {
                blocked[j][i] = true;
            }
            for (int j = height - 3; j < height; j++) {
                blocked[j][i] = false;
            }
        }
    }

    boolean block(int width, int height) {
        return blocked[height][width];
    }

    boolean full() {
        return super.count >= countMax;
    }

    @Override
    protected void set(int width, int height, Transform transform) {
        super.set(width, height, transform);
    }

    protected void setCountMax(int max) {
        countMax = max;
    }
}
