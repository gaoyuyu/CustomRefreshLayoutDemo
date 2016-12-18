package com.gaoyy.customrefreshlayoutdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gaoyy.customrefreshlayoutdemo.view.WaveView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private List<String> list = null;

    private void assignViews()
    {
        rv = (RecyclerView) findViewById(R.id.rv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        WaveView waveView = new WaveView(this);

        setContentView(R.layout.activity_main);
        assignViews();

        initData();

        //设置布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);

        rv.setAdapter(new MyAdapter(list,this));

    }

    private void initData()
    {
        list = new ArrayList<>();
        for (int i = 0; i < 30; i++)
        {
            list.add(i, "data：" + i);
        }
    }

}
