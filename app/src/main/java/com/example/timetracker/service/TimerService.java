package com.example.timetracker.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.timetracker.R;
import com.example.timetracker.activity.MainActivity;

import static com.example.timetracker.App.CHANNEL_ID;
import static com.example.timetracker.App.manager;

public class TimerService extends Service {

    private static final String TAG = "타이머서비스";

    private static final String MY_ACTION = "com.example.timetracker.action.ACTION_TIMER";

    private final int REQUEST_CODE = (int) System.currentTimeMillis();

    // 메인액티비티에서 받아온 시간을 저장하는 변수
    int currentTime;
    // 매인액티비티에서 사용자가 적은 한 일 내용을 저장하는 변수
    String taskName;
    // 메인 액티비티에서 사용자가 시작한 시간을 저장하는 변수
    String startTime;
    // 노피티케이션 채널 아이디
    final static int notificationId = 1;

    CountDownTimer countDownTimer;

    // 노피티케이션 빌더
    NotificationCompat.Builder builder;

    // 브로드캐스트 리시버
    TimerNotificationReceiver timerNotificationReceiver = null;

    // 타이머의 실행 여부
    Boolean isTimerRunning;

    // 서비스가 만들어 졌을 때
    @Override
    public void onCreate() {
        super.onCreate();
    }

    // 사용하지 않을 때는 onBind는 null 값을 반환하면 됨.
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    // 서비스가 시작 되기 전에
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 현재 흐르고 있는 시간을 인텐트로 받아와서 저장
        currentTime = intent.getIntExtra("currentTime", 0);
        Log.v(TAG, "엑티비티에서 받아 온 시간 = " + currentTime);

        taskName = intent.getStringExtra("taskName");

        startTime = intent.getStringExtra("startTime");

        // 리시버 등록
        timerNotificationReceiver = new TimerNotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MY_ACTION);
        filter.addAction("ACTION_PAUSE");
        filter.addAction("ACTION_CONTINUE");
        filter.addAction("ACTION_RESET");
        registerReceiver(timerNotificationReceiver, filter);

        // 타이머 실행 여부 --> 참
        isTimerRunning = true;

        updateNotification(currentTime, isTimerRunning);

        // 포그라운드 서비스 시작 --> 노피티케이션과 함께
        startForeground(notificationId, builder.build());

        // 카운트 다운 타이머 객체 생성
        countDownTimer = new CountDownTimer(currentTime * 1000, 1000) {
            // 타이머가 실행 중 일때
            @Override
            public void onTick(long millisUntilFinished) {
                currentTime = (int)(millisUntilFinished/1000);
                updateNotification(currentTime, isTimerRunning);
                manager.notify(notificationId, builder.build());
                Log.v(TAG, "현재 남은 시간 = " + currentTime);
            }
            // 타이머가 끝났을 때
            @Override
            public void onFinish() {
                // 서비스 종료
                stopSelf();
            }
        }.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy");
        // 타이머 종료
        countDownTimer.cancel();
        Log.v(TAG, "서비스가 죽을 때 currentTIme 시간 값 = " + currentTime);
        // 리시버 해제
        unregisterReceiver(timerNotificationReceiver);
        countDownTimer = null;
        builder = null;
        stopSelf();
    }

    // 초에서 한글 분 초 포맷으로 바꾸기
    private String secondToKoreanTimeFormat(int currentSecond){

        int minute = currentSecond / 60;
        int second = currentSecond % 60;

        return minute + "분 " + second + "초";
    }

    //노티피케이션 갱신 하기
    private void updateNotification(int currentTime, Boolean isTimerRunning){

        // 노티피케이션을 클릭하면 원래 액티비티로 돌아감.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("currentTime", currentTime);
//        Log.v(TAG, "notificationIntent에 담겨 있는 시간 = " + notificationIntent.getIntExtra("currentTime", 0));
        notificationIntent.putExtra("taskName", taskName);
        notificationIntent.putExtra("startTime", startTime);
        notificationIntent.putExtra("fromService", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // 팬딩 인텐트
        // 팬딩 인텐트는 해당 인텐트를 특정한 시점에 실행하라고 하는것 지금 말고
        // 노피티케이션에서는 노피티케이션이 클릭 했을 때 팬딩인텐트가 가지고 있는 인텐트를 실행.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Log.v(TAG, "pendingIntent 코드 = " + REQUEST_CODE);

        // 타이머 조작 인텐트
        Intent timerIntent = new Intent();

        String actionButtonTitle = null;

        // 타이머가 동작 중일 때
        if(isTimerRunning){
            // 인텐트를 만들 때 생성자 변수로 MY_ACTION을 주고 다시 setAction 으로 다른 값을 주니 MY_ACTION이 삭제 되었음.
            timerIntent.setAction("ACTION_PAUSE");
            // 버튼 이름이 "일시정지"
            actionButtonTitle = "일시정지";
        }
        // 타이머가 멈춰 있을 때
        else {
            timerIntent.setAction("ACTION_CONTINUE");
            // 버튼 이름이 "계속"
            actionButtonTitle = "계속";
        }

        // 타이머 "일시정지", "계속" 팬딩인텐트
        PendingIntent playAndPausePendingIntent = PendingIntent.getBroadcast(this, 0, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action playAndPauseAction = new NotificationCompat.Action(0, actionButtonTitle, playAndPausePendingIntent);

        // 초기화 인텐트
        Intent resetIntent = new Intent("ACTION_RESET");
        //resetIntent.setAction("ACTION_RESET");

        // 초기화 팬딩인텐트
        PendingIntent resetPendingIntent = PendingIntent.getBroadcast(this, 0 , resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //노피티케이션 생성
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(taskName)                               // 제목설정
                .setContentText(secondToKoreanTimeFormat(currentTime))   // 시간
                .setSmallIcon(R.drawable.ic_timelapse_black_24dp)        // 작은 아이콘
                .setContentIntent(pendingIntent)                         // 노티 클릭시 실행 되는 인텐트
                .setOnlyAlertOnce(true)                                  // 알림 한번만 울리게 하기
                .setAutoCancel(true)                                     // 자동삭제
                .addAction(playAndPauseAction)                           // "일시정지" or "계속" 버튼
                .addAction(0, "초기화",resetPendingIntent);   // 초기화 버튼

    }

    //노티 타이머 일시정지
    public void pauseTimer(){
        // 카운트 다운 타이머 일시 정저
        countDownTimer.cancel();
        // 타이머 실행 여부 false로 바꿈,
        isTimerRunning = false;

        //"일시정지" 버튼 --> "계속"으로 바뀌기 위해 notification 갱신
        updateNotification(currentTime, isTimerRunning);
        manager.notify(notificationId, builder.build());

    }

    // 노티 타이머 계속
    public void continueTimer(){

        // 그냥 countTimer.start()를 하면 안됨.
        // 타이머 객체를 새로 만들어야 함.
        // 그러지 않으면 처음 currentTIme 값이 들어감.
        countDownTimer= new CountDownTimer(currentTime * 1000, 1000) {
            // 타이머가 실행 중 일때
            @Override
            public void onTick(long millisUntilFinished) {
                currentTime = (int)(millisUntilFinished/1000);
                updateNotification(currentTime, isTimerRunning);
                manager.notify(notificationId, builder.build());
                Log.v(TAG, "현재 남은 시간 = " + currentTime);
            }
            // 타이머가 끝났을 때
            @Override
            public void onFinish() {
                // 서비스 종료
                stopSelf();
            }
        }.start();

        // 타이머 실행 여부 참으로 바꿈.
        isTimerRunning = true;
    }

    // 서비스의 메소드를 처리할 브로드캐스트 리시버
    public class TimerNotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v("타이머노티피케이션리시버", "onReceive");

            if(intent != null){

                String action = intent.getAction();
                switch (action) {
                    // 일시정지 버튼
                    case "ACTION_PAUSE":
                        Log.v("타이머노티피케이션리시버", "일시정지");
                        TimerService.this.pauseTimer();
                        break;
                    // 계속 버튼
                    case "ACTION_CONTINUE":
                        Log.v("타이머노티피케이션리시버", "계속");
                        TimerService.this.continueTimer();
                        break;
                    // 초기화 버튼
                    case "ACTION_RESET":
                        Log.v("타이머노티피케이션리시버", "초기화 버튼 클릭");
                        // 서비스 종료
//                        TimerService.this.stopSelf();

                        // 노피티케이션 창 스크롤를 올리기 위한 인텐트
                        Intent closeNotificationDrawerIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        // 방송 보내기
                        sendBroadcast(closeNotificationDrawerIntent);

                        // 다이얼로그 인텐트
                        Intent dialogIntent=  new Intent(context, MainActivity.class);
                        dialogIntent.putExtra("currentTime", currentTime);          // 남은 시간
                        dialogIntent.putExtra("taskName", taskName);                // 한 일 내용
                        dialogIntent.putExtra("startTime", startTime);              // 시작 시간
                        dialogIntent.putExtra("fromService", true);          // 서비스에 보낸 여부
                        dialogIntent.putExtra("showResetDialog", true);      // 초기화 다이얼로그 띄움 여부
                        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        // 타이머 화면으로 이동
                        startActivity(dialogIntent);

                        break;
                }

            }

        }
    }
}
