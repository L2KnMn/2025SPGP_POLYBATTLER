package kr.ac.tukorea.ge.lkm.polybattler.polybattler.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.tukorea.ge.lkm.polybattler.R;
import kr.ac.tukorea.ge.spgp2025.a2dg.framework.activity.GameActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBtnStartGame(View view) {
        Intent intent = new Intent(this, BattleGameActivity.class);
        startActivity(intent);
    }
}