package com.cyy.qsg;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {

    private int resourceId;

    public StudentAdapter(Context context, int textViewResourceId, List<Student> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Student student = getItem(position); //获取当前项的Student实例
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            //viewHolder.fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
            viewHolder.studentName =(TextView) view.findViewById(R.id.student_name);
            view.setTag(viewHolder);// 将ViewHolder存储在View中。
        }else
        {
            view = convertView;
            viewHolder=(ViewHolder)view.getTag(); //重新获取ViewHolder


        }
        //viewHolder.fruitImage.setImageResource(student.getImageId());
        if ("1".equals(student.getArrive())){
            viewHolder.studentName.setBackgroundColor(Color.GREEN);
        }else{
            viewHolder.studentName.setBackgroundColor(Color.WHITE);
        }
        viewHolder.studentName.setText(student.getName());
        return view;
    }

    class ViewHolder{
        ImageView fruitImage;
        TextView studentName;
    }

}