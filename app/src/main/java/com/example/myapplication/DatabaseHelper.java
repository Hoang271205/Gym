package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "GymApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";

    // User Table Columns
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT NOT NULL,"
                + KEY_EMAIL + " TEXT NOT NULL UNIQUE,"
                + KEY_PASSWORD + " TEXT NOT NULL,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        Log.d("DatabaseHelper", "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

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

    // Lấy thông tin user theo email - FIXED VERSION
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
                // Sử dụng index trực tiếp thay vì getColumnIndex()
                user.setId(cursor.getInt(0));           // KEY_ID ở vị trí 0
                user.setUsername(cursor.getString(1));   // KEY_USERNAME ở vị trí 1
                user.setEmail(cursor.getString(2));      // KEY_EMAIL ở vị trí 2
                user.setPassword(cursor.getString(3));   // KEY_PASSWORD ở vị trí 3
                user.setCreatedAt(cursor.getString(4));  // KEY_CREATED_AT ở vị trí 4
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error reading user data: " + e.getMessage());
                user = null;
            }
        }

        cursor.close();
        db.close();

        return user;
    }

    // Lấy tất cả users (cho admin) - FIXED VERSION
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
                    // Sử dụng index trực tiếp
                    user.setId(cursor.getInt(0));           // KEY_ID
                    user.setUsername(cursor.getString(1));   // KEY_USERNAME
                    user.setEmail(cursor.getString(2));      // KEY_EMAIL
                    user.setPassword(cursor.getString(3));   // KEY_PASSWORD
                    user.setCreatedAt(cursor.getString(4));  // KEY_CREATED_AT

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
}