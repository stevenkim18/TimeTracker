package com.example.timetracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.example.timetracker.DTO.CategoryDTO;
import com.example.timetracker.R;
import com.example.timetracker.adapter.CategoryAdapter;
import com.flask.colorpicker.ColorPickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    Button buttonAddCategory;

    // 카테고리를 담을 리스트 변수
    ArrayList<CategoryDTO> categorys = new ArrayList<>();

    // 리싸이클러뷰 어뎁터
    CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewCategory);
        buttonAddCategory = findViewById(R.id.buttonAddCategory);

        getDataFromSharedPreference();

        setRecyclerView();

        setItemDeleteButtonClickListener();

        setItemClickListener();

        setButtonAddCategory();

    }

    @Override
    protected void onDestroy() {

        saveDataIntoSharedPreference();

        super.onDestroy();
    }

    //리싸이클러뷰 세팅
    private void setRecyclerView(){

        categoryAdapter = new CategoryAdapter(categorys);

        recyclerView.setAdapter(categoryAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addItemDecoration(new DividerItemDecoration(CategoryActivity.this, 1));

    }

    //카테고리 추가 버튼 설정
    private void setButtonAddCategory(){

        buttonAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddCategoryDialog();

            }
        });

    }

    //카테고리 추가 다이얼로그 보여주기
    private void showAddCategoryDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.category_add_dialog, null);
        builder.setView(view);

        // 카테고리 이름 EditText
        final TextInputEditText editText = view.findViewById(R.id.editTextCategoryName);
        // ColorPicker
        final ColorPickerView colorPickerView = view.findViewById(R.id.colorPicker);

        //추가 버튼
        final Button buttonAdd = view.findViewById(R.id.buttonAdd);

        //삭제 버튼
        final Button buttonCancel = view.findViewById(R.id.buttonCancel);

        // 다이얼로그
        final AlertDialog dialog = builder.create();

        //추가 버튼 클릭 리스너
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 카테고리 객체 생성
                CategoryDTO category = new CategoryDTO();

                // 카테고리 이름
                category.setCategoryName(editText.getText().toString());

                // 카테고리 색
                category.setColor(colorPickerView.getSelectedColor());

                categorys.add(category);

                //어뎁터에게 알려줌.
                categoryAdapter.notifyDataSetChanged();

                //다이얼로그 끔.
                dialog.dismiss();

            }
        });

        // 취소 버튼
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 종료
                dialog.dismiss();
            }
        });

        // 다이얼로그 띄우기
        dialog.show();

    }

    //리싸이클러뷰 아이템 클릭 리스너
    private void setItemClickListener(){

        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // 아이템 클릭시 에인화면으로 카테고리 넘기기
                Intent categoryIntent = new Intent();
                categoryIntent.putExtra("categoryName", categorys.get(position).getCategoryName());
                categoryIntent.putExtra("categoryColor", categorys.get(position).getColor());
                setResult(RESULT_OK, categoryIntent);
                finish();
            }
        });

    }

    //리싸이클러뷰 아이템 삭제 버튼 클릭 리스너
    private void setItemDeleteButtonClickListener(){

        categoryAdapter.setOnDeleteButtonClickListener(new CategoryAdapter.OnDeleteButtonClickListener() {
            @Override
            public void onDeleteButtonClick(View v, int position) {
                // 해당 할일 내용 리스트에서 지우기
                categorys.remove(position);

                // 어뎁터에게 해당 아이템이 지워졌다고 알려주기
                categoryAdapter.notifyItemRemoved(position);
                categoryAdapter.notifyItemRangeChanged(position, categoryAdapter.getItemCount());

            }
        });

    }

    //쉐어드에 카테고리 리스트 저장하기
    private void saveDataIntoSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray jsonArray = new JSONArray();

        String jsonToString = null;

        try {
            for (int i = 0; i < categorys.size(); i++){

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", categorys.get(i).getCategoryName());
                jsonObject.put("color", categorys.get(i).getColor());
                jsonArray.put(jsonObject);
            }

            jsonToString = jsonArray.toString();
        }
        catch (JSONException e){

        }

        editor.putString("categorys", jsonToString);
        editor.apply();

    }

    //쉐어드에서 카테고리 리스트 가지고 오기
    private void getDataFromSharedPreference(){

        SharedPreferences preferences = getSharedPreferences("task", MODE_PRIVATE);

        // 쉐어드에 categorys 키 값이 있을 때
        if(preferences.getString("categorys", null) != null){

            try{
                JSONArray jsonArray = new JSONArray(preferences.getString("categorys", null));

                for(int i = 0; i < jsonArray.length(); i++){

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String categoryName = jsonObject.getString("name");     // 카테고리 이름
                    int categoryColor = jsonObject.getInt("color");         // 카테고리 색 코드

                    // 카테고리 객체 생성
                    CategoryDTO category = new CategoryDTO();
                    category.setCategoryName(categoryName);
                    category.setColor(categoryColor);
                    categorys.add(category);
                }

            }catch (JSONException e){

            }
        }

    }

}
