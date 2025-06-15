package kr.ac.tukorea.ge.lkm.polybattler.polybattler.app;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import kr.ac.tukorea.ge.lkm.polybattler.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MediaPlayer startPlayer = MediaPlayer.create(this, R.raw.start_sound);
        startPlayer.setLooping(false);
        startPlayer.setVolume(1.0f, 1.0f);
        startPlayer.start();
    }

    public void onBtnStartGame(View view) {
        MediaPlayer startPlayer = MediaPlayer.create(this, R.raw.touch_effect);
        startPlayer.setLooping(false);
        startPlayer.setVolume(1.0f, 1.0f);
        startPlayer.start();
        Intent intent = new Intent(this, BattleGameActivity.class);
        startActivity(intent);
    }
}