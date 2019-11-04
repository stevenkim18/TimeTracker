package com.example.timetracker.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.timetracker.DTO.CategoryDTO;
import com.example.timetracker.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    //삭제 이미지 버튼 인터페이스
    public interface OnDeleteButtonClickListener{
        void onDeleteButtonClick(View v, int position);
    }

    //삭제 버튼 클릭 리스너
    OnDeleteButtonClickListener onDeleteButtonClickListener;

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener){
        this.onDeleteButtonClickListener = listener;
    }

    //아이템 클릭 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }

    //아이템 클릭 리스너 변수
    OnItemClickListener onItemClickListener;

    //아이템 클릭 리스너 설정
    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    private ArrayList<CategoryDTO> categorys = null;

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewCategory; // 카테고리 이름
        ImageButton imageButtonDelete;  //삭제 버튼


        ViewHolder(View itemView){
            super(itemView);

            textViewCategory = itemView.findViewById(R.id.textViewTaskName);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);

        }
    }

    //생성자
    public CategoryAdapter(ArrayList<CategoryDTO> categorys) {
        this.categorys = categorys;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_task_name, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        CategoryDTO categoryDTO = categorys.get(i);

        // 카테고리 제목
        viewHolder.textViewCategory.setText(categorys.get(i).getCategoryName());

        // 카테고리 색
        viewHolder.textViewCategory.setTextColor(categorys.get(i).getColor());

        // 삭제 버튼
        viewHolder.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDeleteButtonClickListener != null){
                    onDeleteButtonClickListener.onDeleteButtonClick(viewHolder.itemView, i);
                }
            }
        });

        // 이름 클릭
        viewHolder.textViewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(viewHolder.itemView, i);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return categorys.size();
    }

}
