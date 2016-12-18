package com.gaoyy.customrefreshlayoutdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static android.R.attr.data;

/**
 * Created by gaoyy on 2016/12/17 0017.
 */

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private List<String> list;
    private LayoutInflater inflater;
    private Context context;

    public MyAdapter(List<String> list, Context context)
    {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View rootView = inflater.inflate(R.layout.item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(rootView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ((MyViewHolder) holder).tv.setText(list.get(position));
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView tv;

        public MyViewHolder(View itemView)
        {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
