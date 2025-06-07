package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingImageAdapter adapter;
    private Button btnLetsGo;

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomepage);

        viewPager = findViewById(R.id.onboarding_view_pager);
        btnLetsGo = findViewById(R.id.btn_lets_go);

        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.onboarding_image_1);
        imageList.add(R.drawable.onboarding_image_2);
        imageList.add(R.drawable.onboarding_image_3);

        adapter = new OnboardingImageAdapter(this, imageList);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(new SliderTransformer());

        setupAutoSlide();

        btnLetsGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OnboardingActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
            }
        });
    }

    private void setupAutoSlide() {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == adapter.getItemCount() - 1) {
                    currentPage = 0; // Quay lại ảnh đầu tiên
                } else {
                    currentPage++;
                }
                viewPager.setCurrentItem(currentPage, true);
                sliderHandler.postDelayed(this, 2000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 2000);
        }
    }
}