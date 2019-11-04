package com.example.timetracker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.model.Image;
import com.example.timetracker.R;

import java.util.ArrayList;

public class ImageSliderAdapter extends PagerAdapter {

    private ArrayList<Image> images = null;
    private LayoutInflater inflater;
    private Context context;

    // 생성자
    public ImageSliderAdapter(Context context, ArrayList<Image> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        // 이미지 없을 때는 0을 리턴
        if(images == null){
          return 0;
        }
        // 이미지가 있을 때는 이미지 리스트의 갯수를 리턴
        else {
            return images.size();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    // 아이템이 포커스 될 떄 실행되는 메소드
    public Object instantiateItem(ViewGroup viewGroup, int position){
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.image_slider, viewGroup, false);
        ImageView imageView = v.findViewById(R.id.imageView);
        // 사진이 안나옴.
        // Glide.with(context).load(Uri.parse(images.get(position).getPath())).into(imageView);
        imageView.setImageURI(Uri.parse(images.get(position).getPath()));
        viewGroup.addView(v);
        return v;

    }

    // 옆으로 밀면서 아이템이 지워질 때 실행되는 메소드
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
