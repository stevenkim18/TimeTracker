package com.example.timetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.timetracker.DTO.TaskDTO;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimerFragment extends Fragment {

    //툴바
    Toolbar toolbar;
    EditText editTextTaskName;
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

    // 타이머가 실행되고 있는지 여부
    Boolean isTimerStart = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        editTextTaskName = view.findViewById(R.id.editTextTaskName);
        textView0 = view.findViewById(R.id.textView0);
        textView60 = view.findViewById(R.id.textView60);
        pieView = view.findViewById(R.id.pieView);
        seekBar = view.findViewById(R.id.seekBar);
        buttonReset = view.findViewById(R.id.buttonReset);
        imageButtonPlay = view.findViewById(R.id.imageButtonPlay);

        setPieViewZero();

        setSeekBar();

        setImageButtonPlay();

        setButtonReset();

        // 초기화 버튼 안보이게 하기
        buttonReset.setVisibility(View.INVISIBLE);

        return view;

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
                currentTime = progress;

                // 원형 타이머 가운데 글짜에 사용자가 설정한 시간이 나오게 함,
                pieView.setTextFormatter(new ProgressTextFormatter() {
                    @Override
                    public CharSequence provideFormattedText(float v) {
                        return progress + "초";
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

                    // Task 객체 생성
                    task = new TaskDTO();
                    // 버튼을 누른 시간을 가져와서 시작 시간에 저장
                    task.setStartTime(getCurrentTime());

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
                                        return ((currentTime / 60) + " : " + (currentTime - ((currentTime / 60) * 60)));
                                    }
                                    // 초가 1자리수 일때
                                    else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                                        return ((currentTime / 60) + " : 0" + (currentTime - ((currentTime / 60) * 60)));
                                    }

                                }
                            });

                            // 원형 파이도 시간에 줄어듬에 따라 줄어듬.
                            pieView.setProgress(currentTime * 5.0f/3.0f, true);

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

                            // 나머지 정보들 TaskDTO 객체 저장
                            saveDataIntoTaskDTO(task);

                            // 데이터 쉐어드에 저장
                            saveDataIntoSharedPreference();


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

                    // 나머지 정보들 TaskDTO 객체에 저장
                    saveDataIntoTaskDTO(task);

                    // 타이머 정지
                    countDownTimer.cancel();

                }

            }
        });
    }

    // pieVIew 00:00으로 설정
    private void setPieViewZero(){
        pieView.setTextFormatter(new ProgressTextFormatter() {
            @Override
            public CharSequence provideFormattedText(float v) {
                return "00:00";
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

        // fragment 안에서는 this.getActivity()를 호출해야함.
        SharedPreferences preferences = this.getActivity().getSharedPreferences("task", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        for(int i = 0; i < tasks.size(); i++){

            // 데이터를 처음 넣을 때
            if(preferences.getString("keys", null) == null){
                editor.putString("keys", tasks.get(i).getKey());
            }
            // 이미 데이터가 있을 때
            else{
                String value = preferences.getString("keys", null);
                StringBuffer stringBuffer = new StringBuffer(value);
                stringBuffer.append("★" + tasks.get(i).getKey());

                editor.putString("keys", stringBuffer.toString());
            }

            //TaskDTO의 key값을 쉐어드 key값으로 하고
            //value값을 "할일내용 + 시작시간 + 끝낸시간 +  걸린시간"으로 저장

            String value = tasks.get(i).getTaskName() + "★"     // 할일 내용
                            + tasks.get(i).getStartTime() + "★" // 시작 시간
                            + tasks.get(i).getEndTime() + "★" // 끝낸 시간
                            + tasks.get(i).getDurationTime();

            editor.putString(tasks.get(i).getKey(), value);

            editor.apply();

        }

        //모두 저장후 리스트 비우기
        tasks.clear();

    }

    // 초기화 다이얼로그 보여주기
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("초기화");
        builder.setMessage("시간을 기록하시겠습니까?");
        // 아니요
        builder.setPositiveButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetMode();
                    }
                });
        // 예
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

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
        // pieVIew 0으로 하기
        pieView.setProgress(0, true);
        // 원형 타이머 안에 텍스트 00:00으로 하기
        setPieViewZero();
        // 현재시간 0으로 하기
        currentTime = 0;
        // 카운트 다운 타이머 끄시
        countDownTimer.cancel();
    }

}
