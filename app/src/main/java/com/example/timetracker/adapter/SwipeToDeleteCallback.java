package com.example.timetracker.adapter;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.timetracker.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private Drawable icon;
    private final ColorDrawable background;

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    // 리싸이클러뷰의 아이템이 스와프 되어서 완전히 화면에서 사라졌을때 호출 되는 메소드
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        int position = viewHolder.getAdapterPosition();
        // 나중에 어텝터에서 만들 것임.
        taskAdapter.deleteItem(viewHolder, position);

    }

    // 할 일 목록 어뎁터 변수
    private TaskAdapter taskAdapter;

    // 생성자
    public SwipeToDeleteCallback(TaskAdapter taskAdapter) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.taskAdapter = taskAdapter;

        icon = ContextCompat.getDrawable(this.taskAdapter.context, R.drawable.ic_delete_black_24dp);
        background = new ColorDrawable(ContextCompat.getColor(this.taskAdapter.context, R.color.red));
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // 오른쪽으로 스와이프 했을 때
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            int iconLeft = itemView.getLeft() + iconMargin;

            int magicConstraint = (itemView.getLeft() + ((int) dX) < iconRight + iconMargin) ? (int)dX - icon.getIntrinsicWidth() - ( iconMargin * 2 ) : 0;
            iconLeft += magicConstraint;
            iconRight += magicConstraint;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX),
                    itemView.getBottom());

        }
        else if (dX < 0) { // 왼쪽으로 스와이프 했을 떄
                       int iconRight = itemView.getRight() - iconMargin;
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int magicConstraint = (itemView.getRight() + ((int) dX) > iconLeft - iconMargin) ? magicConstraint = icon.getIntrinsicWidth() + ( iconMargin * 2 ) + (int)dX : 0;
            iconLeft += magicConstraint;
            iconRight += magicConstraint;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getRight(), itemView.getTop(),
                    itemView.getRight() + ((int) dX),
                    itemView.getBottom());

        } else { // 스와이프 안했을 떄
            background.setBounds(0, 0, 0, 0);
            icon.setBounds(0,0,0,0);
        }

        background.draw(c);
        icon.draw(c);
    }

}
