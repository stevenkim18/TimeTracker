package com.example.timetracker.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.timetracker.BottomNavigationHelper;
import com.example.timetracker.R;
import com.example.timetracker.DTO.TaskDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    //로그를 위한 액티비티 이름 가지고 오기
    private final String tag = this.getClass().getSimpleName();

    Toolbar toolbar;
    WeekView weekView;
    BottomNavigationView bottomNavigationView;

    ArrayList<WeekViewEvent> events;

    ArrayList<TaskDTO> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        toolbar = findViewById(R.id.toolbar);
        weekView = findViewById(R.id.weekView);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        setToolbar();

        getDataFromSharedPreferences();

        Log.v(tag, "이벤트 개수 = " + events.size()+"");

        weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {

                // Return only the events that matches newYear and newMonth.
                List<WeekViewEvent> matchedEvents = new ArrayList<>();
                for (WeekViewEvent event : events) {
                    if (eventMatches(event, newYear, newMonth)) {
                        matchedEvents.add(event);
                    }
                }
                return matchedEvents;
            }
        });

        weekView.notifyDatasetChanged();

        setBottomNavigationView();

        // 사용자가 설정 한 값으로 달력 화면에 일 수 설정
        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);
        weekView.setNumberOfVisibleDays(preferences.getInt("settingNumOfDays", 1)); //기본 값은 1일

    }
    //toolbar 세팅하기
    private void setToolbar(){
        //툴바 사용 설정
        setSupportActionBar(toolbar);

    }

    // 메뉴파일과 메뉴 결합시켜주는 메소드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_calendar_menu, menu);
        return true;
    }

    // 툴바 메뉴의 아이콘을 눌렀을 때 실행되는 메소드
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            // 달력보기
            case R.id.menu_calendar:

                Calendar calendar = Calendar.getInstance();

                //달력 다이얼로그 띄우기
                DatePickerDialog datePickerDialog = new DatePickerDialog(CalendarActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);

                        // weekview가 선택한 날짜의 시간표를 보여줌.
                        weekView.goToDate(selectedDate);

                    }
                    // 처음 다이얼로그가 나왔을 때 오늘 날짜 선택해 놓기
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

                // 달력 다이얼로그 보여주기
                datePickerDialog.show();

                return true;
            // 오늘로 가기
            case R.id.menu_today:

                //weekView 시간표가 오늘을 보여줌
                weekView.goToToday();

                return true;
            // 추가하기
            case R.id.menu_add:

                addTestData();

                return true;

                default:
                    return super.onOptionsItemSelected(item);

        }
    }

    //BottomNavigationView 설정하기
    private void setBottomNavigationView(){

        //BottomNavigationView shiftMode를 비활성화 시키기
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_calender); // 4번째 달력 버튼이 활성화 되어 있게 함.

        // BottomNavigationView 아이템 클릭 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    // 그래프
                    case R.id.nav_graph :
                        startActivity(new Intent(CalendarActivity.this, GraphActivity.class));
                        finish();
                        break;
                    // 목록
                    case R.id.nav_list :
                        startActivity(new Intent(CalendarActivity.this, ListActivity.class));
                        finish();
                        break;
                    // 타이머
                    case R.id.nav_timer :
                        startActivity(new Intent(CalendarActivity.this, MainActivity.class));
                        finish();
                        break;
                    // 캘린더
                    case R.id.nav_calender :
                        break;
                    // 설정
                    case R.id.nav_setting :
                        startActivity(new Intent(CalendarActivity.this, SettingActivity.class));
                        finish();
                        break;
                }
                // false 를 하면 BottomNavigationView 버튼이 고정이 되서 움직이지 않음.
                // true 를 해야 버튼이 눌렸을 때 효과 발동
                return true;
            }
        });
    }

    //쉐어드에서 데이터 가지고 오기
    private void getDataFromSharedPreferences(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);

        String taskValues = preferences.getString("tasks", null);

        if(taskValues != null){

            try {
                JSONArray jsonArray = new JSONArray(taskValues);

                for (int i = 0; i < jsonArray.length(); i++){

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setKey(jsonObject.getString("key"));
                    taskDTO.setTaskName(jsonObject.getString("taskName"));
                    taskDTO.setStartTime(jsonObject.getString("startTime"));
                    taskDTO.setEndTime(jsonObject.getString("endTime"));
                    taskDTO.setDurationTime(jsonObject.getString("durationTime"));
                    taskDTO.setCategoryName(jsonObject.getString("categoryName"));
                    taskDTO.setCategoryColor(jsonObject.getInt("categoryColor"));

                    tasks.add(taskDTO);

                }

            }catch (JSONException e){
                e.printStackTrace();
            }

            for(int i = 0; i< tasks.size(); i++){
                //이벤트 객체에 넣기
                //이벤트 객체 만들기
                WeekViewEvent event = new WeekViewEvent();

                //이벤트 내용
                event.setName(tasks.get(i).getTaskName());

                //이벤트 시작 시간
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = null;
                try {
                    date = simpleDateFormat.parse(tasks.get(i).getStartTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar startTime = Calendar.getInstance();
                startTime.setTime(date);

                event.setStartTime(startTime);
                Log.v("이벤트", "시작 시간" + event.getStartTime().getTime().toString());

                //이벤트 끝난 시간
                try {
                    date = simpleDateFormat.parse(tasks.get(i).getEndTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar endTime = Calendar.getInstance();
                endTime.setTime(date);

                event.setEndTime(endTime);

                // 이벤트 색(카테고리 색)
                event.setColor(tasks.get(i).getCategoryColor());

                //이벤트 리스트에 추가
                events.add(event);

            }
        }

    }

    //일정 추가하기
    private void addEvents(){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar startTime = Calendar.getInstance();
        startTime.set(2019, 8 - 1 ,28, 14, 10, 10);
        Log.v("시간", format.format(startTime.getTime()));

        Calendar endTime = Calendar.getInstance();
        endTime.set(2019, 8 - 1 ,28, 15,50, 50);
        Log.v("시간", format.format(endTime.getTime()));

        WeekViewEvent event = new WeekViewEvent(1, "타이머 구현", startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorAccent, null));
        events.add(event);

        Calendar startTime1 = Calendar.getInstance();
        startTime1.set(2019, 8 - 1 ,28, 16, 10, 10);
        Log.v("시간", format.format(startTime.getTime()));

        Calendar endTime1 = Calendar.getInstance();
        endTime1.set(2019, 8 - 1 ,28, 16,30, 50);
        Log.v("시간", format.format(endTime.getTime()));

        WeekViewEvent event1 = new WeekViewEvent(2, "달력 기능 구현", startTime1, endTime1);
        event1.setColor(getResources().getColor(R.color.green, null));
        events.add(event1);

    }

    //년과 월이 일치한 이벤트만 출력하기
    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    // 시연용 데이터 넣기
    private void addTestData(){

        // 이벤트 내용
        final String[] eventName = new String[1];

        Calendar calendar = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance();    // 시작시간
        Calendar endTime = Calendar.getInstance();      // 끝낸시간

        // 이벤트 내용을 적을 editText
        final EditText editText = new EditText(this);

        // EditText 다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("테스트용 일정 추가");
        builder.setMessage("일정내용 입력");
        builder.setView(editText);
        builder.setNegativeButton("다음", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // editText에 있는 이벤트 내용을 저장
                eventName[0] = editText.getText().toString();

                // 시작 시간
                // 달력 다이얼로그
                DatePickerDialog datePickerDialog = new DatePickerDialog(CalendarActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // 시작시간 년 월 일 저장
                        startTime.set(year, month, dayOfMonth);

                        // 시간 다이얼로그
                        TimePickerDialog timePickerDialog = new TimePickerDialog(CalendarActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // 시작시간 시 저장
                                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                // 분 저장
                                startTime.set(Calendar.MINUTE, minute);

                                // 끝낸 시간
                                // 달력 다이얼로그
                                DatePickerDialog datePickerDialog1 = new DatePickerDialog(CalendarActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        // 끝낸 시간 년 월 일 저장
                                        endTime.set(year, month, dayOfMonth);

                                        // 시간 다이얼로그
                                        TimePickerDialog timePickerDialog1 = new TimePickerDialog(CalendarActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                // 끝낸 시간 시 저장
                                                endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                // 끝낸 시간 분 저장
                                                endTime.set(Calendar.MINUTE, minute);

                                                // 데이터 저장
                                                WeekViewEvent event = new WeekViewEvent(1, eventName[0], startTime, endTime);

                                                // 이벤트 리스트에 추가
                                                events.add(event);

                                                // weekView 에 갱신
                                                weekView.notifyDatasetChanged();

                                            }
                                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MONTH),true);

                                        timePickerDialog1.show();
                                    }
                                },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

                                datePickerDialog1.show();

                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MONTH),true);

                        timePickerDialog.show();
                    }
                },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

                datePickerDialog.show();

            }
        });
        builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }

}
