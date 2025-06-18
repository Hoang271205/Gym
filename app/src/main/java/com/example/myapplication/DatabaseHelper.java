package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "GymApp.db";
    private static final int DATABASE_VERSION = 5; // ✅ TĂNG VERSION ĐỂ FORCE RECREATE FAVORITES TABLE

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_WORKOUTS = "workouts";
    private static final String TABLE_EXERCISE_INSTRUCTIONS = "exercise_instructions";
    private static final String TABLE_FAVORITES = "favorites";

    // User Table Columns
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CREATED_AT = "created_at";

    // Workout Table Columns
    private static final String WORKOUT_ID = "id";
    private static final String WORKOUT_TITLE = "title";
    private static final String WORKOUT_DETAILS = "details";
    private static final String WORKOUT_CALORIES = "calories";
    private static final String WORKOUT_TYPE = "type";
    private static final String WORKOUT_IMAGE_NAME = "image_name";
    private static final String WORKOUT_ACTIVITY_CLASS = "activity_class";
    private static final String WORKOUT_DURATION = "duration";
    private static final String WORKOUT_LEVEL = "level";
    private static final String WORKOUT_DESCRIPTION = "description";
    private static final String WORKOUT_CREATED_AT = "created_at";

    // Exercise Instructions Table Columns
    private static final String INSTRUCTION_ID = "id";
    private static final String INSTRUCTION_WORKOUT_ID = "workout_id";
    private static final String INSTRUCTION_STEP_NUMBER = "step_number";
    private static final String INSTRUCTION_TITLE = "title";
    private static final String INSTRUCTION_DESCRIPTION = "description";
    private static final String INSTRUCTION_IMAGE_NAME = "image_name";
    private static final String INSTRUCTION_DURATION = "duration";

    // Favorite Table Columns
    private static final String FAVORITE_ID = "id";
    private static final String FAVORITE_WORKOUT_ID = "workout_id";
    private static final String FAVORITE_USER_ID = "user_id";
    private static final String FAVORITE_ADDED_AT = "added_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT NOT NULL,"
                + KEY_EMAIL + " TEXT NOT NULL UNIQUE,"
                + KEY_PASSWORD + " TEXT NOT NULL,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Tạo bảng workouts
        String CREATE_WORKOUTS_TABLE = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WORKOUT_TITLE + " TEXT NOT NULL,"
                + WORKOUT_DETAILS + " TEXT,"
                + WORKOUT_CALORIES + " INTEGER DEFAULT 0,"
                + WORKOUT_TYPE + " TEXT,"
                + WORKOUT_IMAGE_NAME + " TEXT,"
                + WORKOUT_ACTIVITY_CLASS + " TEXT,"
                + WORKOUT_DURATION + " TEXT,"
                + WORKOUT_LEVEL + " TEXT,"
                + WORKOUT_DESCRIPTION + " TEXT,"
                + WORKOUT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Tạo bảng exercise_instructions
        String CREATE_INSTRUCTIONS_TABLE = "CREATE TABLE " + TABLE_EXERCISE_INSTRUCTIONS + "("
                + INSTRUCTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + INSTRUCTION_WORKOUT_ID + " INTEGER,"
                + INSTRUCTION_STEP_NUMBER + " INTEGER,"
                + INSTRUCTION_TITLE + " TEXT,"
                + INSTRUCTION_DESCRIPTION + " TEXT,"
                + INSTRUCTION_IMAGE_NAME + " TEXT,"
                + INSTRUCTION_DURATION + " INTEGER,"
                + "FOREIGN KEY(" + INSTRUCTION_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUTS + "(" + WORKOUT_ID + ")"
                + ")";

        // ✅ NEW: Tạo bảng favorites
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FAVORITE_WORKOUT_ID + " INTEGER,"
                + FAVORITE_USER_ID + " TEXT,"
                + FAVORITE_ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + FAVORITE_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUTS + "(" + WORKOUT_ID + "),"
                + "UNIQUE(" + FAVORITE_WORKOUT_ID + ", " + FAVORITE_USER_ID + ")"
                + ")";

        // Thực thi tạo tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_WORKOUTS_TABLE);
        db.execSQL(CREATE_INSTRUCTIONS_TABLE);
        db.execSQL(CREATE_FAVORITES_TABLE);

        insertSampleWorkouts(db);

        Log.d("DatabaseHelper", "Database tables created with version " + DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2) {
            String CREATE_WORKOUTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUTS + "("
                    + WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + WORKOUT_TITLE + " TEXT NOT NULL,"
                    + WORKOUT_DETAILS + " TEXT,"
                    + WORKOUT_CALORIES + " INTEGER DEFAULT 0,"
                    + WORKOUT_TYPE + " TEXT,"
                    + WORKOUT_IMAGE_NAME + " TEXT,"
                    + WORKOUT_ACTIVITY_CLASS + " TEXT,"
                    + WORKOUT_DURATION + " TEXT,"
                    + WORKOUT_LEVEL + " TEXT,"
                    + WORKOUT_DESCRIPTION + " TEXT,"
                    + WORKOUT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

            db.execSQL(CREATE_WORKOUTS_TABLE);
            insertSampleWorkouts(db);
            Log.d("DatabaseHelper", "Upgraded to version 2: Added workouts table");
        }

        if (oldVersion < 3) {
            String CREATE_INSTRUCTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISE_INSTRUCTIONS + "("
                    + INSTRUCTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + INSTRUCTION_WORKOUT_ID + " INTEGER,"
                    + INSTRUCTION_STEP_NUMBER + " INTEGER,"
                    + INSTRUCTION_TITLE + " TEXT,"
                    + INSTRUCTION_DESCRIPTION + " TEXT,"
                    + INSTRUCTION_IMAGE_NAME + " TEXT,"
                    + INSTRUCTION_DURATION + " INTEGER,"
                    + "FOREIGN KEY(" + INSTRUCTION_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUTS + "(" + WORKOUT_ID + ")"
                    + ")";

            db.execSQL(CREATE_INSTRUCTIONS_TABLE);
            Log.d("DatabaseHelper", "Upgraded to version 3: Added exercise instructions table");
        }

        if (oldVersion < 4) {
            String CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + "("
                    + FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FAVORITE_WORKOUT_ID + " INTEGER,"
                    + FAVORITE_USER_ID + " TEXT,"
                    + FAVORITE_ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(" + FAVORITE_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUTS + "(" + WORKOUT_ID + "),"
                    + "UNIQUE(" + FAVORITE_WORKOUT_ID + ", " + FAVORITE_USER_ID + ")"
                    + ")";

            db.execSQL(CREATE_FAVORITES_TABLE);
            Log.d("DatabaseHelper", "Upgraded to version 4: Added favorites table");
        }

        // ✅ NEW: Force recreate favorites table to ensure it works properly
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                    + FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FAVORITE_WORKOUT_ID + " INTEGER,"
                    + FAVORITE_USER_ID + " TEXT,"
                    + FAVORITE_ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(" + FAVORITE_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUTS + "(" + WORKOUT_ID + "),"
                    + "UNIQUE(" + FAVORITE_WORKOUT_ID + ", " + FAVORITE_USER_ID + ")"
                    + ")";
            db.execSQL(CREATE_FAVORITES_TABLE);
            Log.d("DatabaseHelper", "Upgraded to version 5: Favorites table recreated");
        }
    }

    // Thêm dữ liệu mẫu cho workouts
    private void insertSampleWorkouts(SQLiteDatabase db) {
        // Push-ups
        ContentValues pushup = new ContentValues();
        pushup.put(WORKOUT_TITLE, "Push-ups");
        pushup.put(WORKOUT_DETAILS, "3 sets of 15 reps");
        pushup.put(WORKOUT_CALORIES, 50);
        pushup.put(WORKOUT_TYPE, "Chest");
        pushup.put(WORKOUT_IMAGE_NAME, "pushup_card");
        pushup.put(WORKOUT_ACTIVITY_CLASS, "UniversalWorkoutDetailsActivity");
        pushup.put(WORKOUT_DURATION, "15 min");
        pushup.put(WORKOUT_LEVEL, "Beginner");
        pushup.put(WORKOUT_DESCRIPTION, "Classic push-up exercise for upper body strength");
        db.insert(TABLE_WORKOUTS, null, pushup);

        // Running
        ContentValues running = new ContentValues();
        running.put(WORKOUT_TITLE, "Running");
        running.put(WORKOUT_DETAILS, "5 km run");
        running.put(WORKOUT_CALORIES, 300);
        running.put(WORKOUT_TYPE, "Stamina");
        running.put(WORKOUT_IMAGE_NAME, "running_card");
        running.put(WORKOUT_ACTIVITY_CLASS, "UniversalWorkoutDetailsActivity");
        running.put(WORKOUT_DURATION, "30 min");
        running.put(WORKOUT_LEVEL, "Intermediate");
        running.put(WORKOUT_DESCRIPTION, "Cardiovascular exercise for endurance building");
        db.insert(TABLE_WORKOUTS, null, running);

        // Plank
        ContentValues plank = new ContentValues();
        plank.put(WORKOUT_TITLE, "Plank");
        plank.put(WORKOUT_DETAILS, "Hold for 1 minute");
        plank.put(WORKOUT_CALORIES, 30);
        plank.put(WORKOUT_TYPE, "Core");
        plank.put(WORKOUT_IMAGE_NAME, "plank_card");
        plank.put(WORKOUT_ACTIVITY_CLASS, "UniversalWorkoutDetailsActivity");
        plank.put(WORKOUT_DURATION, "10 min");
        plank.put(WORKOUT_LEVEL, "Beginner");
        plank.put(WORKOUT_DESCRIPTION, "Core strengthening exercise");
        db.insert(TABLE_WORKOUTS, null, plank);

        // Twist Exercise
        ContentValues twist = new ContentValues();
        twist.put(WORKOUT_TITLE, "Twist Exercise");
        twist.put(WORKOUT_DETAILS, "3 sets of 12 reps");
        twist.put(WORKOUT_CALORIES, 40);
        twist.put(WORKOUT_TYPE, "Bicep");
        twist.put(WORKOUT_IMAGE_NAME, "twist_card");
        twist.put(WORKOUT_ACTIVITY_CLASS, "UniversalWorkoutDetailsActivity");
        twist.put(WORKOUT_DURATION, "20 min");
        twist.put(WORKOUT_LEVEL, "Beginner");
        twist.put(WORKOUT_DESCRIPTION, "Upper body twisting movement for bicep development");
        db.insert(TABLE_WORKOUTS, null, twist);

        Log.d("DatabaseHelper", "Sample workouts inserted");
    }

    // ================== ✅ NEW: USER SESSION METHODS ==================

    // ✅ Lưu user session khi login
    public static void saveUserSession(Context context, String userId, String username, String email) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_id", userId);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putBoolean("is_logged_in", true);

        editor.apply();

        Log.d("DatabaseHelper", "✅ User session saved: " + userId);
    }

    // ✅ Lấy current user ID
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // ✅ FIX: Try to get as String first, then as Int if failed
        String userId = null;

        try {
            // Try to get as String first
            userId = prefs.getString("user_id", "");
        } catch (ClassCastException e) {
            // If failed, it means it was stored as Integer, so get as Int and convert
            try {
                int userIdInt = prefs.getInt("user_id", -1);
                if (userIdInt != -1) {
                    userId = String.valueOf(userIdInt);

                    // ✅ FIX: Update SharedPreferences to store as String for future use
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("user_id"); // Remove old Integer value
                    editor.putString("user_id", userId); // Store as String
                    editor.apply();

                    Log.d("DatabaseHelper", "🔧 Converted user_id from Integer to String: " + userId);
                }
            } catch (Exception ex) {
                Log.e("DatabaseHelper", "❌ Error getting user_id: " + ex.getMessage());
            }
        }

        // ✅ Fallback nếu không có session
        if (userId == null || userId.trim().isEmpty()) {
            Log.w("DatabaseHelper", "⚠️ No user session found - user needs to login");
            return ""; // Trả về empty thay vì hardcode
        }

        Log.d("DatabaseHelper", "✅ Current User ID: '" + userId + "'");
        return userId;
    }

    // ✅ Lấy username hiện tại
    public static String getCurrentUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getString("username", "User");
    }

    // ✅ Lấy email hiện tại
    public static String getCurrentEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getString("email", "");
    }

    // ✅ Kiểm tra user đã login chưa
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }

    // ✅ Clear session khi logout
    public static void clearUserSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Log.d("DatabaseHelper", "✅ User session cleared");
    }

    // ================== USER METHODS ==================

    // Thêm user mới
    public long addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PASSWORD, password);

        long id = db.insert(TABLE_USERS, null, values);
        db.close();

        Log.d("DatabaseHelper", "User added with ID: " + id);
        return id;
    }

    // Kiểm tra user login
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_ID};
        String selection = KEY_EMAIL + " = ?" + " AND " + KEY_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);

        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        return cursorCount > 0;
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_ID};
        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);

        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        return cursorCount > 0;
    }

    // Lấy thông tin user theo email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_ID, KEY_USERNAME, KEY_EMAIL, KEY_PASSWORD, KEY_CREATED_AT};
        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);

        User user = null;
        if (cursor.moveToFirst()) {
            try {
                user = new User();
                user.setId(cursor.getInt(0));
                user.setUsername(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPassword(cursor.getString(3));
                user.setCreatedAt(cursor.getString(4));
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error reading user data: " + e.getMessage());
                user = null;
            }
        }

        cursor.close();
        db.close();

        return user;
    }

    // Lấy tất cả users (cho admin)
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_USERNAME + ", " +
                KEY_EMAIL + ", " + KEY_PASSWORD + ", " + KEY_CREATED_AT +
                " FROM " + TABLE_USERS + " ORDER BY " + KEY_CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    User user = new User();
                    user.setId(cursor.getInt(0));
                    user.setUsername(cursor.getString(1));
                    user.setEmail(cursor.getString(2));
                    user.setPassword(cursor.getString(3));
                    user.setCreatedAt(cursor.getString(4));

                    userList.add(user);
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error reading user in list: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return userList;
    }

    // Đếm số lượng users
    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_USERS;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }
    // ✅ THÊM method này vào DatabaseHelper
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;

        try {
            String query = "SELECT * FROM users WHERE username = ?";
            cursor = db.rawQuery(query, new String[]{username});

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                // Don't return password for security

                Log.d("DatabaseHelper", "✅ Found user: " + username + " with ID: " + user.getId());
            } else {
                Log.w("DatabaseHelper", "⚠️ User not found: " + username);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Error getting user by username: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return user;
    }
    // Method helper để lấy thông tin user theo ID
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_ID, KEY_USERNAME, KEY_EMAIL, KEY_PASSWORD, KEY_CREATED_AT};
        String selection = KEY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);

        User user = null;
        if (cursor.moveToFirst()) {
            try {
                user = new User();
                user.setId(cursor.getInt(0));
                user.setUsername(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPassword(cursor.getString(3));
                user.setCreatedAt(cursor.getString(4));
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error reading user by ID: " + e.getMessage());
                user = null;
            }
        }

        cursor.close();
        db.close();

        return user;
    }

    // Method để cập nhật thông tin user
    public boolean updateUser(int userId, String username, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_EMAIL, email);

        String whereClause = KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();

        Log.d("DatabaseHelper", "User updated, rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    // Method để xóa user
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rowsDeleted = db.delete(TABLE_USERS, whereClause, whereArgs);
        db.close();

        Log.d("DatabaseHelper", "User deleted, rows affected: " + rowsDeleted);
        return rowsDeleted > 0;
    }

    // ================== WORKOUT METHODS ==================

    // Thêm workout mới
    public long addWorkout(String title, String details, int calories, String type,
                           String imageName, String activityClass, String duration, String level, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WORKOUT_TITLE, title);
        values.put(WORKOUT_DETAILS, details);
        values.put(WORKOUT_CALORIES, calories);
        values.put(WORKOUT_TYPE, type);
        values.put(WORKOUT_IMAGE_NAME, imageName);
        values.put(WORKOUT_ACTIVITY_CLASS, activityClass);
        values.put(WORKOUT_DURATION, duration);
        values.put(WORKOUT_LEVEL, level);
        values.put(WORKOUT_DESCRIPTION, description);

        long id = db.insert(TABLE_WORKOUTS, null, values);
        db.close();

        Log.d("DatabaseHelper", "Workout added with ID: " + id);
        return id;
    }

    // Lấy tất cả workouts
    public List<Workout> getAllWorkouts() {
        List<Workout> workoutList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_WORKOUTS + " ORDER BY " + WORKOUT_CREATED_AT + " DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    Workout workout = new Workout();
                    workout.setId(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_ID)));
                    workout.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TITLE)));
                    workout.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DETAILS)));
                    workout.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_CALORIES)));
                    workout.setType(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TYPE)));
                    workout.setImageName(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_IMAGE_NAME)));
                    workout.setActivityClass(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_ACTIVITY_CLASS)));
                    workout.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DURATION)));
                    workout.setLevel(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_LEVEL)));
                    workout.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DESCRIPTION)));
                    workout.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_CREATED_AT)));

                    workoutList.add(workout);
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error reading workout: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return workoutList;
    }

    // Lấy workout theo ID
    public Workout getWorkoutById(int workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = WORKOUT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(workoutId)};

        Cursor cursor = db.query(TABLE_WORKOUTS, null, selection, selectionArgs, null, null, null);

        Workout workout = null;
        if (cursor.moveToFirst()) {
            try {
                workout = new Workout();
                workout.setId(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_ID)));
                workout.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TITLE)));
                workout.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DETAILS)));
                workout.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_CALORIES)));
                workout.setType(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TYPE)));
                workout.setImageName(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_IMAGE_NAME)));
                workout.setActivityClass(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_ACTIVITY_CLASS)));
                workout.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DURATION)));
                workout.setLevel(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_LEVEL)));
                workout.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DESCRIPTION)));
                workout.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_CREATED_AT)));
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error reading workout by ID: " + e.getMessage());
            }
        }

        cursor.close();
        db.close();
        return workout;
    }

    // Tìm kiếm workout theo tên
    public List<Workout> searchWorkouts(String query) {
        List<Workout> workoutList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = WORKOUT_TITLE + " LIKE ? OR " + WORKOUT_TYPE + " LIKE ?";
        String[] selectionArgs = {"%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_WORKOUTS, null, selection, selectionArgs, null, null, WORKOUT_TITLE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                try {
                    Workout workout = new Workout();
                    workout.setId(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_ID)));
                    workout.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TITLE)));
                    workout.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DETAILS)));
                    workout.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_CALORIES)));
                    workout.setType(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TYPE)));
                    workout.setImageName(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_IMAGE_NAME)));
                    workout.setActivityClass(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_ACTIVITY_CLASS)));
                    workout.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DURATION)));
                    workout.setLevel(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_LEVEL)));
                    workout.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DESCRIPTION)));
                    workout.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_CREATED_AT)));

                    workoutList.add(workout);
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error reading workout in search: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return workoutList;
    }

    // Đếm số lượng workouts
    public int getWorkoutCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_WORKOUTS;
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // Cập nhật workout
    public boolean updateWorkout(int workoutId, String title, String details, int calories, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WORKOUT_TITLE, title);
        values.put(WORKOUT_DETAILS, details);
        values.put(WORKOUT_CALORIES, calories);
        values.put(WORKOUT_TYPE, type);

        String whereClause = WORKOUT_ID + " = ?";
        String[] whereArgs = {String.valueOf(workoutId)};

        int rowsAffected = db.update(TABLE_WORKOUTS, values, whereClause, whereArgs);
        db.close();

        Log.d("DatabaseHelper", "Workout updated, rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    // Xóa workout
    public boolean deleteWorkout(int workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Xóa instructions trước
            db.delete(TABLE_EXERCISE_INSTRUCTIONS, INSTRUCTION_WORKOUT_ID + " = ?",
                    new String[]{String.valueOf(workoutId)});

            // Xóa favorites liên quan
            db.delete(TABLE_FAVORITES, FAVORITE_WORKOUT_ID + " = ?",
                    new String[]{String.valueOf(workoutId)});

            // Xóa workout
            String whereClause = WORKOUT_ID + " = ?";
            String[] whereArgs = {String.valueOf(workoutId)};

            int rowsDeleted = db.delete(TABLE_WORKOUTS, whereClause, whereArgs);
            Log.d("DatabaseHelper", "Workout, instructions and favorites deleted, rows affected: " + rowsDeleted);
            return rowsDeleted > 0;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting workout: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Debug method để kiểm tra workout instructions
    public void debugWorkoutInstructions(int workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_EXERCISE_INSTRUCTIONS +
                " WHERE " + INSTRUCTION_WORKOUT_ID + " = ? " +
                " ORDER BY " + INSTRUCTION_STEP_NUMBER;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(workoutId)});

        Log.d("DatabaseHelper", "=== DEBUG INSTRUCTIONS FOR WORKOUT " + workoutId + " ===");
        Log.d("DatabaseHelper", "SQL Query: " + query);
        Log.d("DatabaseHelper", "Total rows found: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(INSTRUCTION_ID));
                    int dbWorkoutId = cursor.getInt(cursor.getColumnIndexOrThrow(INSTRUCTION_WORKOUT_ID));
                    int stepNumber = cursor.getInt(cursor.getColumnIndexOrThrow(INSTRUCTION_STEP_NUMBER));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(INSTRUCTION_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(INSTRUCTION_DESCRIPTION));
                    String imageName = cursor.getString(cursor.getColumnIndexOrThrow(INSTRUCTION_IMAGE_NAME));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(INSTRUCTION_DURATION));

                    Log.d("DatabaseHelper", "Row " + stepNumber + ": ID=" + id + ", WorkoutID=" + dbWorkoutId + ", Title='" + title + "', Desc='" + description + "', Duration=" + duration);
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error reading instruction row: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "❌ NO INSTRUCTIONS FOUND for workout ID " + workoutId);
        }

        cursor.close();
        db.close();
    }

    // ================== EXERCISE INSTRUCTIONS METHODS ==================

    // Thêm exercise instruction
    public long addExerciseInstruction(int workoutId, int stepNumber, String title,
                                       String description, String imageName, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(INSTRUCTION_WORKOUT_ID, workoutId);
        values.put(INSTRUCTION_STEP_NUMBER, stepNumber);
        values.put(INSTRUCTION_TITLE, title);
        values.put(INSTRUCTION_DESCRIPTION, description);
        values.put(INSTRUCTION_IMAGE_NAME, imageName);
        values.put(INSTRUCTION_DURATION, duration);

        long id = db.insert(TABLE_EXERCISE_INSTRUCTIONS, null, values);
        db.close();

        Log.d("DatabaseHelper", "Exercise instruction added with ID: " + id);
        return id;
    }

    // Lấy exercise instructions cho một workout
    public List<ExerciseInstruction> getExerciseInstructions(int workoutId) {
        List<ExerciseInstruction> instructions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_EXERCISE_INSTRUCTIONS +
                " WHERE " + INSTRUCTION_WORKOUT_ID + " = ? " +
                " ORDER BY " + INSTRUCTION_STEP_NUMBER;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(workoutId)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(INSTRUCTION_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(INSTRUCTION_DESCRIPTION));
                    String imageName = cursor.getString(cursor.getColumnIndexOrThrow(INSTRUCTION_IMAGE_NAME));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(INSTRUCTION_DURATION));

                    instructions.add(new ExerciseInstruction(title, description, imageName, duration));
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error reading exercise instruction: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return instructions;
    }

    // Xóa tất cả exercise instructions của một workout
    public void deleteExerciseInstructions(int workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISE_INSTRUCTIONS, INSTRUCTION_WORKOUT_ID + " = ?",
                new String[]{String.valueOf(workoutId)});
        db.close();
        Log.d("DatabaseHelper", "Exercise instructions deleted for workout ID: " + workoutId);
    }

    // Cập nhật exercise instruction
    public boolean updateExerciseInstruction(int instructionId, String title, String description,
                                             String imageName, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(INSTRUCTION_TITLE, title);
        values.put(INSTRUCTION_DESCRIPTION, description);
        values.put(INSTRUCTION_IMAGE_NAME, imageName);
        values.put(INSTRUCTION_DURATION, duration);

        String whereClause = INSTRUCTION_ID + " = ?";
        String[] whereArgs = {String.valueOf(instructionId)};

        int rowsAffected = db.update(TABLE_EXERCISE_INSTRUCTIONS, values, whereClause, whereArgs);
        db.close();

        Log.d("DatabaseHelper", "Exercise instruction updated, rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    // Đếm số lượng instructions cho một workout
    public int getInstructionCount(int workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_EXERCISE_INSTRUCTIONS +
                " WHERE " + INSTRUCTION_WORKOUT_ID + " = ?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(workoutId)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // ================== ✅ IMPROVED: FAVORITE METHODS ==================

    // Thêm workout vào favorites
    public boolean addToFavorites(int workoutId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("DatabaseHelper", "=== ADD TO FAVORITES DEBUG ===");
        Log.d("DatabaseHelper", "Workout ID: " + workoutId);
        Log.d("DatabaseHelper", "User ID: '" + userId + "'");

        try {
            ContentValues values = new ContentValues();
            values.put(FAVORITE_WORKOUT_ID, workoutId);
            values.put(FAVORITE_USER_ID, userId);

            long id = db.insert(TABLE_FAVORITES, null, values);
            Log.d("DatabaseHelper", "✅ Added to favorites with ID: " + id);
            return id != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Error adding to favorites: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Xóa workout khỏi favorites
    public boolean removeFromFavorites(int workoutId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("DatabaseHelper", "=== REMOVE FROM FAVORITES DEBUG ===");
        Log.d("DatabaseHelper", "Workout ID: " + workoutId);
        Log.d("DatabaseHelper", "User ID: '" + userId + "'");

        try {
            String whereClause = FAVORITE_WORKOUT_ID + " = ? AND " + FAVORITE_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(workoutId), userId};

            int rowsDeleted = db.delete(TABLE_FAVORITES, whereClause, whereArgs);
            Log.d("DatabaseHelper", "✅ Removed from favorites, rows affected: " + rowsDeleted);
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Error removing from favorites: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Kiểm tra workout có trong favorites không
    public boolean isFavorite(int workoutId, String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String[] columns = {FAVORITE_ID};
            String selection = FAVORITE_WORKOUT_ID + " = ? AND " + FAVORITE_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(workoutId), userId};

            Cursor cursor = db.query(TABLE_FAVORITES, columns, selection, selectionArgs, null, null, null);

            int count = cursor.getCount();
            cursor.close();

            Log.d("DatabaseHelper", "isFavorite check - WorkoutID: " + workoutId + ", UserID: '" + userId + "', Result: " + (count > 0));
            return count > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking favorite: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Toggle favorite status
    public boolean toggleFavorite(int workoutId, String userId) {
        Log.d("DatabaseHelper", "=== TOGGLE FAVORITE ===");
        Log.d("DatabaseHelper", "Workout ID: " + workoutId + ", User ID: '" + userId + "'");

        if (isFavorite(workoutId, userId)) {
            Log.d("DatabaseHelper", "Currently favorite - removing...");
            return removeFromFavorites(workoutId, userId);
        } else {
            Log.d("DatabaseHelper", "Not favorite - adding...");
            return addToFavorites(workoutId, userId);
        }
    }

    // Lấy tất cả workouts yêu thích của user
    public List<Workout> getFavoriteWorkouts(String userId) {
        List<Workout> favoriteWorkouts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT w.* FROM " + TABLE_WORKOUTS + " w " +
                    "INNER JOIN " + TABLE_FAVORITES + " f ON w." + WORKOUT_ID + " = f." + FAVORITE_WORKOUT_ID + " " +
                    "WHERE f." + FAVORITE_USER_ID + " = ? " +
                    "ORDER BY f." + FAVORITE_ADDED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{userId});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        Workout workout = new Workout();
                        workout.setId(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_ID)));
                        workout.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TITLE)));
                        workout.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DETAILS)));
                        workout.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_CALORIES)));
                        workout.setType(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_TYPE)));
                        workout.setImageName(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_IMAGE_NAME)));
                        workout.setActivityClass(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_ACTIVITY_CLASS)));
                        workout.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DURATION)));
                        workout.setLevel(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_LEVEL)));
                        workout.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DESCRIPTION)));
                        workout.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_CREATED_AT)));

                        favoriteWorkouts.add(workout);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error reading favorite workout: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting favorite workouts: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return favoriteWorkouts;
    }

    // Test favorites table functionality
    public void testFavoritesTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            // Test if favorites table exists
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='favorites'", null);
            if (cursor.getCount() > 0) {
                Log.d("DatabaseHelper", "✅ Favorites table exists");

                // Test basic query
                Cursor testCursor = db.rawQuery("SELECT COUNT(*) FROM favorites", null);
                if (testCursor.moveToFirst()) {
                    int count = testCursor.getInt(0);
                    Log.d("DatabaseHelper", "✅ Favorites table accessible - " + count + " records");
                }
                testCursor.close();

            } else {
                Log.e("DatabaseHelper", "❌ Favorites table NOT found - creating it now");
                // Force create the table
                onUpgrade(db, 4, 5);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "❌ Database test error: " + e.getMessage(), e);
            throw e;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Xóa tất cả favorites của user
    public boolean clearAllFavorites(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String whereClause = FAVORITE_USER_ID + " = ?";
            String[] whereArgs = {userId};

            int rowsDeleted = db.delete(TABLE_FAVORITES, whereClause, whereArgs);
            Log.d("DatabaseHelper", "Cleared all favorites, rows affected: " + rowsDeleted);
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing favorites: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Đếm số favorites của user
    public int getFavoriteCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String countQuery = "SELECT COUNT(*) FROM " + TABLE_FAVORITES + " WHERE " + FAVORITE_USER_ID + " = ?";
            Cursor cursor = db.rawQuery(countQuery, new String[]{userId});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            cursor.close();
            return count;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error counting favorites: " + e.getMessage());
            return 0;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Lấy các favorite workout IDs của user (hữu ích cho UI)
    public List<Integer> getFavoriteWorkoutIds(String userId) {
        List<Integer> favoriteIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT " + FAVORITE_WORKOUT_ID + " FROM " + TABLE_FAVORITES +
                    " WHERE " + FAVORITE_USER_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{userId});

            if (cursor.moveToFirst()) {
                do {
                    favoriteIds.add(cursor.getInt(0));
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting favorite workout IDs: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return favoriteIds;
    }

    // Lấy thống kê favorites cho user với details
    public FavoriteStats getFavoriteStatsDetailed(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT COUNT(*) as total_favorites, " +
                    "COALESCE(SUM(w." + WORKOUT_CALORIES + "), 0) as total_calories, " +
                    "COUNT(DISTINCT w." + WORKOUT_TYPE + ") as unique_types " +
                    "FROM " + TABLE_FAVORITES + " f " +
                    "INNER JOIN " + TABLE_WORKOUTS + " w ON f." + FAVORITE_WORKOUT_ID + " = w." + WORKOUT_ID + " " +
                    "WHERE f." + FAVORITE_USER_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{userId});

            FavoriteStats stats = new FavoriteStats();
            if (cursor.moveToFirst()) {
                stats.totalFavorites = cursor.getInt(0);
                stats.totalCalories = cursor.getInt(1);
                stats.uniqueTypes = cursor.getInt(2);
            }

            cursor.close();
            return stats;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting favorite stats: " + e.getMessage());
            return new FavoriteStats(); // Return empty stats
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Lấy thống kê favorites cho user (string format)
    public String getFavoriteStats(String userId) {
        FavoriteStats stats = getFavoriteStatsDetailed(userId);
        return stats.totalFavorites + " favorites, " + stats.totalCalories + " calories, " + stats.uniqueTypes + " types";
    }

    // ✅ NEW: Helper class cho favorite stats
    public static class FavoriteStats {
        public int totalFavorites = 0;
        public int totalCalories = 0;
        public int uniqueTypes = 0;
    }
}