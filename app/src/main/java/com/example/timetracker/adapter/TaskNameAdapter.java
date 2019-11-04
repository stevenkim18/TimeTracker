package com.example.timetracker.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.timetracker.R;

import java.util.ArrayList;

//5.
//6.
public class TaskNameAdapter extends RecyclerView.Adapter<TaskNameAdapter.ViewHolder> implements Filterable { // 서치뷰를 사용하기 위해 Filterable implements

    //7.
    private ArrayList<String> taskNames = null;
    private ArrayList<String> fullList = null;

    //
    //14.아이템 클릭 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }

    //15.아이템 클릭 리스너 변수
    OnItemClickListener onItemClickListener;

    //16.아이템 클릭 리스너 설정
    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    //삭제 이미지 버튼 인터페이스
    public interface OnDeleteButtonClickListener{
        void onDeleteButtonClick(View v, int position);
    }

    //삭제 버튼 클릭 리스너
    OnDeleteButtonClickListener onDeleteButtonClickListener;

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener){
        this.onDeleteButtonClickListener = listener;
    }



    //1.
    public class ViewHolder extends RecyclerView.ViewHolder{

        //2.
        TextView textViewTaskName;      //할일 내용
        ImageButton imageButtonDelete;  //삭제 버튼

        //3.
        ViewHolder(View itemView) {
            super(itemView);

            //4.
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);

        }
    }

    //8.
    public TaskNameAdapter(ArrayList<String> taskNames) {
        this.taskNames = taskNames;
        this.fullList = new ArrayList<>(taskNames);
        Log.v("리스트", taskNames.toString());
        Log.v("리스트", fullList.toString());
    }

    //SearchView를 사용하기 위해서 taskNames 와 다른 주소를 참조하는 리스트 객체를 넣어줌.
    public void setFullListAdapter(ArrayList<String> list){
        this.fullList = new ArrayList<>(list);
    }

    public ArrayList<String> getFullList() {
        return fullList;
    }

    //9.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_task_name, viewGroup, false);
        return new ViewHolder(view);
    }

    //10.
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        // 할 일 내용
        viewHolder.textViewTaskName.setText(taskNames.get(i));

        // 할일 내용 클릭 리스너
        viewHolder.textViewTaskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //17.
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(viewHolder.itemView, i);
                }
            }
        });

        // 삭제 버튼
        viewHolder.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDeleteButtonClickListener != null){
                    onDeleteButtonClickListener.onDeleteButtonClick(viewHolder.itemView, i);
                }
            }
        });
    }

    //11.
    @Override
    public int getItemCount() {
        return taskNames.size();
    }

    // SearchView 사용을 위한 Filter
    @Override
    public Filter getFilter() {
        return taskNameFilter;
    }

    // 필터 객체 생성
    private Filter taskNameFilter = new Filter() {
        // 검색어가 완료 되지 않았을 때 보여주는 메소드
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //인자 --> (검색중인 단어)
            // 검색중인 검색어와 비교해서 맞는 단어를 넣어줄 리스트
            ArrayList<String> filterTaskNames = new ArrayList<>();

            // 검색어가 없을 때
            if(constraint == null || constraint.length() == 0){
                filterTaskNames.addAll(fullList);
            }
            // 검색어가 있을 때
            else{
                // 검색창에 입력된 단어를 소문자로 바꾸고 공백을 없애서 저장
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(String taskName : fullList){

                    // 전체 목록이 담겨있는 리스트와 비교하면서
                    // 검색어와 같은 문자가 있다면 걸러진 리스트에 넣음.
                    if(taskName.toLowerCase().contains(filterPattern)){

                        filterTaskNames.add(taskName);

                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filterTaskNames;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            taskNames.clear();
            taskNames.addAll((ArrayList<String>) results.values);
            notifyDataSetChanged();
        }
    };

}
