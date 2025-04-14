package kr.ac.tukorea.ge.lkm.polybattler.framework.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import kr.ac.tukorea.ge.lkm.polybattler.framework.view.GameView;
import kr.ac.tukorea.ge.lkm.polybattler.polybattler.game.MainScene;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        MainScene mainScene = new MainScene(gameView);
        gameView.pushScene(mainScene);
        setContentView(gameView);

        gameView.setEmptyStackListener(this::finish);
        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            gameView.onBackPressed();
        }
    };

    public void setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30 이상: 최신 방식
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
                insetsController.hide(WindowInsets.Type.systemBars());
            }
        } else {
            // API 29 이하: 기존 방식
            int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            gameView.setSystemUiVisibility(flags);
        }

    }
}