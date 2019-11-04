package com.example.timetracker.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.timetracker.BottomNavigationHelper;
import com.example.timetracker.R;
import com.example.timetracker.DTO.TaskDTO;
import com.example.timetracker.service.TimerService;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdView;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    public final static String TAG = "메인 액티비티";

    BottomNavigationView bottomNavigationView;

    //툴바
    Toolbar toolbar;
    LottieAnimationView animationViewSearch, animationViewTimer;
    EditText editTextTaskName;
    TextView textViewInfo;
    TextView textViewCategory;
    TextView textViewTaskName;
    TextView textView0, textView60;
    PercentageChartView pieView;
    SeekBar seekBar;
    Button buttonReset;
    ImageButton imageButtonPlay;

    // 할일 객체을 담을 리스트
    ArrayList<TaskDTO> tasks = new ArrayList<>();

    // 할일을 기록할 객체 변수
    TaskDTO task;

    // 카운트 다운 타이머 객체 변수
    CountDownTimer countDownTimer;

    // 현재 흐르고 있는 시간을 담기 위한 변수
    int currentTime;
    // 사용자가 설정한 시간(reset 했을때 원래 숫자로 돌아가기 위해 저장)
    int setTime;

    // 타이머가 실행되고 있는지 여부
    Boolean isTimerStart = false;

    // 진동 객체
    Vibrator vibrator;

    // 소리 객체
    Ringtone ringtone;

    //쉐어드 프리퍼런스
    SharedPreferences preferences;

    //시간 체크 여부
    Boolean isTimerUnitMinute = true;
    Boolean timerClockWise = true;
    int timerColor = Color.parseColor("#D81B60");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(TAG, "onCreate");

        toolbar = findViewById(R.id.toolbar);
        //animationViewPencilLeft = findViewById(R.id.pencilLottieLeft);
        animationViewSearch = findViewById(R.id.searchLottie);
        animationViewTimer = findViewById(R.id.LottieTimer);
        textViewInfo = findViewById(R.id.textViewInfo);
        textViewCategory = findViewById(R.id.textViewCategory);
        editTextTaskName = findViewById(R.id.editTextTaskName);
        textViewTaskName = findViewById(R.id.textViewTaskName);
        textView0 = findViewById(R.id.textView0);
        textView60 = findViewById(R.id.textView60);
        pieView = findViewById(R.id.pieView);
        seekBar = findViewById(R.id.seekBar);
        buttonReset = findViewById(R.id.buttonReset);
        imageButtonPlay = findViewById(R.id.imageButtonPlay);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // 쉐어드 객체 생성
        preferences = getSharedPreferences("task", MODE_PRIVATE);

        // 에드몹 초기화
        MobileAds.initialize(this, "ca-app-pub-3868646427853915~7825878341");

        setToolbar();

        setLottie();
        // 타이머 이미지 숨기기
        setLottieVisible(animationViewTimer, false);

        //할 일 내용을 보여주는 textView 숨김
        textViewTaskName.setVisibility(View.INVISIBLE);

        setBottomNavigationView();
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        setTimer(45);

        setSeekBar();

        setImageButtonPlay();

        setButtonReset();

        setAnimationSearch();

        // 초기화 버튼 안보이게 하기
        buttonReset.setVisibility(View.INVISIBLE);



        // 사용자가 설정한 값으로 타이머 설정하기
        settingByUserSetting(preferences.getBoolean("settingTimerUnitMinute", true),                    // 시간 단위        기본 값 = 분
                             preferences.getBoolean("settingTimerClockWise", true),                     // 타이머 방향      기본 값 = 시계 방향
                             preferences.getInt("SettingTimerColor", Color.parseColor("#D81B60")));   // 타이머 색상      기본 값 = Accent 색상

    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        super.onStart();
//        // 서비스 종료
//        Intent stopServiceIntent = new Intent(this, TimerService.class);
//        stopService(stopServiceIntent);
//
//        Log.v(TAG, "인텐트로 받은 값 = " + getIntent().getBooleanExtra("fromService", false));
//
//        if(getIntent().getBooleanExtra("fromService", false)){
//
//            editTextTaskName.setText(getIntent().getStringExtra("taskName"));
//            Log.v(TAG, getIntent().getStringExtra("taskName"));
//
//            currentTime = getIntent().getIntExtra("currentTime", 0);
//            Log.v(TAG, "서비스에서 받아온 시간 = " + getIntent().getIntExtra("currentTime", 0));
//
//            // Task 객체 생성
//            task = new TaskDTO();
//            // 버튼을 누른 시간을 가져와서 시작 시간에 저장
//            task.setStartTime(getIntent().getStringExtra("startTime"));
//            Log.v(TAG, getIntent().getStringExtra("startTime"));
//
//            // 타이머 시작
//            //시작 여부를 true로 바꿈
//            isTimerStart = true;
//            //버튼 이미지를 일시정지 버튼으로 바꿈
//            imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause_button, null));
//            //seekbar 안보이게 함.
//            seekBar.setVisibility(View.INVISIBLE);
//            //seekbar 아래 텍스트 안보이게 함.
//            textView0.setVisibility(View.INVISIBLE);
//            textView60.setVisibility(View.INVISIBLE);
//            //초기화 버튼 보이게 함.
//            buttonReset.setVisibility(View.VISIBLE);
//            // 타이머 이미지 보이기
//            setLottieVisible(animationViewTimer, true);
//            // 시계 이미지 숨기기
//            //setLottieVisible(animationViewPencilLeft, false);
//            setLottieVisible(animationViewSearch, false);
//            //"지금 당신이 집중하고자 하는 일을 적어주세요" 숨기기
//            textViewInfo.setVisibility(View.INVISIBLE);
//            // 할일 내용 적는 EditText 비활성화
//            setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, false);
//
//
//
//            // 카운트 타이머 객체 생성
//            countDownTimer = new CountDownTimer(currentTime*1000, 1000) { // (타이머 설정 시간, 간격(1000ms --> 1초)
//                // 시간이 흐를 때 실행 되는 함수
//                @Override
//                public void onTick(long millisUntilFinished) { //(현재 시간을 ms 단위로 나타니는 매개변수)
//
//                    //현재 타이머에서 흐르고 있는 시간을 현재시간 변수에 넣음.
//                    //일시정지 되었을 때 일시정지된 시간 부터 흐르게 하기 위해서.
//                    currentTime = (int)(millisUntilFinished/1000);
//
//                    // 시간이 흐를 때 원형 타이머 가운데 텍스트도 시간이 줄어들게 함.
//                    pieView.setTextFormatter(new ProgressTextFormatter() {
//                        @Override
//                        public CharSequence provideFormattedText(float v) {
//                            // 초가 2자리수 일때
//                            if ((currentTime - ((currentTime / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
//                                return ((currentTime / 60) + "분 " + (currentTime - ((currentTime / 60) * 60)) + "초");
//                            }
//                            // 초가 1자리수 일때
//                            else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
//                                return ((currentTime / 60) + "분 0" + (currentTime - ((currentTime / 60) * 60)) + "초");
//                            }
//
//                        }
//                    });
//
//                    // 원형 파이도 시간에 줄어듬에 따라 줄어듬.
//                    pieView.setProgress(currentTime * 5.0f/3.0f, true);
//
//                }
//                // 타이머가 끝났을 때 실행되는 함수
//                @Override
//                public void onFinish() {
//                    //시작 여부를 false로 바꿈
//                    isTimerStart = false;
//                    //버튼 이미지를 시작 버튼으로 바꿈
//                    imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play_button, null));
//                    //seekbar 다시 보이게 함
//                    seekBar.setVisibility(View.VISIBLE);
//                    //seekbar아래 텍스트 다시 보이게 함.
//                    textView0.setVisibility(View.VISIBLE);
//                    textView60.setVisibility(View.VISIBLE);
//                    //초기화 버튼 안 보이게 함.
//                    buttonReset.setVisibility(View.INVISIBLE);
//                    // 연필 이미지 보이게 함.
//                    //setLottieVisible(animationViewPencilLeft,true);
//                    setLottieVisible(animationViewSearch, true);
//                    // 모래시계 이미 숨김
//                    setLottieVisible(animationViewTimer, false);
//                    // 할일 적는 EditText 활성화
//                    setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, true);
//                    //"지금 당신이 집중하고자 하는 일을 적어주세요" 보이기
//                    textViewInfo.setVisibility(View.VISIBLE);
//                    // 원형 타이머 안에 텍스트 사용자가 마지막으로 설정된 시간으로 보여주기
//                    setPieViewText(setTime);
//                    // pieVIew 사용자가 마지막으로 설정한 시간으로 맞추기
//                    pieView.setProgress((float)setTime * 5.0f/ 3.0f, true);
//
//                    // 나머지 정보들 TaskDTO 객체 저장
//                    saveDataIntoTaskDTO(task);
//
//                    // 데이터 쉐어드에 저장
//                    saveDataIntoSharedPreference();
//
//
//                }
//            }.start(); // 타이머 시작
//
//        }

    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent호출");
        Log.v(TAG, "이전 인텐트 = " + getIntent().getIntExtra("currentTime", 0));
        setIntent(intent);
        Log.v(TAG,"새로운 인텐트 = " + getIntent().getIntExtra("currentTime", 0));

    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        // 서비스 종료
        Intent stopServiceIntent = new Intent(this, TimerService.class);
        stopService(stopServiceIntent);

        // 사용자가 노피티케이션에서 "초기화" 버튼을 눌렀을 때 초기화 다이얼로그를 보여줌.
        if(getIntent().getBooleanExtra("showResetDialog", false)){
            showDialog();
        }

        // 노피티케이션 서비스에서 클릭을 했을 때 타이머가 클릭한 시점의 시간 부터 동작 됨.
        if(getIntent().getBooleanExtra("fromService", false)){

            currentTime = getIntent().getIntExtra("currentTime", 0);
            Log.v(TAG, "서비스에서 받아온 시간 = " + getIntent().getIntExtra("currentTime", 0));

            editTextTaskName.setText(getIntent().getStringExtra("taskName"));
            Log.v(TAG, "서비스에서 받아온 한 일 내용 = " + getIntent().getStringExtra("taskName"));

            // Task 객체 생성
            task = new TaskDTO();
            // 버튼을 누른 시간을 가져와서 시작 시간에 저장
            task.setStartTime(getIntent().getStringExtra("startTime"));
            Log.v(TAG, "서비스에서 받아온 시작 시간 = " + getIntent().getStringExtra("startTime"));

            // 타이머 시작
            //시작 여부를 true로 바꿈
            isTimerStart = true;
            //버튼 이미지를 일시정지 버튼으로 바꿈
            imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause_button, null));
            //seekbar 안보이게 함.
            seekBar.setVisibility(View.INVISIBLE);
            //seekbar 아래 텍스트 안보이게 함.
            textView0.setVisibility(View.INVISIBLE);
            textView60.setVisibility(View.INVISIBLE);
            //초기화 버튼 보이게 함.
            buttonReset.setVisibility(View.VISIBLE);
            // 타이머 이미지 보이기
            setLottieVisible(animationViewTimer, true);
            // 시계 이미지 숨기기
            //setLottieVisible(animationViewPencilLeft, false);
            setLottieVisible(animationViewSearch, false);
            //"지금 당신이 집중하고자 하는 일을 적어주세요" 숨기기
            textViewInfo.setVisibility(View.INVISIBLE);
            // 할일 내용 적는 EditText 비활성화
            setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, false);

//            // 시간 단위가 분 일때
//            if(preferences.getBoolean("settingTimerUnitMinute", true)){
//                currentTime *= 60;
//            }

            // 카운트 타이머 객체 생성
            countDownTimer = new CountDownTimer(currentTime*1000, 1000) { // (타이머 설정 시간, 간격(1000ms --> 1초)
                // 시간이 흐를 때 실행 되는 함수
                @Override
                public void onTick(long millisUntilFinished) { //(현재 시간을 ms 단위로 나타니는 매개변수)

                    //현재 타이머에서 흐르고 있는 시간을 현재시간 변수에 넣음.
                    //일시정지 되었을 때 일시정지된 시간 부터 흐르게 하기 위해서.
                    currentTime = (int)(millisUntilFinished/1000);

                    // 시간이 흐를 때 원형 타이머 가운데 텍스트도 시간이 줄어들게 함.
                    pieView.setTextFormatter(new ProgressTextFormatter() {
                        @Override
                        public CharSequence provideFormattedText(float v) {
                            // 초가 2자리수 일때
                            if ((currentTime - ((currentTime / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
                                return ((currentTime / 60) + "분 " + (currentTime - ((currentTime / 60) * 60)) + "초");
                            }
                            // 초가 1자리수 일때
                            else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                                return ((currentTime / 60) + "분 0" + (currentTime - ((currentTime / 60) * 60)) + "초");
                            }

                        }
                    });

                    // 원형 파이도 시간에 줄어듬에 따라 줄어듬.
                    // 분
                    if(preferences.getBoolean("settingTimerUnitMinute", true)){
                        Log.v(TAG, "타이머 실행 중 progress = " +  ((float)currentTime / 3600f) * 100f);
                        pieView.setProgress(((float)currentTime / 3600f) * 100f, true);
                    }
                    //초
                    else {
                        Log.v(TAG, "타이머 실행 중 progress = " +  (float)currentTime * 5.0f/ 3.0f);
                        pieView.setProgress((float)currentTime * 5.0f/ 3.0f, true);
                    }

                }
                // 타이머가 끝났을 때 실행되는 함수
                @Override
                public void onFinish() {
                    //시작 여부를 false로 바꿈
                    isTimerStart = false;
                    //버튼 이미지를 시작 버튼으로 바꿈
                    imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play_button, null));
                    //seekbar 다시 보이게 함
                    seekBar.setVisibility(View.VISIBLE);
                    //seekbar아래 텍스트 다시 보이게 함.
                    textView0.setVisibility(View.VISIBLE);
                    textView60.setVisibility(View.VISIBLE);
                    //초기화 버튼 안 보이게 함.
                    buttonReset.setVisibility(View.INVISIBLE);
                    // 연필 이미지 보이게 함.
                    //setLottieVisible(animationViewPencilLeft,true);
                    setLottieVisible(animationViewSearch, true);
                    // 모래시계 이미 숨김
                    setLottieVisible(animationViewTimer, false);
                    // 할일 적는 EditText 활성화
                    setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, true);
                    //"지금 당신이 집중하고자 하는 일을 적어주세요" 보이기
                    textViewInfo.setVisibility(View.VISIBLE);
                    // 원형 타이머 안에 텍스트 사용자가 마지막으로 설정된 시간으로 보여주기
                    setPieViewText(setTime);
                    // pieVIew 사용자가 마지막으로 설정한 시간으로 맞추기
                    pieView.setProgress((float)setTime * 5.0f/ 3.0f, true);

                    // 나머지 정보들 TaskDTO 객체 저장
                    saveDataIntoTaskDTO(task);

                    // 데이터 쉐어드에 저장
                    saveDataIntoSharedPreference();

                    //진동 울림
                    playVibrator(preferences.getBoolean("settingVibrate", true));

                    //알림음 울리기
                    playNotificationSound(preferences.getBoolean("settingSound", true));

                    // 종료 다이얼로그
                    showFinishDialog();


                }
            }.start(); // 타이머 시작
        }

    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();

        // 타이머가 돌고 있을 때
        if(isTimerStart){
            // 타이머 종료
            countDownTimer.cancel();

            // 노티피케이션 서비스 시작
            Intent notificationIntent = new Intent(this, TimerService.class);
            notificationIntent.putExtra("taskName", editTextTaskName.getText().toString());     // 한 일 내용
            notificationIntent.putExtra("currentTime", currentTime);                            // 시간
            Log.v(TAG, "서비스로 넘어가는 시간 = " + currentTime + "");
            notificationIntent.putExtra("startTime", task.getStartTime());                      // 시작시간
            startService(notificationIntent);
        }

    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }

    //BottomNavigationView 설정하기
    private void setBottomNavigationView() {

        //BottomNavigationView shiftMode를 비활성화 시키기
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_timer); // 3번째 타이머 버튼이 활성화 되어 있게 함.

        // BottomNavigationView 아이템 클릭 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    // 그래프
                    case R.id.nav_graph :
                        startActivity(new Intent(MainActivity.this, GraphActivity.class));
                        finish();
                        break;
                    // 목록
                    case R.id.nav_list :
                        startActivity(new Intent(MainActivity.this, ListActivity.class));
                        finish();
                        break;
                    // 타이머
                    case R.id.nav_timer :

                        break;
                    // 캘린더
                    case R.id.nav_calender :
                        startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                        finish();
                        break;
                    // 설정
                    case R.id.nav_setting :
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        finish();
                        break;
                }
                // false 를 하면 BottomNavigationView 버튼이 고정이 되서 움직이지 않음.
                // true 를 해야 버튼이 눌렸을 때 효과 발동
                return true;
            }
        });
    }

    // 툴바 설정하기
    private void setToolbar(){
        setSupportActionBar(toolbar);
    }

    // 툴바와 메뉴 합치기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_timer, menu);
        return true;
    }

    // 툴바 메뉴 기능 추가
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // 카테고리
            case R.id.menu_category :
                Intent categoryIntent = new Intent(MainActivity.this, CategoryActivity.class);
                startActivityForResult(categoryIntent, 101);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Timer 설정 하기
    // num 값으로 seekbar, pieview 설정
    private void setTimer(int num){
        //
        setPieViewText(num);
        seekBar.setProgress(num);
        pieView.setProgress((float)num * 5.0f/ 3.0f, true);
        setTime = num;
        currentTime = num;
    }

    // seekBar 설정
    private void setSeekBar(){

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            //seekbar가 움직일때 실행되는 함수
            //seekbar의 최대수는 60으로 설정(0~60분까지)
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //0~60분을 0~100%로 표현해야함.
                // 분 ---> %로 하려면 분에 5/3을 곱해야 함
                pieView.setProgress((float)progress * 5.0f/ 3.0f, true);

                //사용자가 설정한 시간을 현재시간 변수에 담음
                setTime = progress;
                currentTime = progress;
                Log.v(TAG, "currentTIme = " + currentTime +"초");
                Log.v(TAG, "setTime = " + setTime);
                Log.v(TAG, "pieView = " + pieView.getProgress());

                // 원형 타이머 가운데 글짜에 사용자가 설정한 시간이 나오게 함,
                pieView.setTextFormatter(new ProgressTextFormatter() {
                    @Override
                    public CharSequence provideFormattedText(float v) {

                        //분
                        if(preferences.getBoolean("settingTimerUnitMinute", true)){
                            return progress + "분 0초";
                        }
                        //초
                        else{
                            return "0분 " + progress + "초";
                        }

                    }
                });

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

    }

    // imageButtonPlay 설정
    private void setImageButtonPlay(){

        // imageButtonPlay 눌렀을 때
        imageButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //사용자가 설정한 시간이 0분 일때는 실행이 안되게 함.
                if(setTime == 0){
                    Toast.makeText(getApplicationContext(), "시간을 설정해주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //할 일 내용이 비어 있을 떄 실행이 안되게 함.
                if(editTextTaskName.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "할 일을 적어주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 정지 상태 일때
                // 타이머가 시작이 되어야 함.
                if(isTimerStart == false){
                    //시작 여부를 true로 바꿈
                    isTimerStart = true;
                    //버튼 이미지를 일시정지 버튼으로 바꿈
                    imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause_button, null));
                    //seekbar 안보이게 함.
                    seekBar.setVisibility(View.INVISIBLE);
                    //seekbar 아래 텍스트 안보이게 함.
                    textView0.setVisibility(View.INVISIBLE);
                    textView60.setVisibility(View.INVISIBLE);
                    //초기화 버튼 보이게 함.
                    buttonReset.setVisibility(View.VISIBLE);
                    // 타이머 이미지 보이기
                    setLottieVisible(animationViewTimer, true);
                    // 시계 이미지 숨기기
                    //setLottieVisible(animationViewPencilLeft, false);
                    setLottieVisible(animationViewSearch, false);
                    //"지금 당신이 집중하고자 하는 일을 적어주세요" 숨기기
                    textViewInfo.setVisibility(View.INVISIBLE);
                    // 할일 내용 적는 EditText 비활성화
                    setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, false);

                    // Task 객체 생성
                    task = new TaskDTO();
                    // 버튼을 누른 시간을 가져와서 시작 시간에 저장
                    task.setStartTime(getCurrentTime());

                    // 시간 단위가 분 일때
                    if(preferences.getBoolean("settingTimerUnitMinute", true)){
                        currentTime *= 60;
                    }

                    // 카운트 타이머 객체 생성
                    countDownTimer = new CountDownTimer(currentTime*1000, 1000) { // (타이머 설정 시간, 간격(1000ms --> 1초)
                        // 시간이 흐를 때 실행 되는 함수
                        @Override
                        public void onTick(long millisUntilFinished) { //(현재 시간을 ms 단위로 나타니는 매개변수)

                            //현재 타이머에서 흐르고 있는 시간을 현재시간 변수에 넣음.
                            //일시정지 되었을 때 일시정지된 시간 부터 흐르게 하기 위해서.
                            currentTime = (int)(millisUntilFinished/1000);

                            // 시간이 흐를 때 원형 타이머 가운데 텍스트도 시간이 줄어들게 함.
                            pieView.setTextFormatter(new ProgressTextFormatter() {
                                @Override
                                public CharSequence provideFormattedText(float v) {
                                    // 초가 2자리수 일때
                                    if ((currentTime - ((currentTime / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
                                        return ((currentTime / 60) + "분 " + (currentTime - ((currentTime / 60) * 60)) + "초");
                                    }
                                    // 초가 1자리수 일때
                                    else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                                        return ((currentTime / 60) + "분 0" + (currentTime - ((currentTime / 60) * 60)) + "초");
                                    }

                                }
                            });

                            // 원형 파이도 시간에 줄어듬에 따라 줄어듬.
                            // 분
                            if(preferences.getBoolean("settingTimerUnitMinute", true)){
                                Log.v(TAG, "타이머 실행 중 progress" +  ((float)currentTime / 3600f) * 100f);
                                pieView.setProgress(((float)currentTime / 3600f) * 100f, true);
                            }
                            //초
                            else {
                                Log.v(TAG, "타이머 실행 중 progress" +  (float)currentTime * 5.0f/ 3.0f);
                                pieView.setProgress((float)currentTime * 5.0f/ 3.0f, true);
                            }


                        }
                        // 타이머가 끝났을 때 실행되는 함수
                        @Override
                        public void onFinish() {
                            //시작 여부를 false로 바꿈
                            isTimerStart = false;
                            //버튼 이미지를 시작 버튼으로 바꿈
                            imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play_button, null));
                            //seekbar 다시 보이게 함
                            seekBar.setVisibility(View.VISIBLE);
                            //seekbar아래 텍스트 다시 보이게 함.
                            textView0.setVisibility(View.VISIBLE);
                            textView60.setVisibility(View.VISIBLE);
                            //초기화 버튼 안 보이게 함.
                            buttonReset.setVisibility(View.INVISIBLE);
                            // 연필 이미지 보이게 함.
                            //setLottieVisible(animationViewPencilLeft,true);
                            setLottieVisible(animationViewSearch, true);
                            // 모래시계 이미 숨김
                            setLottieVisible(animationViewTimer, false);
                            // 할일 적는 EditText 활성화
                            setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, true);
                            //"지금 당신이 집중하고자 하는 일을 적어주세요" 보이기
                            textViewInfo.setVisibility(View.VISIBLE);
                            // 원형 타이머 안에 텍스트 사용자가 마지막으로 설정된 시간으로 보여주기
                            setPieViewText(setTime);
                            // pieVIew 사용자가 마지막으로 설정한 시간으로 맞추기
                            pieView.setProgress((float)setTime * 5.0f/ 3.0f, true);

                            // 나머지 정보들 TaskDTO 객체 저장
                            saveDataIntoTaskDTO(task);

                            // 데이터 쉐어드에 저장
                            saveDataIntoSharedPreference();

                            //진동 울림
                            playVibrator(preferences.getBoolean("settingVibrate", true));

                            //알림음 울리기
                            playNotificationSound(preferences.getBoolean("settingSound", true));

                            // 종료 다이얼로그 띄움.
                            showFinishDialog();

                        }
                    }.start(); // 타이머 시작

                }
                // 타이머가 실행되고 있을 때
                //isTimerStart가 true
                else {
                    //시작 여부를 false로 바꿈
                    isTimerStart = false;
                    //버튼 이미지를 시작 버튼으로 바꿈
                    imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play_button, null));

                    // 타이머 이미지 애니메이션 멈춤
                    animationViewTimer.setRepeatCount(0);

                    // 나머지 정보들 TaskDTO 객체에 저장
                    saveDataIntoTaskDTO(task);

                    // 타이머 정지
                    countDownTimer.cancel();

                }

            }
        });
    }

    // pieVIew 00:00으로 설정
    private void setPieViewText(int num){
        pieView.setTextFormatter(new ProgressTextFormatter() {
            @Override
            public CharSequence provideFormattedText(float v) {

                //분
                if(preferences.getBoolean("settingTimerUnitMinute", true)){
                    return num + "분 0초";
                }
                //초
                else{
                    return "0분 " + num + "초";
                }

            }
        });
    }

    // 초기화 버튼 설정
    private void setButtonReset(){

        //초기화 버튼 리스너
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialog();

            }
        });
    }

    // 현재시간 가져오기
    private String getCurrentTime(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();

        String formatDate = sdf.format(calendar.getTime());

        return formatDate;

    }

    // 걸린시간 계산하기
    private String subtractTime(String startTime, String endTime) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date start = sdf.parse(startTime);
        Date end = sdf.parse(endTime);

        long subtractedTime = (end.getTime() - start.getTime())/1000;

        int minute = (int)subtractedTime/60;
        int second = (int)subtractedTime%60;

        String time;

        //0분 일때
        if(minute == 0){
            time = "0분 " + second + "초";
        }
        else{
            time = minute + "분 " + second + "초";
        }

        return time;

    }

    // TaskDTO 객체에 데이터 저장하기
    private void saveDataIntoTaskDTO(TaskDTO task){

        // key값 넣기
        task.setKey(createRandomKey());

        // 한 일 내용을 Task객체에 저장
        task.setTaskName(editTextTaskName.getText().toString());

        // 버튼 누른 시간을 Task객체 끝낸시간에 저장
        task.setEndTime(getCurrentTime());

        // 일의 걸린 시간을 Task객체에 저장
        try {
            task.setDurationTime(subtractTime(task.getStartTime(), task.getEndTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 카테고리 내용
        // 카테고리가 있을 때
        if(!textViewCategory.getText().toString().isEmpty()){
            task.setCategoryName(textViewCategory.getText().toString());
            Log.v(TAG, "task 카테고리 : " + task.getCategoryName());
            task.setCategoryColor(textViewCategory.getCurrentTextColor());
            Log.v(TAG, "task 카테고리 색 코드 : " + task.getCategoryColor()+"");
        }
        // 카테고리가 없을 때
        else{
            task.setCategoryName("카테고리 없음");
            // TextView의 디폴트 값을 가지고 옴.
            task.setCategoryColor(textViewCategory.getCurrentTextColor());
        }

        // TaskDTO 객체를 리스트에 넣음,.
        tasks.add(task);

        // TaskDTO 지움
        task = null;

    }

    // 임의의 8자리 Key 생성
    private String createRandomKey() { //이메일 인증코드 생성
        String[] str = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String newCode = new String();

        for (int x = 0; x < 8; x++) {
            int random = (int) (Math.random() * str.length);
            newCode += str[random];
        }

        return newCode;
    }

    // 쉐어드에 데이터 저장하기
    private void saveDataIntoSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

//            //TaskDTO의 key값을 쉐어드 key값으로 하고
//            //value값을 "할일내용 + 시작시간 + 끝낸시간 +  걸린시간"으로 저장
//
//            String value = tasks.get(i).getTaskName() + "★"     // 할일 내용
//                    + tasks.get(i).getStartTime() + "★" // 시작 시간
//                    + tasks.get(i).getEndTime() + "★" // 끝낸 시간
//                    + tasks.get(i).getDurationTime();
//
//            editor.putString(tasks.get(i).getKey(), value);
//
//            editor.apply();

        // 한 일 객체를 json 에서 string 바꾸고 넣을 값.
        String tasksJson = null;

        // 데이터를 처음 넣을 때
        // key 값이 없을 때
        if(preferences.getString("tasks", null) == null){
            //리스트를 json으로 변환해서 쉐어드에 저장하기
            try{
                JSONArray jsonArray = new JSONArray();
                for(int i = 0; i < tasks.size(); i++){
                    //키값 저장
                    //처음 넣을 때
                    if(preferences.getString("keys", null) == null){
                        editor.putString("keys", tasks.get(i).getKey());
                        editor.apply();
                    }
                    //그 이후로 넣을 때
                    else {
                        String value = preferences.getString("keys", null);
                        StringBuffer stringBufferKeys = new StringBuffer(value);
                        stringBufferKeys.append("★" + tasks.get(i).getKey());

                        editor.putString("keys", stringBufferKeys.toString());
                        editor.apply();
                    }

                    // 할 일 이름 저장 --> 할 일 검색을 위해서
                    editor.putString("taskNames", tasks.get(i).getTaskName());


                    // 객체 저장
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("key", tasks.get(i).getKey());
                    jsonObject.put("taskName", tasks.get(i).getTaskName());
                    jsonObject.put("startTime", tasks.get(i).getStartTime());
                    jsonObject.put("endTime", tasks.get(i).getEndTime());
                    jsonObject.put("durationTime", tasks.get(i).getDurationTime());
                    jsonObject.put("categoryName", tasks.get(i).getCategoryName());
                    jsonObject.put("categoryColor", tasks.get(i).getCategoryColor());
                    jsonArray.put(jsonObject);

                }
                tasksJson = jsonArray.toString();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        //처음 넣는 값이 아닐 때
        else{
            //쉐어드에서 값을 가지고 옴.
            String storedValues = preferences.getString("tasks", null);

            //값에서 마지막 인덱스를 문자를 제외한 문자열 저장
            //예) [{"name":"steven"},{"name":"song"}]에서 마지막 "]"를 뻄.
            String storedData = storedValues.substring(0, storedValues.length() - 1);
            StringBuffer stringBuffer = new StringBuffer(storedData);

            //예) [{"name":"steven"},{"name":"song"} + ,
            stringBuffer.append(",");

            String newData = null;
            //리스트를 json으로 변환해서 쉐어드에 저장하기
            try{
                JSONArray jsonArray = new JSONArray();

                String value = preferences.getString("keys", null);
                StringBuffer stringBufferKeys = new StringBuffer(value);

                for(int i = 0; i<tasks.size(); i++){

                    // 키값 저장
                    stringBufferKeys.append("★" + tasks.get(i).getKey());

                    editor.putString("keys", stringBufferKeys.toString());

                    // 할 일 내용 저장 --> 할 일 검색을 위해서
                    // 할 일 내용들의 데이터가 한개 이상 있을 때
                    if(preferences.getString("taskNames", null) != null){

                        String taskNameValue = preferences.getString("taskNames", null);

                        String[] taskNameValues = taskNameValue.split("★");
                        StringBuffer stringBufferTaskName = new StringBuffer(taskNameValue);
                        int n = 0;
                        // 검색어 중복 방지
                        // 사용자가 입력한 값과 쉐어드에 있는 값을 비교함.
                        // 사용자가 입력학 값과 쉐어드에 있는 값들을 모두 비교하여 하나도 중복되는 것이 없으면
                        // 쉐어드에 저장
                        // 그러지 않으면 저장을 안함.
                        for(int j = 0; j < taskNameValues.length; j++) {
                            // 사용자가 입력한 할 일의 내용과 이미 리스트에 있는 검색어 내용 중 다른 경우만 쉐어드에 저장
                            if (!tasks.get(i).getTaskName().trim().equals(taskNameValues[j].trim())) {
                                n++;
                            }

                        }
                        if(n == taskNameValues.length){
                            stringBufferTaskName.append("★" + tasks.get(i).getTaskName());
                            editor.putString("taskNames", stringBufferTaskName.toString());
                            editor.apply();
                        }
                    }
                    // 할 일 내용 데이터가 한개도 없을 때
                    else{
                        editor.putString("taskNames", tasks.get(i).getTaskName());
                        editor.apply();
                    }

                    // 객체 저장
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("key", tasks.get(i).getKey());
                    jsonObject.put("taskName", tasks.get(i).getTaskName());
                    jsonObject.put("startTime", tasks.get(i).getStartTime());
                    jsonObject.put("endTime", tasks.get(i).getEndTime());
                    jsonObject.put("durationTime", tasks.get(i).getDurationTime());
                    jsonObject.put("categoryName", tasks.get(i).getCategoryName());
                    jsonObject.put("categoryColor", tasks.get(i).getCategoryColor());
                    jsonArray.put(jsonObject);

                }
                newData = jsonArray.toString();
            }catch (JSONException e){
                e.printStackTrace();
            }

            stringBuffer.append(newData.substring(1));

            tasksJson = stringBuffer.toString();

        }

        if(tasksJson != null){
            editor.putString("tasks", tasksJson);
            editor.apply();
        }

        //모두 저장후 리스트 비우기
        tasks.clear();

    }

    // 초기화 다이얼로그 보여주기
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("초기화");
        builder.setMessage("시간을 기록하시겠습니까?");
        // 아니요
        builder.setPositiveButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetMode();

                        // 연필 애니메이션 보이기
                        //setLottieVisible(animationViewPencilLeft, true);
                        setLottieVisible(animationViewSearch, true);
                        // 타이머 애니메이션 숨기기
                        setLottieVisible(animationViewTimer, false);
                        // 할일 적는 EditText 활성화
                        setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, true);
                        //"지금 당신이 집중하고자 하는 일을 적어주세요" 보이기
                        textViewInfo.setVisibility(View.VISIBLE);
                    }
                });
        // 예
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // 연필 애니메이션 보이기
                        //setLottieVisible(animationViewPencilLeft, true);
                        setLottieVisible(animationViewSearch, true);
                        // 타이머 애니메이션 숨기기
                        setLottieVisible(animationViewTimer, false);

                        // 할일 적는 EditText 활성화
                        setEditTextTaskAndTextView(editTextTaskName, textViewTaskName, true);
                        //"지금 당신이 집중하고자 하는 일을 적어주세요" 보이기
                        textViewInfo.setVisibility(View.VISIBLE);

                        // 타이머가 진행 중일때
                        if(isTimerStart){
                            saveDataIntoTaskDTO(task);
                        }

                        resetMode();

                        saveDataIntoSharedPreference();

                    }
                });
        builder.show();

    }

    // 초기화 상태로 설정하기
    private void resetMode(){
        // seekbar 보이게 하기
        seekBar.setVisibility(View.VISIBLE);
        //seekbar아래 텍스트 다시 보이게 함.
        textView0.setVisibility(View.VISIBLE);
        textView60.setVisibility(View.VISIBLE);
        // 타이머 시작 여부 false로 바꿈
        isTimerStart = false;
        // 시작 버튼으로 바꿈
        imageButtonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play_button, null));
        // 초기화 버튼 안보이게 히기
        buttonReset.setVisibility(View.INVISIBLE);
        // pieVIew 사용자가 마지막으로 설정한 시간으로 맞추기
        pieView.setProgress((float)setTime * 5.0f/ 3.0f, true);
        // 원형 타이머 안에 텍스트 사용자가 마지막으로 설정된 시간으로 보여주기
        setPieViewText(setTime);
        // 현재시간에 마지막으로 사용자가 설정한 시간을 저장
        // 분
        if(preferences.getBoolean("settingTimerUnitMinute", true)){
            currentTime = setTime * 60;
        }
        // 초
        else {
            currentTime = setTime;
        }

        // 카운트 다운 타이머 끄시
        countDownTimer.cancel();
    }

    // 로띠 애니메이션 설정
    private void setLottie() {
        //animationViewPencilLeft.playAnimation();
        //animationViewPencilLeft.setRepeatCount(LottieDrawable.INFINITE);

        animationViewSearch.playAnimation();
        animationViewSearch.setRepeatCount(LottieDrawable.INFINITE);

        // 타이머 애니메이션
        animationViewTimer.playAnimation();
        animationViewTimer.setRepeatCount(LottieDrawable.INFINITE);
    }

    // 로띠 애니메이션 보이게 하기
    private void setLottieVisible(LottieAnimationView animationView, Boolean visible) {
        // true 일때 보이게 하기
        if(visible){
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
            animationView.setRepeatCount(LottieDrawable.INFINITE);
        }
        // false 일때 숨기기
        else{
            animationView.setVisibility(View.INVISIBLE);
            animationView.pauseAnimation();

        }

    }

    // EditText TextView 설정
    private void setEditTextTaskAndTextView(EditText editText, TextView textView, Boolean editTextVisible){
        // 사용자가 할일을 입력할 수 있을 때
        if(editTextVisible){
            //Edittext 보이기
            editText.setVisibility(View.VISIBLE);

            //TextView 숨기기
            textView.setVisibility(View.INVISIBLE);

            //TextView 선
        }
        // 사용자가 할 일을 입력할 수 없을 때
        else{
            // EditText 숨기기
            editText.setVisibility(View.INVISIBLE);

            //TextView 보이기
            textView.setVisibility(View.VISIBLE);

            // EditText에 쓴 글을 TextView에 보내기
            textView.setText(editText.getText().toString());

            // textView 선택을 해줘야 글자가 흐름
            textView.setSelected(true);

        }


    }

    // 검색 아이콘 클릭 했을 때 할일 검색 창으로 넘어가기
    private void setAnimationSearch(){
        animationViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // EditText 클릭 했을 때 키보드 숨기기
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // 할 일 검색 화면으로 넘어감
                Intent intent = new Intent(MainActivity.this, TaskNameSearchActivity.class);
                // 만약에 할일 내용에 값이 있을 경우 인텐트로 넘겨줌.
                intent.putExtra("taskName", editTextTaskName.getText().toString());

                startActivityForResult(intent, 100);
            }
        });
    }

    // 인텐트 결과값 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                // 할 일 내용
                case 100:
                    //할 일 내용을 적는 editTextTaskName에 검색창에서 인텐트로 받아온 할 일 내용으로 설정
                    editTextTaskName.setText(data.getStringExtra("taskName"));
                    break;
                // 카테고리
                case 101:
                    // 인텐트로 받은 값으로 카테고리 설정
                    textViewCategory.setText(data.getStringExtra("categoryName"));  // 카테고리 내용
                    textViewCategory.setTextColor(data.getIntExtra("categoryColor", 0));
                    break;
            }
        }
//        else if(resultCode == RESULT_CANCELED){
//            switch (requestCode){
//                case 101:
//                    textViewCategory.setText("카테고리 없음");
//                    textViewCategory.setTextColor(0x000000);
//                    break;
//            }
//        }
    }

    // 백 버튼을 눌렀을 때
    @Override
    public void onBackPressed() {
        showAdDialog();
    }

    // 광고 다이얼로그 보여주기
    private void showAdDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.close_dialog, null);
        builder.setView(view);

        final AdView adView = view.findViewById(R.id.adView);
        final Button buttonCancel = view.findViewById(R.id.buttonCancel);
        final Button buttonClose = view.findViewById(R.id.buttonClose);

        final AlertDialog dialog = builder.create();

        //취소 버튼
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 닫기
                dialog.dismiss();
            }
        });

        //종료 버튼
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 앱 종료
                finish();
            }
        });

        // 광고 요청 객체
        AdRequest adRequest = new AdRequest.Builder().build();

        // 요청한 광고를 광고뷰에 로딩하기
        adView.loadAd(adRequest);

        dialog.show();

    }

    // 진동 울리기
    private void playVibrator(boolean vibration){

        if(vibration){
            // 바이레이터 객체 셍성
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

            // 1초 진동이 울리고 0.05초
            long[] pattern = {0, 200, 50, 200, 1000};

            // -1 --> 반복 안함. 0 --> 무한 진동
            vibrator.vibrate(pattern, 0);
        }

    }

    // 알림음 울리기
    private void playNotificationSound(boolean playSound){

        if(playSound){

            String soundPath = preferences.getString("settingSoundPath",RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
            Log.v(TAG, "알림음 경로 = " + soundPath);
            Uri uri = Uri.parse(soundPath);
            Log.v(TAG, "알림음 uri = " + uri.toString());
            ringtone = RingtoneManager.getRingtone(this, uri);
            ringtone.play();

        }
    }

    // 사용가 설정 값으로 세팅하기
    private void settingByUserSetting(Boolean timerUnit, Boolean clockWise, int timerColor){

        // 시간 단위
        //분
        if(timerUnit){
            textView0.setText("0분");
            textView60.setText("60분");
        }
        //초
        else {
            textView0.setText("0초");
            textView60.setText("60초");
        }

        // 시계 방향 설정
        // 시계 방향
        if(clockWise){
            pieView.setOrientation(BaseModeRenderer.ORIENTATION_CLOCKWISE);
        }
        // 반 시계 방향
        else {
            pieView.setOrientation(BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE);
        }

        // 타이머 색상
        pieView.setProgressColor(timerColor);

    }

    // 알림 종료 다이얼로그
    private void showFinishDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("설정 시간 종료");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(preferences.getBoolean("settingVibrate", true)){
                            // 진동 종료
                            vibrator.cancel();
                        }

                        if(preferences.getBoolean("settingSound", true)){
                            // 소리 종료
                            ringtone.stop();
                        }

                    }
                });
        builder.show();
    }
}
