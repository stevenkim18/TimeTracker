package com.example.timetracker.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.timetracker.R;
import com.example.timetracker.DTO.TaskDTO;
import com.example.timetracker.adapter.ImageSliderAdapter;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.TimePicker;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TaskDetailActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView textViewTaskName, textViewCategory, textViewStartTime, textViewEndTime, textViewDurationTime;
    LinearLayout viewStartTime, viewEndTime;
    ImageButton imageButtonWriteMemo, imageButtonAddPhoto;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;

    Calendar startTime = Calendar.getInstance(),
            endTime = Calendar.getInstance();

    // 할 일 객체 변수
    TaskDTO taskDTO;

    // 목록화면에서 인텐트로 받은 아이템 위치를 저장하기 위한 변수
    int position;

    ArrayList<Image> images = new ArrayList<>();
    ImageSliderAdapter imageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        toolbar = findViewById(R.id.toolbar);                           // 툴바
        textViewTaskName = findViewById(R.id.textViewTaskName);         // 한 일 내용
        textViewCategory = findViewById(R.id.textViewCategory);         // 카테고리
        textViewStartTime = findViewById(R.id.textViewStartTime);       // 시작 시간
        textViewEndTime = findViewById(R.id.textViewEndTime);           // 끝낸 시간
        textViewDurationTime = findViewById(R.id.textViewDurationTime); // 걸린 시간
        imageButtonWriteMemo = findViewById(R.id.imageButtonWriteMemo); // 메모 추가
        imageButtonAddPhoto = findViewById(R.id.imageButtonAddPhoto);   // 사진 추가
        viewPager = findViewById(R.id.viewPagerImage);                  // 사진 뷰페이져
        wormDotsIndicator = findViewById(R.id.worm_dots_indicator);     // 사진 점 지시자(dot indicator)

        viewStartTime = findViewById(R.id.viewStartTime);   // 시작 시간 리니어 레이아웃 --> 클릭시 시간 수정 다이얼로그 띄어줌
        viewEndTime = findViewById(R.id.viewEndTime);       // 끝낸 시간 리니어 레이아웃 --> 클릭시 시간 수정 다이얼로그 띄어줌.

        setToolbar();

        getDataFromIntentAndSendToTextView();

        setEditTIme();

        setImageButtonAddPhoto();

        getDataFromSharedPreference();

        setViewPager();

    }

    // 액티비티가 onDestroy 될 때 이미지 리스트에 있는 값을 쉐어드에 저장
    @Override
    protected void onDestroy() {

        saveDataIntoSharedPreference();

        super.onDestroy();
    }

    // 툴바 세팅
    private void setToolbar(){

        //툴바 사용 설정
        setSupportActionBar(toolbar);

        // 툴바 왼쪽 버튼 활성화
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 툴바 왼쪽 버튼 이미지
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chevron_left_black_24dp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            // 뒤로가기
            case android.R.id.home:
                finish();
                return true;
            // 삭제하기
            case R.id.menu_delete:

                // 사용자에게 한 번 더 확인 다이얼로그 띄우기
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
                deleteBuilder.setTitle("삭제");
                deleteBuilder.setMessage("기록을 삭제하시겠습니까?");
                deleteBuilder.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent deleteIntent = new Intent();
                        deleteIntent.putExtra("taskPosition", position);
                        setResult(RESULT_CANCELED, deleteIntent);
                        finish();

                    }
                });
                deleteBuilder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                deleteBuilder.show();
                return true;
            // 수정한 내용 저장하기
            case R.id.menu_save:
                // 사용자에게 한 번 더 확인 다이얼로그 띄우기
                AlertDialog.Builder saveBuilder = new AlertDialog.Builder(this);
                saveBuilder.setTitle("저장");
                saveBuilder.setMessage("현재 기록을 저장하시겠습니까?");
                saveBuilder.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 한 일 내용
                        taskDTO.setTaskName(textViewTaskName.getText().toString());
                        // 시작 시간
                        taskDTO.setStartTime(textViewStartTime.getText().toString());
                        // 종료 시간
                        taskDTO.setEndTime(textViewEndTime.getText().toString());
                        // 걸린 시간
                        taskDTO.setDurationTime(textViewDurationTime.getText().toString());

                        Intent saveIntent = new Intent();
                        saveIntent.putExtra("taskPosition", position);
                        saveIntent.putExtra("taskDTO", taskDTO);
                        setResult(RESULT_OK, saveIntent);
                        finish();

                    }
                });
                saveBuilder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                saveBuilder.show();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    // 인텐트에서 받아온 값을 TextView에 보여주기
    private void getDataFromIntentAndSendToTextView(){

        taskDTO = (TaskDTO) getIntent().getSerializableExtra("taskDTO");
        // 아이템 위치 저장
        position = getIntent().getIntExtra("taskPosition", 0);

        // 한 일 내용
        textViewTaskName.setText(taskDTO.getTaskName());
        // 카테고리
        textViewCategory.setText(taskDTO.getCategoryName());
        textViewCategory.setTextColor(taskDTO.getCategoryColor());
        // 시작 시간
        textViewStartTime.setText(taskDTO.getStartTime());
        // 끝낸 시간
        textViewEndTime.setText(taskDTO.getEndTime());
        // 걸린 시간
        textViewDurationTime.setText(taskDTO.getDurationTime());

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;

        // 시작 시간 String --> Calendar
        try {
            date = format.parse(taskDTO.getStartTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        startTime.setTime(date);

        // 끝낸 시간 String --> Calendar
        try {
            date = format.parse(taskDTO.getEndTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        endTime.setTime(date);

    }

    // 시작시간과 끝낸시간을 클릭 수정이 가능 하게 세팅
    private void setEditTIme(){

        // 시작 시간 영역 눌렀을 떄
        viewStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 달력 다이얼로그
                DatePickerDialog datePickerDialog = new DatePickerDialog(TaskDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        //시간 다이얼로그
                        MyTimePickerDialog timePickerDialog = new MyTimePickerDialog(TaskDetailActivity.this, new MyTimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                                startTime.set(year, month, dayOfMonth, hourOfDay, minute, seconds);

                                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                                // 사용자가 수정한 시간이 시작 시간이 끝낸 시간 보다 빠를 때
                                if(compareTime(startTime, endTime)){
                                    //바뀐 값을 객체에 저장

                                    String editedStartTime = format.format(startTime.getTime());

                                    // 시작 시간 TextView에 변경된 값을 넣어줌.
                                    textViewStartTime.setText(editedStartTime);

                                    // 변경된 걸린 시간을 넣어줌.
                                    textViewDurationTime.setText(subtractTIme(startTime, endTime));
                                }
                                // 사용자가 잘못 설정 했을 때
                                else {

                                    Date date = null;

                                    try {
                                        date = format.parse(taskDTO.getStartTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    // 할 일 객체에 저장 되있던 시작 시간 저장
                                    startTime.setTime(date);

                                    Toast.makeText(getApplicationContext(),"시작 시간이 종료 시간 보다 느립니다", Toast.LENGTH_LONG).show();

                                }

                            }
                        },startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND), true);
                        // 버튼 이름을 영어에서 한글로 바꾸기
                        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소", timePickerDialog);
                        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "확인", timePickerDialog);
                        timePickerDialog.show();


                    }
                    // 달력 다이얼로그를 띄울 때
                    // 사용자가 설정한 시간으로 달력 날짜가 초기에 설정 됨,
                },startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();
            }

        });

        // 종료 시간 영역 눌렀을 떄
        viewEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 달력 다이얼로그
                DatePickerDialog datePickerDialog = new DatePickerDialog(TaskDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        //시간 다이얼로그
                        MyTimePickerDialog timePickerDialog = new MyTimePickerDialog(TaskDetailActivity.this, new MyTimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                                endTime.set(year, month, dayOfMonth, hourOfDay, minute, seconds);

                                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                                // 사용자가 수정한 시간이 시작 시간이 끝낸 시간 보다 빠를 때
                                if(compareTime(startTime, endTime)){
                                    //바뀐 값을 객체에 저장

                                    String editedEndTime = format.format(endTime.getTime());

                                    // 시작 시간 TextView에 변경된 값을 넣어줌.
                                    textViewEndTime.setText(editedEndTime);

                                    // 변경된 걸린 시간을 넣어줌.
                                    textViewDurationTime.setText(subtractTIme(startTime, endTime));
                                }
                                // 사용자가 잘못 설정 했을 때
                                else {

                                    Date date = null;

                                    try {
                                        date = format.parse(taskDTO.getEndTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    // 할 일 객체에 저장 되있던 시작 시간 저장
                                    endTime.setTime(date);

                                    Toast.makeText(getApplicationContext(),"종료 시간이 시작 시간보다 빠릅니다!", Toast.LENGTH_LONG).show();

                                }

                            }
                        },endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), endTime.get(Calendar.SECOND), true);
                        // 버튼 이름을 영어에서 한글로 바꾸기
                        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소", timePickerDialog);
                        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "확인", timePickerDialog);
                        timePickerDialog.show();

                    }
                    // 달력 다이얼로그를 띄울 때
                    // 사용자가 설정한 시간으로 달력 날짜가 초기에 설정 됨,
                },endTime.get(Calendar.YEAR), endTime.get(Calendar.MONTH), endTime.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();
            }

        });

    }

    // 수정된 걸린 시간을 위해서 메소드 생성
    // 끝낸 시간에서 시작 시간 빼기
    private String subtractTIme(Calendar startTime, Calendar endTime){

        Date startDate = new Date(startTime.getTimeInMillis());
        Date endDate = new Date(endTime.getTimeInMillis());

        // 밀리초 단위까지 나옴으로 1000을 나눠줌.
        long diff = (endDate.getTime() - startDate.getTime()) / 1000;
        // ex) 2000초 / 60 = 33 나머지 20 --> 33분 20초
        int diffMinute = (int) (diff / 60);
        int diffSecond = (int) (diff % 60);

        return diffMinute + "분 " + diffSecond + "초";

    }

    // 시간을 비교하는 메소드
    // 시간을 수정 할때 시작 시간 < 끝낸 시간이 되어야 하기 때문에
    private boolean compareTime(Calendar startTime, Calendar endTime){

        Date startDate = new Date(startTime.getTimeInMillis());
        Date endDate = new Date(endTime.getTimeInMillis());

        if(endDate.getTime() > startDate.getTime()){
            return true;
        }

        else {
            return false;
        }

    }

    //사진 추가 버튼 설정하기
    private void setImageButtonAddPhoto(){

        imageButtonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence photoMode[] = new CharSequence[]{"카메라", "사진첩"};

                AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetailActivity.this);
                builder.setTitle("사진 추가(최대 10장)");
                builder.setItems(photoMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            //카메라
                            case 0:
                                ImagePicker.cameraOnly()
                                        .start(TaskDetailActivity.this, 51);

                                break;

                            //사진첩
                            case 1:
                                ImagePicker.create(TaskDetailActivity.this)
                                        .folderMode(false)   // 폴더로 사진첩을 보여줌
                                        .limit(10)          // 최대 10장으로 제한
                                        .showCamera(false)  // 카메라 버튼 클릭 못함
                                        .start(50);

                                break;
                        }

                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(ImagePicker.getImages(data) != null){
            for(int i = 0; i < ImagePicker.getImages(data).size(); i++){
                images.add(ImagePicker.getImages(data).get(i));
                Log.v("받은 이미지", "id = " + ImagePicker.getImages(data).get(i).getId() + "");
                Log.v("받은 이미지", "name = " + ImagePicker.getImages(data).get(i).getName());
                Log.v("받은 이미지", "path = " + ImagePicker.getImages(data).get(i).getPath());
            }

            Log.v("상세보기 화면", "선택된 이미지 갯수 = " + images.size() + "");

            imageSliderAdapter.notifyDataSetChanged();

            Log.v("상세보기 화면", "어뎁터의 아이템 갯수 = " + imageSliderAdapter.getCount() + "");

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 뷰페이저 세팅하기
    private void setViewPager(){

        imageSliderAdapter = new ImageSliderAdapter(this, images);
        // 뷰페이져 어뎁터 설정
        viewPager.setAdapter(imageSliderAdapter);
        // 점 지시사 설정
        wormDotsIndicator.setViewPager(viewPager);

    }

    // 사진이나 메모 데이터 쉐어드에 저장하기
    private void saveDataIntoSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String values = null;

        try{
            JSONArray jsonArray = new JSONArray();
            for(int i = 0; i < images.size(); i++){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", images.get(i).getId());
                jsonObject.put("name", images.get(i).getName());
                jsonObject.put("path", images.get(i).getPath());

                jsonArray.put(jsonObject);
            }

            values = jsonArray.toString();

        }catch (JSONException e){
            e.printStackTrace();
        }

        editor.putString(taskDTO.getKey() + "images", values);
        editor.apply();

    }

    // 사진이나 메모 데이터 쉐어드에서 가지고 오기
    private void getDataFromSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);

        String valueImages = preferences.getString(taskDTO.getKey()+"images", null);

        // 이미지가 있는 경우만 실행
        if(valueImages != null){

            try {
                JSONArray jsonArray = new JSONArray(valueImages);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    long id = Long.parseLong(jsonObject.getString("id"));
                    String name = jsonObject.getString("name");
                    String path = jsonObject.getString("path");

                    Image image = new Image(id, name, path);

                    images.add(image);

                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}








