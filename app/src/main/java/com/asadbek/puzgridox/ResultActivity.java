package com.asadbek.puzgridox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvSteps = findViewById(R.id.tv_result_steps);
        TextView tvTime = findViewById(R.id.tv_result_time);
        View btnMenu = findViewById(R.id.btn_back_menu);
        View recordBar = findViewById(R.id.record_bar);

        int steps = getIntent().getIntExtra("STEPS", 0);
        int totalSeconds = getIntent().getIntExtra("TIME", 0);

        tvSteps.setText(String.valueOf(steps));
        
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        if (tvTime != null) {
            tvTime.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
        }

        boolean isNewRecord = saveTopRecord(steps, totalSeconds);
        if (recordBar != null) {
            recordBar.setVisibility(isNewRecord ? View.VISIBLE : View.GONE);
        }

        btnMenu.setOnClickListener(v -> finish());
    }

    private boolean saveTopRecord(final int steps, final int totalSeconds) {
        SharedPreferences prefs = getSharedPreferences("PuzzlePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int[] scores = new int[3];
        int[] times = new int[3];

        for (int i = 0; i < 3; i++) {
            scores[i] = prefs.getInt("SCORE" + (i + 1), 99999);
            times[i] = prefs.getInt("TIME" + (i + 1), 99999);
        }

        int insertPos = -1;
        for (int i = 0; i < 3; i++) {
            if (totalSeconds < times[i] || (totalSeconds == times[i] && steps < scores[i])) {
                insertPos = i;
                break;
            }
        }

        if (insertPos != -1) {
            for (int i = 2; i > insertPos; i--) {
                scores[i] = scores[i - 1];
                times[i] = times[i - 1];
            }
            scores[insertPos] = steps;
            times[insertPos] = totalSeconds;

            for (int i = 0; i < 3; i++) {
                editor.putInt("SCORE" + (i + 1), scores[i]);
                editor.putInt("TIME" + (i + 1), times[i]);
            }
            editor.apply();
            return true;
        }

        return false;
    }
}