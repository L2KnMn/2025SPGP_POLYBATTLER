package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.GameMap;


import android.graphics.RectF;

import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Position;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.object.Transform.Transform;

class MapPart{
    protected static class Tiles {
        protected final Transform[] transforms;
        protected final int width;
        protected int count;

        Tiles(int width) {
            this.width = width;
            count = 0;
            transforms = new Transform[width];
        }
    }
    final int width;
    final int height;
    private final Tiles[] tiles;
    protected final RectF dstRect;
    protected final Position leftTop;
    protected int count;

    protected MapPart(int width, int height){
        this.width = width; this.height = height;
        tiles = new Tiles[height];
        for (int i = 0; i < height; i++) {
            tiles[i] = new Tiles(width);
        }
        dstRect = new RectF();
        leftTop = new Position();

        count = 0;
    }


    protected Transform get(int width, int height){
        return tiles[height].transforms[width];
    }

    protected Transform get(int width){
        if(height > 0)
            return tiles[0].transforms[width];
        else
            return null;
    }


    protected void set(int width, Transform transform){
        set(width, 0, transform);
    }

    protected void set(int width, int height, Transform transform){
        if(transform == null){
            count--;
        }else{
            count++;
        }
        tiles[height].transforms[width] = transform;
        tiles[height].count++;
    }


    public boolean isCorrectWidth(int width) {
        return width >= 0 && width < this.width;
    }

    public boolean isCorrectHeight(int height) {
        return height >= 0 && height < this.height;
    }

    protected boolean isCorrect(int width, int height){
        return isCorrectWidth(width) && isCorrectHeight(height);
    }
}


