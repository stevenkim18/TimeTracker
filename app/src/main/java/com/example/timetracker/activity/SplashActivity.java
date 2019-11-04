package com.example.timetracker.activity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.timetracker.R;

public class SplashActivity extends AppCompatActivity {

    LottieAnimationView loadingAnimationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        loadingAnimationView = findViewById(R.id.loading_lottie);

        loadingAnimationView.setRepeatCount(2);
        loadingAnimationView.playAnimation();

        loadingAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                // 스플래쉬 액티비티 종료
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                // 스플래쉬 액티비티 종료
//                finish();
//            }
//        }, 3000);

    }

    @Override
    public void onBackPressed() {
        // 스플래쉬 화면에서는 백 버튼을 작동 안하게 하기 위해서 onBackPressed 메소드 오버라이드
    }
}
