package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFavorites;
    private FavoriteWorkoutCardAdapter adapter;
    private List<WorkoutCard> favoriteWorkoutCards;
    private DatabaseHelper databaseHelper;

    private TextView tvTotalFavorites, tvTotalCalories, tvWorkoutTypes;
    private LinearLayout llEmptyState;
    private EditText etSearch;
    private String currentUserId = "user123"; // Default fallback

    // User info
    private String currentUsername = "Guest";
    private String currentEmail = "";
    private int currentUserIdInt = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d("FavoriteActivity", "🚀 Starting FavoriteActivity onCreate");

            setContentView(R.layout.activity_favorite);
            Log.d("FavoriteActivity", "✅ Layout loaded successfully");

            // Initialize database first
            databaseHelper = new DatabaseHelper(this);
            Log.d("FavoriteActivity", "✅ DatabaseHelper created");

            // Test database connection
            testDatabaseConnection();

            // Load user info from intent
            loadUserInfo();

            initViews();
            setupClickListeners();
            setupBottomNavigation();
            loadFavoriteWorkouts();

            Log.d("FavoriteActivity", "✅ FavoriteActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ FATAL ERROR in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading favorites: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Navigate back to MainActivity instead of crashing
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", currentUserIdInt);
            intent.putExtra("username", currentUsername);
            intent.putExtra("email", currentEmail);
            startActivity(intent);
            finish();
        }
    }

    // Test database connection
    private void testDatabaseConnection() {
        try {
            int workoutCount = databaseHelper.getWorkoutCount();
            Log.d("FavoriteActivity", "✅ Database test successful - " + workoutCount + " workouts found");

            // Test favorites table specifically
            databaseHelper.testFavoritesTable();

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Database connection failed: " + e.getMessage(), e);
            throw new RuntimeException("Database not accessible", e);
        }
    }

    // Load user info from intent with better error handling
    private void loadUserInfo() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                currentUserIdInt = intent.getIntExtra("user_id", -1);
                currentUsername = intent.getStringExtra("username");
                currentEmail = intent.getStringExtra("email");

                // Update currentUserId for database queries
                if (currentUserIdInt != -1) {
                    currentUserId = String.valueOf(currentUserIdInt);
                }

                Log.d("FavoriteActivity", "✅ User loaded: " + currentUsername + " (ID: " + currentUserId + ")");
            } else {
                Log.w("FavoriteActivity", "⚠️ No intent data found, using defaults");
            }

            // Validate user data
            if (currentUsername == null || currentUsername.trim().isEmpty()) {
                currentUsername = "Guest";
                Log.w("FavoriteActivity", "⚠️ Username was null/empty, set to Guest");
            }

            if (currentEmail == null) {
                currentEmail = "";
                Log.w("FavoriteActivity", "⚠️ Email was null, set to empty string");
            }

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error loading user info: " + e.getMessage(), e);
            // Set safe defaults
            currentUsername = "Guest";
            currentEmail = "";
            currentUserId = "user123";
        }
    }

    private void initViews() {
        try {
            Log.d("FavoriteActivity", "🔧 Initializing views...");

            // Header - with null checks
            ImageView btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
                Log.d("FavoriteActivity", "✅ btnBack initialized");
            } else {
                Log.w("FavoriteActivity", "⚠️ btnBack not found in layout");
            }

            // Stats - with null checks
            tvTotalFavorites = findViewById(R.id.tvTotalFavorites);
            tvTotalCalories = findViewById(R.id.tvTotalCalories);
            tvWorkoutTypes = findViewById(R.id.tvWorkoutTypes);

            if (tvTotalFavorites == null) Log.w("FavoriteActivity", "⚠️ tvTotalFavorites not found");
            if (tvTotalCalories == null) Log.w("FavoriteActivity", "⚠️ tvTotalCalories not found");
            if (tvWorkoutTypes == null) Log.w("FavoriteActivity", "⚠️ tvWorkoutTypes not found");

            // Search
            etSearch = findViewById(R.id.etSearch);
            TextView btnClearAll = findViewById(R.id.btnClearAll);

            if (etSearch == null) Log.w("FavoriteActivity", "⚠️ etSearch not found");
            if (btnClearAll == null) Log.w("FavoriteActivity", "⚠️ btnClearAll not found");

            // Empty state
            llEmptyState = findViewById(R.id.llEmptyState);
            if (llEmptyState == null) Log.w("FavoriteActivity", "⚠️ llEmptyState not found");

            // RecyclerView
            recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
            if (recyclerViewFavorites != null) {
                recyclerViewFavorites.setLayoutManager(new GridLayoutManager(this, 2));
                Log.d("FavoriteActivity", "✅ RecyclerView initialized with GridLayoutManager");
            } else {
                Log.e("FavoriteActivity", "❌ recyclerViewFavorites not found - this will cause issues");
                throw new RuntimeException("RecyclerView not found in layout");
            }

            // Search functionality - with null check
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (adapter != null) {
                            adapter.filter(s.toString());
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                Log.d("FavoriteActivity", "✅ Search functionality initialized");
            }

            // Clear all button - with null check
            if (btnClearAll != null) {
                btnClearAll.setOnClickListener(v -> showClearAllDialog());
                Log.d("FavoriteActivity", "✅ Clear all button initialized");
            }

            Log.d("FavoriteActivity", "✅ All views initialized successfully");

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error in initViews: " + e.getMessage(), e);
            throw e; // Re-throw to be caught by onCreate
        }
    }

    private void setupClickListeners() {
        try {
            View btnBrowseWorkouts = findViewById(R.id.btnBrowseWorkouts);
            if (btnBrowseWorkouts != null) {
                btnBrowseWorkouts.setOnClickListener(v -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("user_id", currentUserIdInt);
                    intent.putExtra("username", currentUsername);
                    intent.putExtra("email", currentEmail);
                    startActivity(intent);
                });
                Log.d("FavoriteActivity", "✅ Browse workouts button initialized");
            } else {
                Log.w("FavoriteActivity", "⚠️ btnBrowseWorkouts not found");
            }
        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error in setupClickListeners: " + e.getMessage(), e);
        }
    }

    // Setup bottom navigation with better error handling
    private void setupBottomNavigation() {
        try {
            Log.d("FavoriteActivity", "🔧 Setting up bottom navigation...");

            // Home navigation
            LinearLayout navHome = findViewById(R.id.navHome);
            if (navHome != null) {
                navHome.setOnClickListener(v -> {
                    try {
                        Log.d("FavoriteActivity", "Navigating to Home");
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("user_id", currentUserIdInt);
                        intent.putExtra("username", currentUsername);
                        intent.putExtra("email", currentEmail);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e("FavoriteActivity", "Error navigating to home: " + e.getMessage(), e);
                    }
                });
                Log.d("FavoriteActivity", "✅ navHome set");
            } else {
                Log.w("FavoriteActivity", "⚠️ navHome not found in layout");
            }

            // Favorite navigation (current page)
            LinearLayout navFavorite = findViewById(R.id.navFavorite);
            if (navFavorite != null) {
                navFavorite.setOnClickListener(v -> {
                    Log.d("FavoriteActivity", "Already on Favorite page");
                });
                Log.d("FavoriteActivity", "✅ navFavorite set");
            } else {
                Log.w("FavoriteActivity", "⚠️ navFavorite not found in layout");
            }

            // Profile navigation
            LinearLayout navProfile = findViewById(R.id.navProfile);
            if (navProfile != null) {
                navProfile.setOnClickListener(v -> {
                    try {
                        Log.d("FavoriteActivity", "Navigating to Profile");
                        Intent intent = new Intent(this, ProfileActivity.class);
                        intent.putExtra("user_id", currentUserIdInt);
                        intent.putExtra("username", currentUsername);
                        intent.putExtra("email", currentEmail);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("FavoriteActivity", "Error navigating to profile: " + e.getMessage(), e);
                    }
                });
                Log.d("FavoriteActivity", "✅ navProfile set");
            } else {
                Log.w("FavoriteActivity", "⚠️ navProfile not found in layout");
            }

            Log.d("FavoriteActivity", "✅ Bottom navigation setup completed");

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error in setupBottomNavigation: " + e.getMessage(), e);
        }
    }

    private void loadFavoriteWorkouts() {
        try {
            Log.d("FavoriteActivity", "🔄 Loading favorite workouts for user: " + currentUserId);

            List<Workout> favoriteWorkouts = databaseHelper.getFavoriteWorkouts(currentUserId);
            favoriteWorkoutCards = new ArrayList<>();

            Log.d("FavoriteActivity", "📊 Found " + favoriteWorkouts.size() + " favorite workouts");

            for (Workout workout : favoriteWorkouts) {
                try {
                    Bitmap bitmap = getBitmapFromDrawableName(workout.getImageName());
                    Class<?> activityClass = getActivityClassFromString(workout.getActivityClass());

                    WorkoutCard workoutCard = WorkoutCard.fromWorkout(workout, bitmap, activityClass);
                    favoriteWorkoutCards.add(workoutCard);
                } catch (Exception e) {
                    Log.e("FavoriteActivity", "Error processing workout: " + workout.getTitle(), e);
                }
            }

            // Update UI
            updateStatsUI();
            updateWorkoutList();

            Log.d("FavoriteActivity", "✅ Favorite workouts loaded successfully");

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error loading favorite workouts: " + e.getMessage(), e);

            // Initialize empty list to prevent further crashes
            favoriteWorkoutCards = new ArrayList<>();
            updateStatsUI();

            Toast.makeText(this, "Error loading favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatsUI() {
        try {
            int totalFavorites = favoriteWorkoutCards != null ? favoriteWorkoutCards.size() : 0;
            int totalCalories = 0;
            Set<String> workoutTypes = new HashSet<>();

            if (favoriteWorkoutCards != null) {
                for (WorkoutCard card : favoriteWorkoutCards) {
                    totalCalories += card.getCalories();
                    workoutTypes.add(card.getType());
                }
            }

            // Update UI with null checks
            if (tvTotalFavorites != null) {
                tvTotalFavorites.setText(String.valueOf(totalFavorites));
            }
            if (tvTotalCalories != null) {
                tvTotalCalories.setText(String.valueOf(totalCalories));
            }
            if (tvWorkoutTypes != null) {
                tvWorkoutTypes.setText(String.valueOf(workoutTypes.size()));
            }

            // Show/hide empty state
            if (llEmptyState != null && recyclerViewFavorites != null) {
                if (totalFavorites == 0) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    recyclerViewFavorites.setVisibility(View.GONE);
                } else {
                    llEmptyState.setVisibility(View.GONE);
                    recyclerViewFavorites.setVisibility(View.VISIBLE);
                }
            }

            Log.d("FavoriteActivity", "✅ Stats UI updated - " + totalFavorites + " favorites, " + totalCalories + " calories");

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error updating stats UI: " + e.getMessage(), e);
        }
    }

    private void updateWorkoutList() {
        try {
            if (favoriteWorkoutCards == null || favoriteWorkoutCards.isEmpty()) {
                Log.d("FavoriteActivity", "No favorite workouts to display");
                return;
            }

            if (recyclerViewFavorites == null) {
                Log.e("FavoriteActivity", "RecyclerView is null, cannot update workout list");
                return;
            }

            adapter = new FavoriteWorkoutCardAdapter(favoriteWorkoutCards, this::onRemoveFromFavorites);
            recyclerViewFavorites.setAdapter(adapter);

            Log.d("FavoriteActivity", "✅ Adapter set with " + favoriteWorkoutCards.size() + " items");

        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error updating workout list: " + e.getMessage(), e);
        }
    }

    private void onRemoveFromFavorites(WorkoutCard workoutCard) {
        try {
            Log.d("FavoriteActivity", "🗑️ Removing from favorites: " + workoutCard.getTitle());

            boolean removed = databaseHelper.removeFromFavorites(workoutCard.getId(), currentUserId);
            if (removed) {
                if (favoriteWorkoutCards != null) {
                    favoriteWorkoutCards.remove(workoutCard);
                }

                if (adapter != null) {
                    adapter.removeItem(workoutCard);
                }

                updateStatsUI();
                Toast.makeText(this, "💔 Removed from favorites", Toast.LENGTH_SHORT).show();
                Log.d("FavoriteActivity", "✅ Successfully removed: " + workoutCard.getTitle());
            } else {
                Toast.makeText(this, "❌ Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                Log.e("FavoriteActivity", "❌ Failed to remove: " + workoutCard.getTitle());
            }
        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error removing from favorites: " + e.getMessage(), e);
            Toast.makeText(this, "Error removing from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearAllDialog() {
        try {
            if (favoriteWorkoutCards == null || favoriteWorkoutCards.isEmpty()) {
                Toast.makeText(this, "No favorites to clear", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Clear All Favorites")
                    .setMessage("Are you sure you want to remove all workouts from your favorites?")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        try {
                            boolean cleared = databaseHelper.clearAllFavorites(currentUserId);
                            if (cleared) {
                                if (favoriteWorkoutCards != null) {
                                    favoriteWorkoutCards.clear();
                                }

                                if (adapter != null) {
                                    adapter.updateData(favoriteWorkoutCards);
                                }

                                updateStatsUI();
                                Toast.makeText(this, "🗑️ All favorites cleared", Toast.LENGTH_SHORT).show();
                                Log.d("FavoriteActivity", "✅ All favorites cleared");
                            } else {
                                Toast.makeText(this, "❌ Failed to clear favorites", Toast.LENGTH_SHORT).show();
                                Log.e("FavoriteActivity", "❌ Failed to clear all favorites");
                            }
                        } catch (Exception e) {
                            Log.e("FavoriteActivity", "Error clearing favorites: " + e.getMessage(), e);
                            Toast.makeText(this, "Error clearing favorites", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error showing clear dialog: " + e.getMessage(), e);
        }
    }

    private Bitmap getBitmapFromDrawableName(String imageName) {
        try {
            if (imageName == null || imageName.trim().isEmpty()) {
                Log.w("FavoriteActivity", "Image name is null/empty, using default");
                return BitmapFactory.decodeResource(getResources(), R.drawable.pushup_card);
            }

            int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (resourceId != 0) {
                return BitmapFactory.decodeResource(getResources(), resourceId);
            } else {
                Log.w("FavoriteActivity", "Image resource not found: " + imageName + ", using default");
            }
        } catch (Exception e) {
            Log.e("FavoriteActivity", "Error loading image: " + imageName, e);
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.pushup_card);
    }

    private Class<?> getActivityClassFromString(String activityClassName) {
        try {
            if (activityClassName == null || activityClassName.trim().isEmpty()) {
                Log.w("FavoriteActivity", "Activity class name is null/empty, using default");
                return UniversalWorkoutDetailsActivity.class;
            }

            return Class.forName("com.example.myapplication." + activityClassName);
        } catch (ClassNotFoundException e) {
            Log.e("FavoriteActivity", "Activity class not found: " + activityClassName, e);
            return UniversalWorkoutDetailsActivity.class;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadFavoriteWorkouts(); // Refresh when returning to this activity
            Log.d("FavoriteActivity", "✅ Activity resumed, favorites refreshed");
        } catch (Exception e) {
            Log.e("FavoriteActivity", "❌ Error in onResume: " + e.getMessage(), e);
        }
    }
}