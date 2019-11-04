package com.example.timetracker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.timetracker.R;
import com.example.timetracker.adapter.TaskNameAdapter;

import java.util.ArrayList;

public class TaskNameSearchActivity extends AppCompatActivity {

    Toolbar toolbar; // 툴바
    RecyclerView recyclerViewTaskName; // 할일 목록 리싸이클러뷰
    Button buttonClear; //전체 삭제 버튼
    SearchView searchView; // 검색창

    //12. 할일 내용을 담을 리스트
    ArrayList<String> taskNames = new ArrayList<>();

    // 리싸이클러뷰 어뎁터
    TaskNameAdapter taskNameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_name_search);

        toolbar = findViewById(R.id.toolbar);
        recyclerViewTaskName = findViewById(R.id.recyclerViewTaskName);
        buttonClear = findViewById(R.id.buttonClear);

        setToolbar();
        getDataFromSharedPreference();
        setRecyclerView();
        setItemClickListener();
        setItemDeleteButtonClickListener();
        setButtonClear();

        //addTestDate();

        // searchview를 사용하기 위해서 어뎁터 안에 있는 풀 리스트에
        // 새로 이름들 리스트를 넣어줌.
        taskNameAdapter.setFullListAdapter(taskNames);

    }

    @Override
    protected void onStop() {
        Log.v("리스트", taskNames.size() + "");
        saveDataToSharedPreference();
        super.onStop();
    }

    //toolbar 설정하기
    private void setToolbar(){
        setSupportActionBar(toolbar);

//        // 툴바 왼쪽 버튼 활성화
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        // 왼쪽 버튼 아이콘 설정 --> 왼쪽 화살표 버튼
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
    }

    // toolbar 와 menu 파일 연결하기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_task_name_search_menu, menu);

        //서치뷰 생성
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();

        //검색 버튼을 눌렀을 때 뷰가 꽉차게 하기
        searchView.setMaxWidth(Integer.MAX_VALUE);

        if(getIntent().getStringExtra("taskName") != null){
            searchView.setQuery(getIntent().getStringExtra("taskName"), false);
        }

        //SearchView 힌트
        searchView.setQueryHint("할 일을 입력해주세요!");

        searchView.setSubmitButtonEnabled(true);

        // SearchView 제출 버튼 이미지 바꾸기
        ImageView submit = searchView.findViewById(R.id.search_go_btn);
        submit.setImageResource(R.drawable.ic_check_black_24dp);

        // 화면으로 들어가자마자 키보드가 켜짐
        searchView.setIconified(false);
        // 이 화면에 들어왔을 때 SearchView가 활성화 되어있게 하기
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 검색어가 제출되었을 때
            @Override
            public boolean onQueryTextSubmit(String s) {

                Intent intent = new Intent();
                // 인텐트에 검색창에 있는 할 일 내용을 담음
                intent.putExtra("taskName", s);
                setResult(RESULT_OK, intent);
                // 검색 액티비티 종료
                finish();
                return false;
            }
            // 검색어가 바뀌었을 때
            @Override
            public boolean onQueryTextChange(String s) {

                // 어뎁터 필터
                taskNameAdapter.getFilter().filter(s);

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
//            // 뒤로가기 버튼
//            case android.R.id.home:
//                finish();
//                return true;
            // 검색 버튼

            // 체크 버튼

        }
        return super.onOptionsItemSelected(item);
    }

    //13.
    //RecyclerView 설정하기
    private void setRecyclerView(){

        //리싸이클러뷰를 리니어 레이아웃 형태로 보여주기
        recyclerViewTaskName.setLayoutManager(new LinearLayoutManager(this));

        //어뎁터 만들기
        taskNameAdapter = new TaskNameAdapter(taskNames);

        //리싸이클러뷰에 어뎁터 넣기
        recyclerViewTaskName.setAdapter(taskNameAdapter);

        //리싸이클러뷰 구분선 넣기
        recyclerViewTaskName.addItemDecoration(new DividerItemDecoration(TaskNameSearchActivity.this, 1));

    }

    //테스트 데이터 넣기
    private void addTestDate(){

        for (int i = 0; i < 10; i++){
            taskNames.add("원형 타이머 구현을 하다가 다른 것을 하네요");
            taskNames.add("자료 검색");
            taskNames.add("우리지금만나");
            taskNames.add("안녕하세요");
            taskNames.add("운동");
            taskNames.add("리싸이클러뷰 구현");
            taskNames.add("지금 보기");
        }

    }

    //18. 리싸이클러뷰 아이템 클릭 리스너 설정
    private void setItemClickListener(){

        taskNameAdapter.setOnItemClickListener(new TaskNameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // 서치뷰에 해당 아이템 넣기
                searchView.setQuery(taskNames.get(position), false);

            }
        });
    }

    //리싸이클러뷰 아이템 삭제 버튼 클릭 리스너
    private void setItemDeleteButtonClickListener(){

        taskNameAdapter.setOnDeleteButtonClickListener(new TaskNameAdapter.OnDeleteButtonClickListener() {
            @Override
            public void onDeleteButtonClick(View v, int position) {
                // 해당 할일 내용 리스트에서 지우기
                taskNames.remove(position);

                // 어뎁터에게 해당 아이템이 지워졌다고 알려주기
                taskNameAdapter.notifyItemRemoved(position);
                taskNameAdapter.notifyItemRangeChanged(position, taskNameAdapter.getItemCount());

                taskNameAdapter.getFullList().remove(position);

            }
        });

    }

    //전체 삭제 버튼 설정
    private void setButtonClear(){

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //확인 다이얼로그 띄우기
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskNameSearchActivity.this);
                builder.setTitle("전체 삭제");
                builder.setMessage("정말로 전체 삭제를 하시겠습니까?");
                builder.setPositiveButton("아니요",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.setNegativeButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // 리스트 비우기
                                taskNames.clear();

                                // 검색을 위해서 어뎁터안에 있는 전체 리스트도 비우긴
                                taskNameAdapter.setFullListAdapter(taskNames);

                                // 어뎁터 갱신
                                taskNameAdapter.notifyDataSetChanged();

                            }
                        });
                builder.show();

            }
        });

    }

    //쉐어드에서 검색어 데이터 가지고 오기
    private void getDataFromSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);

        if(preferences.getString("taskNames", null) != null){

            String[] valueTaskNames = preferences.getString("taskNames", null).split("★");

            // 쉐어드에 있는 할 일 내용들을 리스트에 최신 순으로 넣음.
            for(String taskName : valueTaskNames){
                taskNames.add(0, taskName);
            }
        }

    }

    //쉐어드에 가장 최신 데이터 저장하기
    private void saveDataToSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // 리스트안에 값이 있을 때 만 저장
        if(taskNameAdapter.getFullList().size() != 0 ){

            StringBuffer stringBufferTaskName = new StringBuffer();

            for(int i = 0; i < taskNameAdapter.getFullList().size(); i++){

                //처음 값을 집어 넣을 때
                if(i == 0){
                    stringBufferTaskName.append(taskNameAdapter.getFullList().get(taskNameAdapter.getFullList().size() - 1 - i));
                }
                //이후에 집어 넣을 때
                else{
                    stringBufferTaskName.append("★" + taskNameAdapter.getFullList().get(taskNameAdapter.getFullList().size() - 1 - i));
                }

            }

            editor.putString("taskNames", stringBufferTaskName.toString());
            editor.apply();
        }
        // 리스트 안에 값이 없을 때
        else{
            // 빈 값 저장
            editor.putString("taskNames", null);
            editor.apply();
        }

    }
}
