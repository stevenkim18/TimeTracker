package com.example.timetracker.adapter;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.timetracker.R;
import com.example.timetracker.DTO.TaskDTO;

import java.util.ArrayList;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    Context context;
    private ArrayList<TaskDTO> tasks = null;

    private TaskDTO recentDeletedTask;
    int recentDeletedTaskPosition;


    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }

    private OnItemClickListener itemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }

    //1.
    public class ViewHolder extends RecyclerView.ViewHolder{

        //2.
        TextView textViewTaskName;      //한 일 내용
        TextView textViewCategory;      //카테고리
        TextView textViewDurationTime;      //걸린시간

        //3.
        ViewHolder(View itemView) {
            super(itemView);

            //4.
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewDurationTime = itemView.findViewById(R.id.textViewDurationTime);

        }
    }

    public TaskAdapter(ArrayList<TaskDTO> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    public ArrayList<TaskDTO> getTasks() {
        return tasks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_task, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        //Task 객체 생성
        //리스트에 있는 객체 저장
        TaskDTO task = tasks.get(i);

        // 한 일 내용
        viewHolder.textViewTaskName.setText(task.getTaskName());
        // 카테고리
        viewHolder.textViewCategory.setText(task.getCategoryName());
        viewHolder.textViewCategory.setTextColor(task.getCategoryColor());
        // 걸린 시간
        viewHolder.textViewDurationTime.setText(task.getDurationTime());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClickListener != null){
                    itemClickListener.onItemClick(viewHolder.itemView ,i);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // 아이템 삭제 메소드
    // 스와이프 할 때 실행 됨.
    public void deleteItem(RecyclerView.ViewHolder viewHolder, int position) {
        recentDeletedTask = tasks.get(position);
        recentDeletedTaskPosition = position;
        tasks.remove(position);
        notifyItemRemoved(position);

        Snackbar.make(viewHolder.itemView, "\""+recentDeletedTask.getTaskName()+"\""+"이 삭제 됨", Snackbar.LENGTH_LONG)
                .setAction("실행 취소", new View.OnClickListener() {
                    // 실행 취소 버튼 클릭
                    @Override
                    public void onClick(View v) {
                        tasks.add(recentDeletedTaskPosition,recentDeletedTask);
                        notifyItemInserted(recentDeletedTaskPosition);
                    }
                }).show();

    }
}
