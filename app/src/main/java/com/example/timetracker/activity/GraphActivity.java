package com.example.timetracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.timetracker.BottomNavigationHelper;
import com.example.timetracker.DTO.ChartDataDTO;
import com.example.timetracker.DTO.TaskDTO;
import com.example.timetracker.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.wisnu.datetimerangepickerandroid.CalendarPickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GraphActivity extends AppCompatActivity {

//    public static final String ASSET_PATH = "file:///android_asset/";
    public static final String TAG = "그래프 액티비티";

    Toolbar toolbar;
//    WebView webView;
    BarChart barChart;
    BottomNavigationView bottomNavigationView;

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // 한 일들 담는 리스트
    ArrayList<TaskDTO> tasks = new ArrayList<>();

    // 사용자가 지정한 한 일들을 담는 리스트
    ArrayList<TaskDTO> selectedTasks = new ArrayList<>();

    // 달력 피커 다이얼로그에서 선택 날짜를 담는 리스트
    ArrayList<Date> selectedDates = null;

    // 날짜와 카테고리 별로 분류 된 데이터를 담는 리스트
    ArrayList<ChartDataDTO> chartDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        toolbar = findViewById(R.id.toolbar);
        barChart = findViewById(R.id.barChart);
//        webView = findViewById(R.id.webView);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        setToolbar();
        setBottomNavigationView();

        getDataFromSharedPreference();

        // 바 차트에 데이터가 없을 때
        barChart.setNoDataText("날짜를 선택해주세요!");
        barChart.setNoDataTextColor(Color.RED);
//        ArrayList<BarEntry> barEntries = new ArrayList<>();
//
//        barEntries.add(new BarEntry(0, 1));
//        barEntries.add(new BarEntry(1, 4));
//        barEntries.add(new BarEntry(2, 3));
//        barEntries.add(new BarEntry(3, 6));
//        barEntries.add(new BarEntry(4, 2));
//        barEntries.add(new BarEntry(5, 5));
//
//        BarDataSet barDataSet = new BarDataSet(barEntries, "안드로이드 2주차");
//
//        BarData barData = new BarData(barDataSet);
//
//        ArrayList<String> dates = new ArrayList<>();
//        dates.add("8/14");
//        dates.add("8/15");
//        dates.add("8/16");
//        dates.add("8/17");
//        dates.add("8/18");
//        dates.add("8/19");
//
//        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
//
//        barChart.setData(barData);

    }

    //BottomNavigationView 설정하기
    private void setBottomNavigationView(){

        //BottomNavigationView shiftMode를 비활성화 시키기
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_graph); // 4번째 달력 버튼이 활성화 되어 있게 함.

        // BottomNavigationView 아이템 클릭 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    // 그래프
                    case R.id.nav_graph :
                        break;
                    // 목록
                    case R.id.nav_list :
                        startActivity(new Intent(GraphActivity.this, ListActivity.class));
                        finish();
                        break;
                    // 타이머
                    case R.id.nav_timer :
                        startActivity(new Intent(GraphActivity.this, MainActivity.class));
                        finish();
                        break;
                    // 캘린더
                    case R.id.nav_calender :
                        startActivity(new Intent(GraphActivity.this, CalendarActivity.class));
                        finish();
                        break;
                    // 설정
                    case R.id.nav_setting :
                        startActivity(new Intent(GraphActivity.this, SettingActivity.class));
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
        getMenuInflater().inflate(R.menu.toolbar_graph_menu, menu);
        return true;
    }

    // 툴바 메뉴 기능 추가
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // 카테고리
            case R.id.menu_calendar :

                showCalendarPickerDialog();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //캘린더 피커 다이얼로그 띄우기
    private void showCalendarPickerDialog(){

        // 다이얼로그 객체 만들기
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // inflate --> 부풀리다.
        // xml의 뷰들을 실제로 부풀리는, 실제로 만드는 역할
        LayoutInflater inflater = getLayoutInflater();
        // 만들어 놓은 다이얼로그 xml 파일을 view 객체 부풀려줌. 넣어줌.
        View view = inflater.inflate(R.layout.calendar_range_picker_dialog, null);
        // 다이얼로그에 이 뷰를 넣어줌.
        builder.setView(view);

        final CalendarPickerView calendarPickerView = view.findViewById(R.id.calendarPicker);   // 캘린더 피커
        final Button buttonSelect = view.findViewById(R.id.buttonSelect);                       // 선택 버튼
        final Button buttonCancel = view.findViewById(R.id.buttonCancel);                       // 취소 버튼

        final AlertDialog calendarDialog = builder.create();

        Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -1);
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DAY_OF_MONTH, 1);

        // 날짜 선택 가능 범위 지정
        calendarPickerView.init(lastYear.getTime(), today.getTime())     // 오늘부터 작년부터 내년까지
                .withSelectedDate(Calendar.getInstance().getTime())         // 오늘 날짜로 선택
                .inMode(CalendarPickerView.SelectionMode.RANGE);            // 범위 선택 모드로 설정


        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {

            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        // 선택 버튼 리스너
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDates = (ArrayList<Date>) calendarPickerView.getSelectedDates();

                Log.v(TAG, "선택된 날짜 일수 : " + selectedDates.size());
                Log.v(TAG, "선택 된 시작 날짜 : " + selectedDates.get(0).getTime());
                Log.v(TAG, "선택 된 종료 날짜 : " + selectedDates.get(selectedDates.size()-1).getTime());

                // 사용자가 선택 한 날짜대로 bar 차트 그리기
                setBarChart(selectedDates);

                calendarDialog.dismiss();
            }
        });
        // 취소 버튼 리스너
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 겔린더 피커 다이얼로그 끄기
                calendarDialog.dismiss();
            }
        });


        // 다이얼로그 보이기
        calendarDialog.show();


    }

    //쉐어드에서 데이터 가지고 오기
    private void getDataFromSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);

        String values = preferences.getString("tasks", null);

        // 데이터가 없을 때는 방지하기 위해
        if(values != null){
            try{
                JSONArray jsonArray = new JSONArray(values);

                for(int i = 0; i < jsonArray.length(); i++){

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
                    Log.v(TAG, "가지고 온 데이터 - 내용: " + taskDTO.getTaskName() + " 카테고리: " + taskDTO.getCategoryName() + " 걸린시간: " + taskDTO.getDurationTime());

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        Log.v(TAG, "쉐어드에서 가져온 한 일 개수 : " + tasks.size());

    }

    // 오늘의 데이터 가지고 오기
    private void setBarChart(ArrayList<Date> selectedDates){

        // 사용자가 선택한 날짜로 필터하기
        filterTaskByDate(selectedDates);

        // 사용가 선택한 날짜의 객체들을 카테고리 별로 분류하기
        classifyByCategory();

        drawChart();

    }

    // 두 날짜 비교 하기
    private boolean compareDate(Date taskDate, Date selectedDate){ // 한 일 객체의 날짜와 선택 된 날짜 비교

        // 한 일 날짜
        Calendar taskCalendar = Calendar.getInstance();
        taskCalendar.setTime(taskDate);
        int taskYear = taskCalendar.get(Calendar.YEAR);
        int taskMonth = taskCalendar.get(Calendar.MONTH);
        int taskDay = taskCalendar.get(Calendar.DAY_OF_MONTH);

        // 사용자가 선택한 날짜
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);
        int selectedYear = selectedCalendar.get(Calendar.YEAR);
        int selectedMonth = selectedCalendar.get(Calendar.MONTH);
        int selectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        boolean comparison = (taskYear == selectedYear && taskMonth == selectedMonth && taskDay == selectedDay);

        // 비교하는 날짜의 년 월 일 모두 같은 true 하나라도 틀리면 false;
        return comparison;

    }

    // 사용자가 선택한 날짜의 한 일만 걸러내기
    private void filterTaskByDate(ArrayList<Date> selectedDates){

        // 불러온 한 일 데이터들의 날짜와 매개변수의 날짜가 같은 것만 선택 된 할 일 리스트에 저장
        for(TaskDTO task : tasks){

            // taskDTO 객체에 string으로 있는 날짜 데이터를 date로 넣어주기 위해 만든 변수
            Date taskEndTime = stringToDate(task.getEndTime());
//            try {
//                // string --> Date 포맷 변경
//                taskEndTime = format.parse(task.getEndTime());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            // 선택한 날짜의 범위와 한 일의 날짜 비교
           for(Date date : selectedDates){

                // 한 일 객체 날짜와 선택한 날짜 비교
                if(compareDate(taskEndTime, date)){

                    // 같으면 selectedTasks 리스트에 task 추가
                    selectedTasks.add(task);

                    Log.v(TAG, "선택된 할 일 객체: 이름: " + task.getTaskName() + " 카테고리: " + task.getCategoryName() + " 걸린시간: " + task.getDurationTime());
                }

            }

        }
        Log.v(TAG, "선택 된 할일 객체 개수 : " + selectedTasks.size());

    }

    // 카테고리 별로 분류 하기
    private void classifyByCategory(){

        // 처음 비교의 원본이 되는 한 일 객체
        TaskDTO firstTask = null;
        // 비교를 당하는 한 일 객체
        TaskDTO comparedTask = null;

        int categoryTime = 0;

        Loop:
        for (int i = 0; i < selectedTasks.size(); i++) {
            // 맨 처음에 있는 한 일 객체를 원본 객체에 넣음.
            if (firstTask == null) {
                // 맨 처음 한 일을 분류된 리스트에서 지우고 원본 객체에 저장
                firstTask = selectedTasks.remove(0);

                // 카테고리 시간을 더함.
                categoryTime += stringToSecond(firstTask.getDurationTime());
            }

            // 그 다음 객체를 비교 당하는 객체에 넣기
                comparedTask = selectedTasks.get(i);

            // 원본 객체와 비교 당하는 객체의 끝나는 시간을 비교
            if(compareDate(stringToDate(firstTask.getEndTime()),stringToDate(comparedTask.getEndTime()))){

                // 카테고리까지 같을 경우
                if(firstTask.getCategoryName().equals(comparedTask.getCategoryName())){

                    // 카테고리 시간을 더함.
                    categoryTime += stringToSecond(comparedTask.getDurationTime());

                    // 리스트에 비교된 한 일 객체 지움.
                    selectedTasks.remove(i);

                    i -= 1;

                }

            }
            //같은 날짜가 아니면
            else {
                // 분류가 완료 되었음. --> 차트 데이터 리스트에 넣음.
                ChartDataDTO chartData = new ChartDataDTO();
                chartData.setCategoryName(firstTask.getCategoryName());         // 카테고리 이름 저장
                chartData.setDate(stringToDate(firstTask.getEndTime()));        // Date 형태로 날짜 저장
                chartData.setTextDate(stringToMMDD(firstTask.getEndTime()));  // 월과 일 형식으로 저장 //예) 8/14,  12/25....
                chartData.setDurationTime(categoryTime);                        // 카테고리 별 시간 저장
                chartData.setColor(firstTask.getCategoryColor());               // 카테고리 색

                Log.v(TAG, "카테고리 별로 분류된 차트 데이터 - 날짜: " + chartData.getTextDate() + " 카테고리: " + chartData.getCategoryName() + " 총 걸린시간 : " + chartData.getDurationTime() + "초");

                //리스트에 넣음
                chartDatas.add(chartData);

                // 카테고리 시간을 담는 변수 초기화
                categoryTime = 0;

                // 원본 한일 객체 초기화
                firstTask = null;

                // 다시 위에 for 문으로 이동.
                i = -1;

            }
            // 선택된 데이터들이 마지막 날만 남았을 때
            // 모든 것을 다 비교하고 차트 데이터 리스트에 넣기 위해 선언
            if (selectedTasks.size() - 1 == i) {

                // 분류가 완료 되었음. --> 차트 데이터 리스트에 넣음.
                ChartDataDTO chartData = new ChartDataDTO();
                chartData.setCategoryName(firstTask.getCategoryName());         // 카테고리 이름 저장
                chartData.setDate(stringToDate(firstTask.getEndTime()));        // Date 형태로 날짜 저장
                chartData.setTextDate(stringToMMDD(firstTask.getEndTime()));  // 월과 일 형식으로 저장 //예) 8/14,  12/25....
                chartData.setDurationTime(categoryTime);                        // 카테고리 별 시간 저장
                chartData.setColor(firstTask.getCategoryColor());               // 카테고리 색

                Log.v(TAG, "카테고리 별로 분류된 차트 데이터 - 날짜: " + chartData.getTextDate() + " 카테고리: " + chartData.getCategoryName() + " 총 걸린시간 : " + chartData.getDurationTime() + "초");

                //리스트에 넣음
                chartDatas.add(chartData);

                // 카테고리 시간을 담는 변수 초기화
                categoryTime = 0;

                // 원본 한일 객체 초기화
                firstTask = null;

                // 다시 위에 for 문으로 이동.
                i = -1;

            }



        }


    }

    // 차트 그리기
    private void drawChart(){

        //텍스트 날짜를 넣을 리스트 [8/14, 8/15, 8/16...]
        ArrayList<String> dates = new ArrayList<>();

        // 카테고리를 넣을 리스트
        ArrayList<String> categorys = new ArrayList<>();

        // 색 리스트
        ArrayList<Integer> colors = new ArrayList<>();

        // 바 차트에 넣을 데이터 리스트 만들기
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // 맨 처음 날짜 데이터를 리스트에 넣기
        dates.add(chartDatas.get(0).getTextDate());

        // 맨 처음 카테고리 이름 데이터를 리스트에 넣기
        categorys.add(chartDatas.get(0).getCategoryName());

        // X축에 넣을 텍스트 날짜 데이터 넣기
        for(int i = 0; i < chartDatas.size(); i++){

            int dateCount = 0;
            int categoryCount = 0;

            // 차트 데이터의 날짜와 날짜 리스트 넣은 데이터 비교
            for(int j = 0; j< dates.size(); j++){

                // 날짜가 다르면 1증가
                if(!chartDatas.get(i).getTextDate().equals(dates.get(j))){

                    dateCount++;

                }

            }
            //차트 데이터의 카테고리 이름과 카테고리 리스트 넣은 데이터 비교
            for(int j = 0; j < categorys.size(); j++){

                // 카테고리가 다르면 1증가
                if(!chartDatas.get(i).getCategoryName().equals(categorys.get(j))){

                    categoryCount++;

                }

            }

            // 날짜 데이터가 모든 것이 중복되지 않으면 날짜 리스트에 해당 날짜를 넣음.
            if(dates.size() == dateCount){

                dates.add(chartDatas.get(i).getTextDate());

            }

            // 카테고리 테이터가 모든 것이 중복되지 않으면 카테고리 리스트에 해당 카테고리 이름을 넣음.
            if(categorys.size() == categoryCount){

                categorys.add(chartDatas.get(i).getCategoryName());

            }

        }

        // 바 차트에 들어갈 barEntry 객체 만들기
        // 날짜
        for(int x = 0; x < dates.size(); x++){

            // y좌표 리스트
            ArrayList<Float> yAxis = new ArrayList<>();

            //카테고리
            Category:
            for (int y = 0; y < categorys.size(); y++) {

                // 차트 데이터들
                for (int z = 0; z < chartDatas.size(); z++) {

                    // 날짜가 같으면
                    if (chartDatas.get(z).getTextDate().equals(dates.get(x))) {

                        // 카테고리가 같으면
                        if (chartDatas.get(z).getCategoryName().equals(categorys.get(y))) {

                            // 차트 데이터 객체에 걸린 시간을 y좌표 리스트에 넣음
                            yAxis.add((float) chartDatas.get(z).getDurationTime());

                            // 색 넣음.
                            colors.add(chartDatas.get(z).getColor());

                            // 한 번 사용된 차트 데이터는 삭제
                            chartDatas.remove(z);

                            break;
                        }

                    }
                    // 날짜가 다르면
                    else{

                        if(z < categorys.size()){

                            // y좌표 0
                            yAxis.add((float) 0);
                            // 색 0
                            colors.add(0);

                            break;
                        }

                    }

                }   // 차트 데이터들


            }   // 카테고리

            // 차트 데이터 추가
            barEntries.add(new BarEntry(x, listToFloatArray(yAxis)));

        } // 날짜


        BarDataSet barDataSet = new BarDataSet(barEntries, null);
        // 카테고리
        barDataSet.setStackLabels(listToStringArray(categorys));

        // 막대 색 설정
        barDataSet.setColors(listToIntArray(colors));

        BarData barData = new BarData(barDataSet);

        // 막대기 위에 y값을 나타내기 위함.
        barData.setValueFormatter(new MyValueFormatter());
        // 막대기 위 글씨 크기
        barData.setValueTextSize(15);

        // 바 차트 x축 라벨을 사용자가 선택한 날짜로!
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));

        barChart.setData(barData);

        barChart.setFitBars(true);

        setBarChartAxis();

        // 바차트 갱신
        barChart.invalidate();


    }

    // String 에서 Date 로 바꾸기
    private Date stringToDate(String date){

        Date resultDate = null;
        try {
            resultDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultDate;
    }

    // String 에서 second 로 바꾸기
    // 걸린 시간을 초 형태로 바꾸기 위해서
    private int stringToSecond(String time){

        String[] tempMinute = time.split("분");
        int minute = Integer.parseInt(tempMinute[0]);

        String[] tempSecond = tempMinute[1].trim().split("초");
        int second = Integer.parseInt(tempSecond[0]);

        return (minute * 60) + second;
    }

    // 웹뷰보다 mpchart가 데이어터 많아서 바꾸기로 함.
//    //웹뷰 세팅하기
//    private void setWebView(){
//
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setBuiltInZoomControls(true);
//        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        webView.setWebViewClient(new WebViewClient());
//
//    }
//
//    // 차트 불러오기
//    private void loadChart(){
//
//        String content = null;
//
//        try{
//            AssetManager assetManager = getAssets();
//            InputStream in = assetManager.open("piechart.html");
//            byte[] bytes = readFully(in);
//            content = new String(bytes, "UTF-8");
//        }catch (IOException e){
//
//        }
//
//        String formattedContent = String.format(content);
//        webView.loadDataWithBaseURL(ASSET_PATH, formattedContent, "text/html", "utf-8", null);
//        webView.requestFocusFromTouch();
//
//    }
//
//
//    private static byte[] readFully(InputStream in) throws IOException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        for (int count; (count = in.read(buffer)) != -1;) {
//            out.write(buffer, 0, count);
//        }
//        return out.toByteArray();
//    }

    // String에서 월/일 형태로 변환
    private String stringToMMDD(String date){

        //예) 2019/08/14 13:23:12 --> split
        // [0] = 2019
        // [1] = 08
        // [2] = 14 13:23:12    --> split

        // [0] = 14
        // [1] = 13:23:12

        String[] splitedDate = date.split("/");
        String[] splitedTime = splitedDate[2].split(" ");

        return splitedDate[1] + "/" + splitedTime[0];
    }

    // float 리스트에서 배열로
    private float[] listToFloatArray(ArrayList<Float> list){

        float[] array = new float[list.size()];

        for(int i = 0; i < list.size(); i++){
            array[i] = list.get(i);
        }

        return array;
    }

    private int[] listToIntArray(ArrayList<Integer> list){

        int[] array = new int[list.size()];

        for(int i = 0; i < list.size(); i++){
            array[i] = list.get(i);
        }

        return array;

    }

    private String[] listToStringArray(ArrayList<String> list){

        String[] array = new String[list.size()];

        for(int i = 0; i < list.size(); i++){
            array[i] = list.get(i);
        }

        return array;
    }

    // 바 차트의 x값과 y값을 원하는대로 바꾸기
    private void setBarChartAxis(){

        // 바 차트 설명 부분 지우기
        barChart.getDescription().setEnabled(false);


        // X축 데이터
        XAxis xAxis = barChart.getXAxis();
        // x축이 0부터 시작하게
//        xAxis.setAxisMinimum(0);

        // y축 왼쪽
        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setValueFormatter(new MyValueFormatter());

        // 바 차트 애니메이션
//        barChart.animate();

    }

    private class MyValueFormatter extends ValueFormatter {

        // y축
        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            // 6개만 보이게 함.
            axis.setLabelCount(6, true);

            // 시 분으로 볼 수 있게 함.
            int hour = (int)value / 3600;

            int minute = (int)(value % 3600) / 60;

            if(value >= 3600){
                return hour + "h " + minute + "m";
            }
            else {
                return minute + "m";
            }

        }

        // 실제 데이터터
        @Override
       public String getBarStackedLabel(float value, BarEntry stackedEntry) {

            int hour = (int)value / 3600;

            int minute = (int)(value % 3600) / 60;

            int second = (int)(value % 3600) % 60;

            if(value >= 3600){
                return hour + "h " + minute + "m " + second + "s";
            }
            else if(value >= 60 && value < 3600){
                return minute + "m " + second + "s";
            }
            else {
                return  second + "s";
            }

        }
    }
}
