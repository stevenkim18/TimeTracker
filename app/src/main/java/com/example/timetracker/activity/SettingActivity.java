package com.example.timetracker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timetracker.BottomNavigationHelper;
import com.example.timetracker.BuildConfig;
import com.example.timetracker.R;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.BootpayAnalytics;
import kr.co.bootpay.CancelListener;
import kr.co.bootpay.CloseListener;
import kr.co.bootpay.ConfirmListener;
import kr.co.bootpay.DoneListener;
import kr.co.bootpay.ErrorListener;
import kr.co.bootpay.ReadyListener;
import kr.co.bootpay.enums.Method;
import kr.co.bootpay.enums.PG;

public class SettingActivity extends AppCompatActivity {

    LinearLayout timerUnit,timerClockwise, timerColor, soundView, weekViewNumOfDays;
    TextView timerUnitTextView, timerClockwiseTextView, timerColorView, soundTextView, notificationSoundTitle, numOfDaysTextView, supportView;
    CheckedTextView vibratorCheckTextView, soundCheckTextView;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    BottomNavigationView bottomNavigationView;

    // 설정 값들
    Boolean isTimerUnitMinute;
    Boolean clockwise;
    int timerColorHex;
    Boolean vibrator;
    Boolean sound;
    String soundName;
    int numOfDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        timerUnit = findViewById(R.id.timerUnit);                           // 시간 단위 뷰
        timerUnitTextView = findViewById(R.id.timerUnitTextView);           // 시간 단위 텍스트 뷰
        timerClockwise = findViewById(R.id.timerClockwise);                 // 타이머 방향
        timerColor = findViewById(R.id.timerColor);                         // 타이머 색상
        timerClockwiseTextView = findViewById(R.id.timerClockwiseTextView); // 타이머 방향 텍스트 뷰
        timerColorView = findViewById(R.id.timerColorView);                 // 타이머 색상 배경 색
        vibratorCheckTextView = findViewById(R.id.vibratorCheckTextView);   // 진동 체크박스
        soundCheckTextView = findViewById(R.id.soundCheckTextView);         // 소리 체크박스
        soundView = findViewById(R.id.soundView);                           // 알림음 선택
        notificationSoundTitle = findViewById(R.id.notificationSoundTitle); // "알림음" 텍스트
        soundTextView = findViewById(R.id.soundTextView);                   // 알림음 제목
        weekViewNumOfDays = findViewById(R.id.weekViewNumOfDays);           // 달력 날짜 뷰
        numOfDaysTextView = findViewById(R.id.numOfDaysTextView);           // 날짜 수 텍스트 뷰
        supportView = findViewById(R.id.supportView);                       // 후원 하기 뷰

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // 쉐어드 프리퍼런스 객체 생성
        preferences = getSharedPreferences("task", MODE_PRIVATE);
        editor = preferences.edit();

        setBottomNavigationView();

        // 시간 단위
        setTimerUnit();

        // 타이머 방향
        setTimerClockwise();

        // 타이머 색상
        setTimerColor();

        // 진동
        setVibratorCheckTextView();

        // 소리
        setSoundCheckTextView();

        // 알림음 선택
        setSoundView();

        // 날짜 수
        setWeekViewNumOfDays();

        // 결제 api 사용을 위한 초기 설정
        BootpayAnalytics.init(this, "59a4d326396fa607cbe75de5");


        setSupportView();

        getVaulesFromSharedPreference();

        setSettingsByUserSetting(isTimerUnitMinute, clockwise, timerColorHex, vibrator, sound, soundName, numOfDays);

    }

    //BottomNavigationView 설정하기
    private void setBottomNavigationView(){

        //BottomNavigationView shiftMode를 비활성화 시키기
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_setting); // 4번째 달력 버튼이 활성화 되어 있게 함.

        // BottomNavigationView 아이템 클릭 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    // 그래프
                    case R.id.nav_graph :
                        startActivity(new Intent(SettingActivity.this, GraphActivity.class));
                        finish();
                        break;
                    // 목록
                    case R.id.nav_list :
                        startActivity(new Intent(SettingActivity.this, ListActivity.class));
                        finish();
                        break;
                    // 타이머
                    case R.id.nav_timer :
                        startActivity(new Intent(SettingActivity.this, MainActivity.class));
                        finish();
                        break;
                    // 캘린더
                    case R.id.nav_calender :
                        startActivity(new Intent(SettingActivity.this, CalendarActivity.class));
                        finish();
                        break;
                    // 설정
                    case R.id.nav_setting :

                        break;
                }
                // false 를 하면 BottomNavigationView 버튼이 고정이 되서 움직이지 않음.
                // true 를 해야 버튼이 눌렸을 때 효과 발동
                return true;
            }
        });
    }

    //시간 단위 레이아웃 설정
    private void setTimerUnit(){

        timerUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showTimerUnitDialog();

            }
        });
    }

    //시간 단위 옵션 다이얼로그 보여주기
    private void showTimerUnitDialog(){
        final CharSequence items[] = new CharSequence[]{"분(minute)", "초(second)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("시간 단위");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // 분
                    case 0:
                        // 텍스트를 "분"으로 바꾸기
                        timerUnitTextView.setText(items[0]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putBoolean("settingTimerUnitMinute", true);
                        editor.apply();
                        break;
                    // 초
                    case 1:
                        // 텍스트를 "초"으로 바꾸기
                        timerUnitTextView.setText(items[1]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putBoolean("settingTimerUnitMinute", false);
                        editor.apply();
                        break;

                }
                // 다이얼로그 종료
                dialog.dismiss();
            }
        });

        // 다이얼로그 보여주기
        builder.show();
    }

    //타이머 방향 레이아웃 설정
    private void setTimerClockwise(){

        // 타이머 방향 레이아웃 클릭 시
        timerClockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClockwiseListDialog();
            }
        });

    }

    // 타이머 방향 옵션 다이얼로그 보여주기
    private void showClockwiseListDialog() {

        final CharSequence items[] = new CharSequence[]{"시계방향", "반시계방향"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("타이머 방향");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // 시계 방향
                    case 0:
                        // 텍스트를 "시계방향"으로 바꾸기
                        timerClockwiseTextView.setText(items[0]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putBoolean("settingTimerClockWise", true);
                        editor.apply();
                        break;
                    // 반시계 방향
                    case 1:
                        // 텍스트를 "반시계방향"으로 바꾸기
                        timerClockwiseTextView.setText(items[1]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putBoolean("settingTimerClockWise", false);
                        editor.apply();
                        break;

                }
                // 다이얼로그 종료
                dialog.dismiss();
            }
        });

        // 다이얼로그 보여주기
        builder.show();

    }

    // 타이머 색상 레이아웃 설정
    private void setTimerColor(){

        timerColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog();
            }
        });
    }

    // 색상 다이얼로그 보여주기
    private void showColorPickerDialog(){

        // 색상 다이얼로그 객체
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("타이머 색상")                         // 제목
                .initialColor(Color.WHITE)                       // 초기화 색 --> 흰색
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)    // 색 보여주는 타입
                .density(5)                                      // 깊이 숫자 설정
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 다이얼로그 종료
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("선택", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        // 칼라뷰의 색상을 사용자가 선택한 색상으로
                        timerColorView.setBackgroundColor(lastSelectedColor);
                        editor.putInt("SettingTimerColor", lastSelectedColor);
                        editor.apply();
                    }
                })
                .build()
                .show();

    }

    // 진동 체크박스 설정하기
    private void setVibratorCheckTextView(){

        vibratorCheckTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 체크 텍스트 뷰 안에 있는 체크 박스를 저장
                CheckedTextView view = (CheckedTextView) v;
                // 클릭시 체크박스 체크 or 체크 해제
                view.toggle();

                // 진동여부가 체크되어 있을 때
                if(view.isChecked()){
                    editor.putBoolean("settingVibrate", true);
                }
                // 체크가 안 되어 있을 때
                else {
                    editor.putBoolean("settingVibrate", false);
                }
                editor.apply();

            }
        });
    }

    // 소리 채크박스 설정하기
    private void setSoundCheckTextView(){

        soundCheckTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 체크 텍스트 뷰 안에 있는 체크 박스를 저장
                CheckedTextView view = (CheckedTextView) v;
                // 클릭시 체크박스 체크 or 체크 해제
                view.toggle();

                // 진동여부가 체크되어 있을 때
                if(view.isChecked()){
                    editor.putBoolean("settingSound", true);
                    // 알림음 선택 뷰 활성화
                    soundView.setClickable(true);
                    soundView.setFocusable(true);
                    // 글씨 활성화
                    notificationSoundTitle.setTextColor(Color.BLACK);
                    soundTextView.setTextColor(Color.parseColor("#808080"));    // 기본 TextView 색상
                }
                // 체크가 안 되어 있을 때
                else {
                    editor.putBoolean("settingSound", false);
                    // 알림음 선택 뷰 비활성화
                    soundView.setClickable(false);
                    soundView.setFocusable(false);
                    // 글씨 비활성화
                    notificationSoundTitle.setTextColor(Color.parseColor("#cccccc"));
                    soundTextView.setTextColor(Color.parseColor("#cccccc"));
                }
                editor.apply();
            }
        });

    }

    // 알림음 선택 설정하기
    private void setSoundView(){

        soundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 기본 알림음 선택 화면으로 넘어가기
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);             // 암시적 인텐트
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알림음 선택");    // 제목
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);      // 무음을 선택 리스트에서 제외
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);      // 기본 벨소리는 선택리스트에 넣음.
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALL);  // 전체 기본 벨소리, 알림음 등을 다 가지고 옴.
                startActivityForResult(intent, 777);

            }
        });
    }

    // 날짜 수 설정하기
    private void setWeekViewNumOfDays(){

        weekViewNumOfDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberOfDaysListDialog();
            }
        });

    }

    // 날짜 수 선택 다이얼로그 보여주기
    private void showNumberOfDaysListDialog(){

        final CharSequence items[] = new CharSequence[]{"1일", "2일", "3일"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("한 화면에 보이는 일 수");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // 1일
                    case 0:
                        // 텍스트를 "1일"으로 바꾸기
                        numOfDaysTextView.setText(items[0]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putInt("settingNumOfDays", 1);
                        editor.apply();
                        break;
                    // 2일
                    case 1:
                        // 텍스트를 "2일"으로 바꾸기
                        numOfDaysTextView.setText(items[1]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putInt("settingNumOfDays", 2);
                        editor.apply();
                        break;
                    // 3일
                    case 2:
                        // 텍스트를 "3일"으로 바꾸기
                        numOfDaysTextView.setText(items[2]);
                        // 쉐어드에 시계 방향 설정 저장하기
                        editor.putInt("settingNumOfDays", 3);
                        editor.apply();
                        break;

                }
                // 다이얼로그 종료
                dialog.dismiss();
            }
        });

        // 다이얼로그 보여주기
        builder.show();


    }

    // 후원하기 설정하기
    private void setSupportView(){

        supportView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSupportDialog();

            }
        });


    }

    // 후원하기 다이얼로그 보여부기
    private void showSupportDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("후원하기");
        builder.setMessage("앱을 개발하느라 수고한 개발자에게 후원해주세요");
        builder.setPositiveButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showPayActivity();
                    }
                });
        builder.show();
    }

    //결제 화면으로 넘어가기
    private void showPayActivity(){
        Bootpay.init(getFragmentManager())
                .setApplicationId("59a4d326396fa607cbe75de5")
                .setPG(PG.KAKAO) // 결제할 PG 사 --> 다날
                .setMethod(Method.EASY) // 결제수단
                .setName("개발자에게 후원하기") // 결제할 상품명
                .setOrderId("1234") // 결제 고유번호
                .setPrice(1000) // 결제할 금액
                .onConfirm(new ConfirmListener() { // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                    @Override
                    public void onConfirm(@Nullable String message) {
                        Log.d("confirm", message);
                    }
                })
                .onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                    @Override
                    public void onDone(@Nullable String message) {
                        Toast.makeText(getApplicationContext(), "결제가 취소 되었습니다!", Toast.LENGTH_SHORT).show();
                        Log.d("done", message);
                    }
                })
                .onCancel(new CancelListener() { // 결제 취소시 호출
                    @Override
                    public void onCancel(@Nullable String message) {
                        Toast.makeText(getApplicationContext(), "결제가 취소 되었습니다!", Toast.LENGTH_SHORT).show();
                        Log.d("cancel", message);
                    }
                })
                .onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                    @Override
                    public void onError(@Nullable String message) {
                        Log.d("error", message);
                    }
                })
                .onClose(new CloseListener() { //결제창이 닫힐때 실행되는 부분
                    @Override
                    public void onClose(String message) {
                        Log.d("close", "close");
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 777:
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);

                    String soundName = ringtone.getTitle(this);
                    if (uri != null) {
                        String ringtonePath = uri.toString();
                        // 쉐어드에 저장
                        soundTextView.setText(soundName);                       // 알림음 제목
                        editor.putString("settingSoundName", soundName);
                        editor.putString("settingSoundPath", ringtonePath);     // 알림음 경로
                        editor.apply();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    // 쉐어드에서 사용자 세팅 값 가지고 오기
    private void getVaulesFromSharedPreference(){

        // 시간 단위
        isTimerUnitMinute = preferences.getBoolean("settingTimerUnitMinute", true);

        // 시계 방향
        clockwise = preferences.getBoolean("settingTimerClockWise", true);

        // 타이머 색상
        timerColorHex = preferences.getInt("SettingTimerColor", Color.parseColor("#D81B60"));

        // 진동
        vibrator = preferences.getBoolean("settingVibrate", true);

        // 소리
        sound = preferences.getBoolean("settingSound", true);

        // 알림음 제목
        soundName = preferences.getString("settingSoundName", "기본 벨소리");

        // 달력에 보이는 날짜 수
        numOfDays = preferences.getInt("settingNumOfDays", 1);

    }

    // 사용자 세팅 값으로 설정 하기
    private void setSettingsByUserSetting(Boolean isTimerUnitMinute, Boolean clockwise, int timerColor, Boolean vibrator, Boolean sound, String soundName, int numOfDays){


        // 시간 단위
        if(isTimerUnitMinute){
            timerUnitTextView.setText("분(minute)");
        }
        else{
            timerUnitTextView.setText("초(second)");
        }

        // 타이머 방향
        if(clockwise){
            timerClockwiseTextView.setText("시계방향");
        }
        else {
            timerClockwiseTextView.setText("반시계방향");
        }

        // 타이머 색상
        timerColorView.setBackgroundColor(timerColor);

        // 진동
        if(vibrator){
            vibratorCheckTextView.setChecked(true);
        }
        else {
            vibratorCheckTextView.setChecked(false);
        }

        // 소리
        if(sound){
            soundCheckTextView.setChecked(true);
            // 알림음 선택 뷰 활성화
            soundView.setClickable(true);
            soundView.setFocusable(true);
            // 글씨 활성화
            notificationSoundTitle.setTextColor(Color.BLACK);
            soundTextView.setTextColor(Color.parseColor("#808080"));    // 기본 TextView 색상
        }
        else{
            soundCheckTextView.setChecked(false);
            // 알림음 선택 뷰 비활성화
            soundView.setClickable(false);
            soundView.setFocusable(false);
            // 글씨 비활성화
            notificationSoundTitle.setTextColor(Color.parseColor("#cccccc"));
            soundTextView.setTextColor(Color.parseColor("#cccccc"));
        }

        // 알림음 제목
        soundTextView.setText(soundName);

        //날짜 일 수
        if(numOfDays == 1){
            numOfDaysTextView.setText("1일");
        }
        else if(numOfDays == 2){
            numOfDaysTextView.setText("2일");
        }
        else if(numOfDays == 3){
            numOfDaysTextView.setText("3일");
        }

    }
}
