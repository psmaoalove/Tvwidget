
package com.android.tvdemo;

import com.stv.widget.GridManager;
import com.stv.widget.GridRecyclerView;
import com.stv.widget.adapter.GeneralAdapter;
import com.stv.widget.adapter.RecyclerViewPresenter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;

public class MainActivity extends AppCompatActivity {
    private GridRecyclerView mRecyclerView;
    RecyclerViewPresenter mRecyclerViewPresenter;
    GeneralAdapter mGeneralAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (GridRecyclerView) findViewById(R.id.recyclerview);
        GridManager gridlayoutManager = new GridManager(this, 6); // 解决快速长按焦点丢失问题.
        gridlayoutManager.setOrientation(OrientationHelper.VERTICAL);
        mRecyclerView.setLayoutManager(gridlayoutManager);
        mRecyclerView.setFocusable(false);
        mRecyclerViewPresenter = new RecyclerViewPresenter(1000);
        mGeneralAdapter = new GeneralAdapter(mRecyclerViewPresenter);
        mRecyclerView.setAdapter(mGeneralAdapter);
    }

}
