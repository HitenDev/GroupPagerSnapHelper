package me.hiten.grouppagersnaphelper.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.hiten.grouppagersnaphelper.GroupPagerSnapHelper;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recyclerView);

        AverageMeasure averageMeasure = new AverageMeasure(5, true);
        Adapter adapter = new Adapter(getList(), averageMeasure);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        new GroupPagerSnapHelper(5).attachToRecyclerView(mRecyclerView);
    }


    private List<String> getList() {
        List<String> apps = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            apps.add(String.valueOf(i));
        }
        return apps;
    }

}
