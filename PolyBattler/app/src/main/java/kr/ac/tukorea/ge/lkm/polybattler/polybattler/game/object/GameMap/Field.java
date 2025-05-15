package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;

import java.util.ArrayList;
import java.util.Random;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;

class Field extends MapPart {
    private final boolean[][] blocked;
    private final ArrayList<int[]> suffledBlocked;
    protected int countMax;

    Field(int width, int height) {
        super(width, height);
        countMax = 6;
        blocked = new boolean[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height - 3; j++) {
                blocked[j][i] = true;
            }
            for (int j = height - 3; j < height; j++) {
                blocked[j][i] = false;
            }
        }
        suffledBlocked = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (blocked[i][j]) {
                    suffledBlocked.add(new int[]{j, i});
                }
            }
        }
        for(int i = suffledBlocked.size() - 1; i > 0; --i) {
            Random random = new Random();
            int j = random.nextInt(i + 1);
            if (i != j) {
                int[] temp = suffledBlocked.get(i);
                suffledBlocked.set(i, suffledBlocked.get(j));
                suffledBlocked.set(j, temp);
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

    protected int[] getBlockRandom() {
        if (suffledBlocked.isEmpty()) {
            return null;
        }
        int[] result = suffledBlocked.get(0);
        suffledBlocked.remove(0);
        suffledBlocked.add(result);
        return result;
    }
}
