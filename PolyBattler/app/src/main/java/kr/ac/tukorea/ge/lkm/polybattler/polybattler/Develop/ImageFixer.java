package kr.ac.tukorea.ge.lkm.polybattler.polybattler.Develop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.objects.Sprite;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.res.BitmapPool;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.GameView;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.view.Metrics;

public class ImageFixer extends Sprite {
    protected int bitmapId;
    protected float fps;
    protected int frameCount;
    protected ArrayList<RectF> frames;
    protected final long createdOn;

    Paint paint;
    private boolean coin_face;
    private float coin_anim;

    static class Coord{
        public int x, y;
        Coord(){x= 0; y= 0;}
        Coord(int x, int y){
            this.x = x;
            this.y = y;
        }
        public Coord set(int x, int y){
            this.x = x;
            this.y = y;
            return this;
        }
    }

    public ImageFixer(int mipmapId) {
        super(mipmapId);
        bitmapId = mipmapId;
        srcRect = new Rect();
        createdOn = System.currentTimeMillis();
        super.setPosition(Metrics.width/2, Metrics.height/2, SPRITE_WIDTH, SPRITE_HEIGHT);

        frames = new ArrayList<>();
        frameCount = 5;

        paint = new Paint();
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        coin_face = true;
        coin_anim = 60;
    }

    public void setPos(float x, float y){
        super.setPosition(x, y, SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    @Override
    public void draw(Canvas canvas) {
        // AnimSprite 는 단순반복하는 이미지이므로 time 을 update 에서 꼼꼼히 누적하지 않아도 된다.
        // draw 에서 생성시각과의 차이로 frameIndex 를 계산한다.
        long now = System.currentTimeMillis();
        float time = (now - createdOn) / 1000.0f;
        int frameIndex = Math.round(time * fps) % frameCount;

        RectF rect = dstRect;
        Coord offset = new Coord(0, 0);
        Coord size = new Coord( SPRITE_WIDTH, SPRITE_HEIGHT);
        srcRect.set(offset.x, offset.y, offset.x + size.x, offset.y + size.y);
        canvas.drawBitmap(bitmap, srcRect, rect, null);
        canvas.drawRect(rect, paint);

        rect.offset(0, SPRITE_HEIGHT);
        offset.set(256, 0);
        size.set(256, 256);
        srcRect.set(offset.x, offset.y, offset.x + size.x, offset.y + size.y);
        canvas.drawBitmap(bitmap, srcRect, rect, null);
        canvas.drawRect(rect, paint);
    }

    private static final String TAG = "SpriteUtils";
    private static final int SPRITE_WIDTH = 256; // 실제 스프라이트 너비 (픽셀)
    private static final int SPRITE_HEIGHT = 256; // 실제 스프라이트 높이 (픽셀)

    // Activity나 다른 클래스에서 이 메소드를 호출하세요.
    public void rearrangeAndSaveSprites(Context context) {

        // 1. 원본 비트맵 로드
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap originalBitmap = bitmap;

        if (originalBitmap == null) {
            Log.e(TAG, "원본 비트맵 로드 실패");
            return;
        }
        int numberOfSpritesToDraw = 8;

        // 2. 새 비트맵 생성 (가로로 나열)
        int targetWidth = SPRITE_WIDTH * numberOfSpritesToDraw;
        int targetHeight = SPRITE_HEIGHT;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

        // 4. 캔버스 가져오기
        Canvas canvas = new Canvas(targetBitmap);

        // 5. 원하는 순서대로 스프라이트 복사/그리기
        int col = 0;
        int row = 0;
        int term = SPRITE_WIDTH/2 /(numberOfSpritesToDraw/2 + 1);
        for (int i = 0; i < numberOfSpritesToDraw/2; i++) {
            // 원본에서 가져올 영역 (Source Rect)
            int srcLeft = col * SPRITE_WIDTH;
            int srcTop = row * SPRITE_HEIGHT;
            int srcRight = srcLeft + SPRITE_WIDTH;
            int srcBottom = srcTop + SPRITE_HEIGHT;
            Rect sourceRect = new Rect(srcLeft, srcTop, srcRight, srcBottom);

            // 새 캔버스에 그릴 영역 (Destination Rect)
            int destLeft = i * SPRITE_WIDTH;
            int destTop = 0;
            int destRight = destLeft + SPRITE_WIDTH;
            int destBottom = SPRITE_HEIGHT;
            Rect destinationRect = new Rect(destLeft, destTop, destRight, destBottom);
            destinationRect.inset(i * term, 0);
            // 그리기!
            canvas.drawBitmap(originalBitmap, sourceRect, destinationRect, null);
        }

        col = 1;
        for (int i = 0; i < numberOfSpritesToDraw/2; i++) {
            // 원본에서 가져올 영역 (Source Rect)
            int srcLeft = col * SPRITE_WIDTH;
            int srcTop = row * SPRITE_HEIGHT;
            int srcRight = srcLeft + SPRITE_WIDTH;
            int srcBottom = srcTop + SPRITE_HEIGHT;
            Rect sourceRect = new Rect(srcLeft, srcTop, srcRight, srcBottom);

            // 새 캔버스에 그릴 영역 (Destination Rect)
            int destLeft = (i+numberOfSpritesToDraw/2) * SPRITE_WIDTH;
            int destTop = 0;
            int destRight = destLeft + SPRITE_WIDTH;
            int destBottom = SPRITE_HEIGHT;
            Rect destinationRect = new Rect(destLeft, destTop, destRight, destBottom);
            destinationRect.inset((numberOfSpritesToDraw/2 - i) * term, 0);
            // 그리기!
            canvas.drawBitmap(originalBitmap, sourceRect, destinationRect, null);
        }

        // 6. 결과 이미지 저장
        saveBitmapToFile(context, targetBitmap, "rearranged_sprites.png");

        // 필요시 비트맵 메모리 해제
        // originalBitmap.recycle();
    }
    // 비트맵을 파일로 저장하는 함수
    private static void saveBitmapToFile(Context context, Bitmap bitmap, String filename) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.d(TAG, "이미지 저장 성공: " + file.getAbsolutePath());
            // 미디어 스캐너 필요 시 추가
        } catch (IOException e) {
            Log.e(TAG, "이미지 저장 실패", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
