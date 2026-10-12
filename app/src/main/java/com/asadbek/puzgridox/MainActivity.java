package com.asadbek.puzgridox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final TextView[] views = new TextView[16];
    private final int[][] matrix = new int[4][4];
    private int x = -1, y = -1, stepCount = 0;
    private TextView tvSteps;
    private TextView tvTime;
    private final Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private MediaPlayer clickSfx;
    private String userName;
    private boolean isGameFinished = false;
    private boolean isSoundOn = true;
    private final java.util.Stack<GameState> history = new java.util.Stack<>();

    private static class GameState {
        int[][] matrix;
        int steps;
        int seconds;

        GameState(int[][] m, int s, int sec) {
            this.matrix = new int[4][4];
            for (int i = 0; i < 4; i++) System.arraycopy(m[i], 0, this.matrix[i], 0, 4);
            this.steps = s;
            this.seconds = sec;
        }
    }

    private void saveState() {
        history.push(new GameState(matrix, stepCount, secondsElapsed));
    }

    private void undo() {
        if (!history.isEmpty() && !isGameFinished) {
            GameState lastState = history.pop();
            for (int i = 0; i < 4; i++) {
                System.arraycopy(lastState.matrix[i], 0, matrix[i], 0, 4);
            }
            stepCount = lastState.steps;
            secondsElapsed = lastState.seconds;
            
            tvSteps.setText(String.valueOf(stepCount));
            int mins = secondsElapsed / 60;
            int secs = secondsElapsed % 60;
            if (tvTime != null) {
                tvTime.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            }

            showAmountToViews();
            saveData();
        }
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGameFinished) {
                secondsElapsed++;
                int mins = secondsElapsed / 60;
                int secs = secondsElapsed % 60;
                if (tvTime != null) {
                    tvTime.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
                }
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userName = getIntent().getStringExtra("USER_NAME");
        tvSteps = findViewById(R.id.tv_steps);
        tvTime = findViewById(R.id.tv_time);
        clickSfx = MediaPlayer.create(this, R.raw.clicktabb);

        loadViews();

        boolean isContinue = getIntent().getBooleanExtra("IS_CONTINUE", false);
        if (isContinue) {
            loadData();
        } else {
            getSharedPreferences("PuzzlePrefs", MODE_PRIVATE).edit().clear().apply();
            loadMatrix();
        }

        timerHandler.postDelayed(timerRunnable, 1000);

        clickUIViews();

        findViewById(R.id.btn_refresh).setOnClickListener(v -> {
            showConfirmDialog(getString(R.string.msg_confirm_restart), () -> restartGame());
        });
        
        findViewById(R.id.btn_exit).setOnClickListener(v -> {
            showConfirmDialog(getString(R.string.msg_confirm_exit), () -> {
                getSharedPreferences("PuzzlePrefs", MODE_PRIVATE).edit()
                    .remove("steps")
                    .remove("seconds")
                    .apply();
                finish();
            });
        });

        ImageView btnSound = findViewById(R.id.btn_sound);
        btnSound.setOnClickListener(v -> {
            isSoundOn = !isSoundOn;
            btnSound.setImageResource(isSoundOn ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        });
        
        View btnBackBottom = findViewById(R.id.btn_back_bottom);
        if (btnBackBottom != null) {
            btnBackBottom.setOnClickListener(v -> undo());
        }
    }

    private void loadViews() {
        int[] resIds = {
                R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7, R.id.button8,
                R.id.button9, R.id.button10, R.id.button11, R.id.button12,
                R.id.button13, R.id.button14, R.id.button15, R.id.button16
        };
        for (int i = 0; i < 16; i++) views[i] = findViewById(resIds[i]);
    }

    private void loadMatrix() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 16; i++) list.add(i);

        do {
            Collections.shuffle(list);
        } while (!isSolvable(list));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = list.get(4 * i + j);
            }
        }

        showAmountToViews();
    }

    private boolean isSolvable(ArrayList<Integer> list) {
        int inversions = 0;
        int blankRowFromBottom = 0;

        for (int i = 0; i < 16; i++) {
            int a = list.get(i);
            if (a == 16) {
                blankRowFromBottom = 4 - (i / 4);
                continue;
            }
            for (int j = i + 1; j < 16; j++) {
                int b = list.get(j);
                if (b != 16 && a > b) {
                    inversions++;
                }
            }
        }

        if (blankRowFromBottom % 2 != 0) {
            return inversions % 2 == 0;
        } else {
            return inversions % 2 != 0;
        }
    }

    private void clickUIViews() {
        for (int i = 0; i < views.length; i++) {
            views[i].setTag(i);
            views[i].setOnClickListener(v -> {
                int index = (int) v.getTag();
                if (canMove(index)) {
                    saveState();
                    if (isSoundOn && clickSfx != null) clickSfx.start();

                    stepCount++;
                    tvSteps.setText(String.valueOf(stepCount));

                    swapAmount(index);
                    saveData();
                }
            });
        }
    }

    private void showAmountToViews() {
        for (int i = 0; i < 16; i++) {
            int val = matrix[i / 4][i % 4];
            views[i].setText(String.valueOf(val));
            if (val == 16) {
                views[i].setVisibility(View.INVISIBLE);
                x = i / 4; y = i % 4;
            } else {
                views[i].setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean canMove(int index) {
        int r = index / 4, c = index % 4;
        return (Math.abs(r - x) == 1 && c == y) || (Math.abs(c - y) == 1 && r == x);
    }

    private void swapAmount(int index) {
        int r = index / 4, c = index % 4;
        int temp = matrix[r][c];
        matrix[r][c] = matrix[x][y];
        matrix[x][y] = temp;
        showAmountToViews();

        if (isFinish()) {
            isGameFinished = true;
            timerHandler.removeCallbacks(timerRunnable);

            getSharedPreferences("PuzzlePrefs", MODE_PRIVATE).edit().clear().apply();

            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("NAME", userName);
            intent.putExtra("STEPS", stepCount);
            intent.putExtra("TIME", secondsElapsed);
            startActivity(intent);
            finish();
        }
    }

    private boolean isFinish() {
        for (int i = 0; i < 15; i++) {
            if (matrix[i / 4][i % 4] != (i + 1)) return false;
        }
        return true;
    }

    private void restartGame() {
        stepCount = 0;
        tvSteps.setText("0");
        secondsElapsed = 0;
        if (tvTime != null) tvTime.setText("00:00");
        isGameFinished = false;
        history.clear();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
        getSharedPreferences("PuzzlePrefs", MODE_PRIVATE).edit().clear().apply();
        loadMatrix();
    }

    private void saveData() {
        if (isGameFinished || stepCount < 0) return;

        SharedPreferences.Editor editor = getSharedPreferences("PuzzlePrefs", MODE_PRIVATE).edit();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                editor.putInt("matrix_" + i + "_" + j, matrix[i][j]);
            }
        }
        editor.putInt("steps", stepCount);
        editor.putInt("seconds", secondsElapsed);
        editor.putString("user", userName);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("PuzzlePrefs", MODE_PRIVATE);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = prefs.getInt("matrix_" + i + "_" + j, i * 4 + j + 1);
            }
        }
        stepCount = prefs.getInt("steps", 0);
        secondsElapsed = prefs.getInt("seconds", 0);
        userName = prefs.getString("user", "Player");
        tvSteps.setText(String.valueOf(stepCount));
        
        int mins = secondsElapsed / 60;
        int secs = secondsElapsed % 60;
        if (tvTime != null) {
            tvTime.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
        }

        showAmountToViews();
    }

    private void showConfirmDialog(String message, Runnable onYes) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.85f);
        }

        TextView tvMessage = dialogView.findViewById(R.id.tv_confirm_message);
        tvMessage.setText(message);

        dialogView.findViewById(R.id.btn_no).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_yes).setOnClickListener(v -> {
            dialog.dismiss();
            onYes.run();
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}