package com.gaoyy.customrefreshlayoutdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gaoyy.customrefreshlayoutdemo.view.MaterialRefreshLayout;
import com.gaoyy.customrefreshlayoutdemo.view.OnMaterialRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private MaterialRefreshLayout rl;
    private List<String> list = null;

    private void assignViews()
    {
        rv = (RecyclerView) findViewById(R.id.rv);
        rl = (MaterialRefreshLayout) findViewById(R.id.rl);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            rl.finishRefresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();

        initData();

        //设置布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);

        rv.setAdapter(new MyAdapter(list, this));

//        rl.setRefreshing(true);

        rl.setOnMaterialRefreshListener(new OnMaterialRefreshListener()
        {
            @Override
            public void onRefresh(MaterialRefreshLayout refreshLayout)
            {
                handler.sendEmptyMessageDelayed(0,2000);
            }
        });

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
