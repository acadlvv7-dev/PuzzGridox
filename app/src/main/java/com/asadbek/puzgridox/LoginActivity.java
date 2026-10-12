package com.asadbek.puzgridox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private Button btnContinue;
    private boolean hasSavedGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button startButton = findViewById(R.id.startButton);
        ImageView btnInfo = findViewById(R.id.btn_info);
        Button btnRecords = findViewById(R.id.btn_records);
        btnContinue = findViewById(R.id.btn_continue);
        Button btnQuit = findViewById(R.id.btn_quit);

        updateContinueState();

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("IS_CONTINUE", false);
            startActivity(intent);
        });

        btnContinue.setOnClickListener(v -> {
            if (hasSavedGame) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("IS_CONTINUE", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No saved game found!", Toast.LENGTH_SHORT).show();
            }
        });

        btnRecords.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_records, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().setDimAmount(0.85f);
            }

            SharedPreferences prefs = getSharedPreferences("PuzzlePrefs", MODE_PRIVATE);

            TextView tvTime1 = dialogView.findViewById(R.id.tv_time1);
            TextView tvMoves1 = dialogView.findViewById(R.id.tv_moves1);
            int score1 = prefs.getInt("SCORE1", 99999);
            if (score1 != 99999) {
                int totalSecs = prefs.getInt("TIME1", 0);
                tvTime1.setText(String.format(java.util.Locale.US, "%02d:%02d", totalSecs / 60, totalSecs % 60));
                tvMoves1.setText(String.valueOf(score1));
            } else {
                tvTime1.setText("00:00");
                tvMoves1.setText("0");
            }

            TextView tvTime2 = dialogView.findViewById(R.id.tv_time2);
            TextView tvMoves2 = dialogView.findViewById(R.id.tv_moves2);
            int score2 = prefs.getInt("SCORE2", 99999);
            if (score2 != 99999) {
                int totalSecs = prefs.getInt("TIME2", 0);
                tvTime2.setText(String.format(java.util.Locale.US, "%02d:%02d", totalSecs / 60, totalSecs % 60));
                tvMoves2.setText(String.valueOf(score2));
            } else {
                tvTime2.setText("00:00");
                tvMoves2.setText("0");
            }

            TextView tvTime3 = dialogView.findViewById(R.id.tv_time3);
            TextView tvMoves3 = dialogView.findViewById(R.id.tv_moves3);
            int score3 = prefs.getInt("SCORE3", 99999);
            if (score3 != 99999) {
                int totalSecs = prefs.getInt("TIME3", 0);
                tvTime3.setText(String.format(java.util.Locale.US, "%02d:%02d", totalSecs / 60, totalSecs % 60));
                tvMoves3.setText(String.valueOf(score3));
            } else {
                tvTime3.setText("00:00");
                tvMoves3.setText("0");
            }

            dialogView.findViewById(R.id.btn_close).setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });

        btnInfo.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_info, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().setDimAmount(0.85f);
            }

            dialogView.findViewById(R.id.btn_ok).setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });

        btnQuit.setOnClickListener(v -> {
            showConfirmDialog("DO YOU WANT TO QUIT THE GAME?", () -> {
                finishAffinity();
                System.exit(0);
            });
        });
    }

    private void showConfirmDialog(String message, Runnable onYes) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
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

    private void updateContinueState() {
        SharedPreferences prefs = getSharedPreferences("PuzzlePrefs", MODE_PRIVATE);
        int steps = prefs.getInt("steps", 0);
        hasSavedGame = steps > 0;
        
        btnContinue.setVisibility(View.VISIBLE);
        if (hasSavedGame) {
            btnContinue.setAlpha(1.0f);
        } else {
            btnContinue.setAlpha(0.4f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContinueState();
    }
}