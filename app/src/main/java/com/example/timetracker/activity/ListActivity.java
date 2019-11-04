package com.example.timetracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.timetracker.adapter.TaskAdapter;
import com.example.timetracker.BottomNavigationHelper;
import com.example.timetracker.R;
import com.example.timetracker.adapter.SwipeToDeleteCallback;
import com.example.timetracker.DTO.TaskDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerViewTask;
    BottomNavigationView bottomNavigationView;

    // 리싸이클러뷰 어뎁터
    TaskAdapter taskAdapter;

    // 한 일 목록을 담을 리스트
    ArrayList<TaskDTO> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Log.v("ListActivity", "onCreate");
        toolbar = findViewById(R.id.toolbar);
        recyclerViewTask = findViewById(R.id.recyclerViewTask);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        tasks = new ArrayList<>();

        setBottomNavigationView();

//        addTestData();

        getDataFromSharedPreference();

        setRecyclerViewTask();

    }


    //BottomNavigationView 설정하기
    private void setBottomNavigationView(){

        //BottomNavigationView shiftMode를 비활성화 시키기
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_list); // 4번째 달력 버튼이 활성화 되어 있게 함.

        // BottomNavigationView 아이템 클릭 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    // 그래프
                    case R.id.nav_graph :
                        startActivity(new Intent(ListActivity.this, GraphActivity.class));
                        finish();
                        break;
                    // 목록
                    case R.id.nav_list :
                        break;
                    // 타이머
                    case R.id.nav_timer :
                        startActivity(new Intent(ListActivity.this, MainActivity.class));
                        finish();
                        break;
                    // 캘린더
                    case R.id.nav_calender :
                        startActivity(new Intent(ListActivity.this, CalendarActivity.class));
                        finish();
                        break;
                    // 설정
                    case R.id.nav_setting :
                        startActivity(new Intent(ListActivity.this, SettingActivity.class));
                        finish();
                        break;
                }
                // false 를 하면 BottomNavigationView 버튼이 고정이 되서 움직이지 않음.
                // true 를 해야 버튼이 눌렸을 때 효과 발동
                return true;
            }
        });
    }

    //recyclerview 세팅
    private void setRecyclerViewTask(){

        // 리싸이클러뷰 한 줄 목록 형식으로 보여줌(리니어 레이아웃 형태)
        recyclerViewTask.setLayoutManager(new LinearLayoutManager(this));

        // 어뎁터 객체 생성
        taskAdapter = new TaskAdapter(tasks, getApplicationContext());

        //리싸이클러뷰 어뎁터 설정
        recyclerViewTask.setAdapter(taskAdapter);

        //리싸이클러뷰 구분선 넣기
        recyclerViewTask.addItemDecoration(new DividerItemDecoration(ListActivity.this, 1));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(taskAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerViewTask);

        //리싸이클러뷰 아이템 클릭 리스너
        taskAdapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(ListActivity.this, TaskDetailActivity.class);
                intent.putExtra("taskDTO", tasks.get(position));
                intent.putExtra("taskPosition", position);
                // 상세보기 페이지에서 할 일을 수정 하든, 삭제 하든, 아무것도 안하든 다시 액티비티로 돌아오면 결과값을 받기 위해 startActivityForResult 사용
                startActivityForResult(intent, 3000);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 3000){
            switch (resultCode) {
                // 수정
                case RESULT_OK:

                    int editedPosition = data.getIntExtra("taskPosition", 0);
                    TaskDTO editedTask = (TaskDTO) data.getSerializableExtra("taskDTO");

                    // 할 일 내용
                    tasks.get(editedPosition).setTaskName(editedTask.getTaskName());
                    // 시작 시간
                    tasks.get(editedPosition).setStartTime(editedTask.getStartTime());
                    // 종료 시간
                    tasks.get(editedPosition).setEndTime(editedTask.getEndTime());
                    // 걸린 시간
                    tasks.get(editedPosition).setDurationTime(editedTask.getDurationTime());

                    // 수정된 위치만 리스트 어뎁터 갱신
                    taskAdapter.notifyItemChanged(editedPosition);

                    break;
                // 삭제
                case RESULT_CANCELED:

                    if(data != null){
                        int deletePosition = data.getIntExtra("taskPosition", 0);
                        // 리스트에서 삭제
                        tasks.remove(deletePosition);

                        taskAdapter.notifyItemRemoved(deletePosition);
                        taskAdapter.notifyItemRangeChanged(deletePosition, taskAdapter.getItemCount());
                    }
                    break;
            }
        }
    }

    //테스트 아이템 넣기
    private void addTestData(){

        for (int i = 0; i < 30; i++){

            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setTaskName("완료 한 일" + (i + 1));
            taskDTO.setDurationTime("00:10:00");

            tasks.add(taskDTO);

        }

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

                    tasks.add(0, taskDTO);

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }

    }

    //쉐어드에 데이터 저장하기
    private void saveDataToSharedPreference(){
        // 갱신 된 데이터를 쉐어드에 저장
        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        String tasksJson = null;

        //리스트를 json으로 변환해서 쉐어드에 저장하기
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < tasks.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                // 가장 최근에 한 일이 가장 맨 위에 올라 오기 때문에
                // 데이터를 반대로 저장해야함.
                jsonObject.put("key", tasks.get(tasks.size() - 1 - i).getKey());
                jsonObject.put("taskName", tasks.get(tasks.size() - 1 - i).getTaskName());
                jsonObject.put("startTime", tasks.get(tasks.size() - 1 - i).getStartTime());
                jsonObject.put("endTime", tasks.get(tasks.size() - 1 - i).getEndTime());
                jsonObject.put("durationTime", tasks.get(tasks.size() - 1 - i).getDurationTime());
                jsonObject.put("categoryName", tasks.get(tasks.size() - 1 - i).getCategoryName());
                jsonObject.put("categoryColor", tasks.get(tasks.size() - 1 - i).getCategoryColor());
                jsonArray.put(jsonObject);

            }
            tasksJson = jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(tasksJson != null) {
            // 리스트에 데이터가 없을 경우
            if(tasks.size() == 0){
                //쉐어드에서 지움.
                editor.remove("tasks");
                editor.apply();
            }
            // 리스트에 데이터가 있을 경우
            else {
                //쉐어드에 저장
                editor.putString("tasks", tasksJson);
                editor.apply();
            }
        }

    }


    @Override
    protected void onStart() {
        Log.v("ListActivity", "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.v("ListActivity", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.v("ListActivity", "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v("ListActivity", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v("ListActivity", "onStop");
        saveDataToSharedPreference();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v("ListActivity", "onDestroy");
        super.onDestroy();
    }

}
