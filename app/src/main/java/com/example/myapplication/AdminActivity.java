package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdminActivity extends AppCompatActivity {

    private TextView tvUserCount, tvCurrentDate, tvLoginTime;
    private ListView lvUsers;
    private Button btnLogout, btnRefresh;
    private DatabaseHelper databaseHelper;
    private List<User> userList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
        setupBackPressedHandler();
        loadUserData();
        updateDateTime();
    }

    private void initViews() {
        tvUserCount = findViewById(R.id.tvUserCount);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvLoginTime = findViewById(R.id.tvLoginTime);
        lvUsers = findViewById(R.id.lvUsers);
        btnLogout = findViewById(R.id.btnLogout);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupClickListeners() {
        // Nút đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmDialog();
            }
        });

        // Nút refresh
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserData();
                updateDateTime();
                Toast.makeText(AdminActivity.this, "Data refreshed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sử dụng OnBackPressedDispatcher thay vì override onBackPressed()
    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showLogoutConfirmDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout from Admin Panel?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void logout() {
        Toast.makeText(this, "Admin logged out successfully!", Toast.LENGTH_SHORT).show();

        // Chuyển về trang Login
        Intent intent = new Intent(AdminActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateDateTime() {
        // Sử dụng UTC timezone như yêu cầu
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date currentDateTime = new Date();
        String formattedDateTime = dateTimeFormat.format(currentDateTime);

        // Tách ngày và giờ
        String[] parts = formattedDateTime.split(" ");
        String currentDate = parts[0];  // yyyy-MM-dd
        String currentTime = parts[1] + " UTC";  // HH:mm:ss UTC

        tvCurrentDate.setText(currentDate);
        tvLoginTime.setText(currentTime);
    }

    private void loadUserData() {
        // Lấy tất cả users từ database
        userList = databaseHelper.getAllUsers();

        // Hiển thị số lượng users
        int userCount = databaseHelper.getUserCount();
        tvUserCount.setText(String.valueOf(userCount));

        // Tạo danh sách để hiển thị
        ArrayList<String> userDisplayList = new ArrayList<>();
        for (User user : userList) {
            String userInfo = "🔹 ID: " + user.getId() +
                    "\n👤 Username: " + user.getUsername() +
                    "\n📧 Email: " + user.getEmail() +
                    "\n📅 Created: " + user.getCreatedAt() + "\n";
            userDisplayList.add(userInfo);
        }

        // Thiết lập adapter cho ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userDisplayList);
        lvUsers.setAdapter(adapter);

        if (userList.isEmpty()) {
            userDisplayList.add("📝 No users registered yet.\n\nUsers will appear here when they register through the app.");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại activity
        loadUserData();
        updateDateTime();
    }

    // Nếu bạn vẫn muốn giữ onBackPressed() (cho tương thích cũ)
    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Thêm dòng này để tránh warning
        showLogoutConfirmDialog();
    }
}