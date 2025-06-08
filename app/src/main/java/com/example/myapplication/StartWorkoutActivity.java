package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList; // Ví dụ cho danh sách bài tập

public class StartWorkoutActivity extends AppCompatActivity {

    private Chronometer chronometerWorkoutTimer;
    private Button btnEndWorkout;
    private Button btnPauseResumeWorkout;
    private TextView tvCurrentExercise;
    // private TextView tvInProgress; // Có thể không cần nếu đã có thông tin bài tập
    // private ProgressBar progressLoading; // Tương tự

    private boolean isWorkoutPaused = false;
    private long timeWhenStopped = 0; // Lưu thời gian khi chronometer dừng

    // Ví dụ về danh sách bài tập và bài tập hiện tại
    // private ArrayList<String> workoutPlan;
    // private int currentExerciseIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn sử dụng đúng tên file layout mới nếu đã đổi tên
        setContentView(R.layout.start_workout);

        chronometerWorkoutTimer = findViewById(R.id.chronometerWorkoutTimer);
        btnEndWorkout = findViewById(R.id.btnEndWorkout);
        btnPauseResumeWorkout = findViewById(R.id.btnPauseResumeWorkout);
        tvCurrentExercise = findViewById(R.id.tvCurrentExercise);
        // tvInProgress = findViewById(R.id.tvInProgress);
        // progressLoading = findViewById(R.id.progressLoading);

        // Khởi tạo danh sách bài tập (ví dụ)
        // workoutPlan = new ArrayList<>();
        // workoutPlan.add("Chống đẩy - 3 hiệp");
        // workoutPlan.add("Squats - 3 hiệp");
        // workoutPlan.add("Plank - 60 giây");
        // updateCurrentExerciseDisplay(); // Hiển thị bài tập đầu tiên

        startWorkout();

        btnPauseResumeWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWorkoutPaused) {
                    resumeWorkout();
                } else {
                    pauseWorkout();
                }
            }
        });

        btnEndWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endWorkout();
            }
        });
    }

    private void startWorkout() {
        chronometerWorkoutTimer.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
        chronometerWorkoutTimer.start();
        btnPauseResumeWorkout.setText("Tạm dừng");
        isWorkoutPaused = false;
        // Cập nhật hiển thị bài tập (nếu có)
        // tvCurrentExercise.setText("Bài tập: " + workoutPlan.get(currentExerciseIndex));
        // Hoặc một thông báo chung ban đầu
        tvCurrentExercise.setText("Bài tập hiện tại: Khởi động");
    }

    private void pauseWorkout() {
        chronometerWorkoutTimer.stop();
        timeWhenStopped = SystemClock.elapsedRealtime() - chronometerWorkoutTimer.getBase();
        isWorkoutPaused = true;
        btnPauseResumeWorkout.setText("Tiếp tục");
    }

    private void resumeWorkout() {
        chronometerWorkoutTimer.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
        chronometerWorkoutTimer.start();
        isWorkoutPaused = false;
        btnPauseResumeWorkout.setText("Tạm dừng");
    }

    private void endWorkout() {
        chronometerWorkoutTimer.stop();
        // Tại đây, bạn có thể thêm logic để lưu lại thông tin buổi tập
        // Ví dụ: thời gian, danh sách bài tập đã hoàn thành,...

        Intent intent = new Intent(StartWorkoutActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Kết thúc Activity hiện tại để người dùng không quay lại được bằng nút back
    }

    // (Tùy chọn) Hàm để cập nhật hiển thị bài tập hiện tại
    // private void updateCurrentExerciseDisplay() {
    //     if (workoutPlan != null && !workoutPlan.isEmpty() && currentExerciseIndex < workoutPlan.size()) {
    //         tvCurrentExercise.setText(workoutPlan.get(currentExerciseIndex));
    //     } else {
    //         tvCurrentExercise.setText("Hoàn thành bài tập!");
    //     }
    // }

    // (Tùy chọn) Hàm để chuyển sang bài tập tiếp theo
    // private void nextExercise() {
    //     if (workoutPlan != null && currentExerciseIndex < workoutPlan.size() - 1) {
    //         currentExerciseIndex++;
    //         updateCurrentExerciseDisplay();
    //         // Reset timer cho bài tập mới hoặc làm gì đó khác
    //     } else {
    //         // Đã hoàn thành tất cả bài tập
    //         tvCurrentExercise.setText("Hoàn thành tất cả bài tập!");
    //         pauseWorkout(); // Tự động tạm dừng
    //         btnPauseResumeWorkout.setEnabled(false); // Vô hiệu hóa nút tạm dừng/tiếp tục
    //     }
    // }
}