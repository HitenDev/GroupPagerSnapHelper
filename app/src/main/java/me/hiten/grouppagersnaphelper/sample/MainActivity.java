package me.hiten.grouppagersnaphelper.sample;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.hiten.grouppagersnaphelper.GroupPagerSnapHelper;
import me.hiten.grouppagersnaphelper.OnPageChangeCallback;
import me.relex.circleindicator.CommonCircleIndicator;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private float mMainAxisSize;

    private int mCrossAxisSize;
    private GroupPagerSnapHelper groupPagerSnapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recyclerView);

        mMainAxisSize = 5f;
        mCrossAxisSize = 1;

        AverageMeasure averageMeasure = new AverageMeasure(mMainAxisSize, true);
        Adapter adapter = new Adapter(getList(), averageMeasure);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mCrossAxisSize, RecyclerView.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        groupPagerSnapHelper = new GroupPagerSnapHelper((int) mMainAxisSize, mCrossAxisSize);
        groupPagerSnapHelper.attachToRecyclerView(mRecyclerView);
        setTabLayout();
        setCircleIndicator();
    }

    private static Method sSetScrollPosition;
    private static Method sSelectTab;

    static {
        try {
            sSetScrollPosition = TabLayout.class.getDeclaredMethod("setScrollPosition", int.class,
                    float.class, boolean.class, boolean.class);
            sSetScrollPosition.setAccessible(true);

            sSelectTab = TabLayout.class.getDeclaredMethod("selectTab", TabLayout.Tab.class,
                    boolean.class);
            sSelectTab.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Can't reflect into method TabLayout"
                    + ".setScrollPosition(int, float, boolean, boolean)");
        }
    }

    private void setTabLayout() {
        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.removeAllTabs();
        for (int i=0;i<mRecyclerView.getAdapter().getItemCount() / (int) mMainAxisSize / mCrossAxisSize;i++){
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(String.valueOf(i));
            tabLayout.addTab(tab,i==0);
        }
        groupPagerSnapHelper.addOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                try {
                    sSelectTab.invoke(tabLayout,tabLayout.getTabAt(position),true);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                try {
                    sSetScrollPosition.invoke(tabLayout,position,positionOffset,true,true);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setCircleIndicator() {
        final CommonCircleIndicator indicator = findViewById(R.id.indicator);
        indicator.createIndicators(mRecyclerView.getAdapter().getItemCount() / (int) mMainAxisSize / mCrossAxisSize, 0);
        groupPagerSnapHelper.addOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                indicator.setPageSelected(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        });
    }


    private List<String> getList() {
        List<String> apps = new ArrayList<>();
        for (int i = 0; i < 48; i++) {
            apps.add(String.valueOf(i));
        }
        final int appSize = apps.size();
        for (int i = 0; i < DataAlignUtil.calculateAppendCount(appSize, (int) mMainAxisSize, mCrossAxisSize); i++) {
            apps.add("");
        }
        GridListSorter.sort(apps, (int) mMainAxisSize, mCrossAxisSize);
        return apps;
    }


    static class DataAlignUtil {
        static int calculateAppendCount(int listSize, int mainAxisSize, int crossAxisSize) {
            int capacity = mainAxisSize * crossAxisSize;
            int endPagerSize = listSize % capacity;
            if (endPagerSize == 0) {
                return 0;
            }
            return capacity - endPagerSize;
        }

        static int calculateAppendCount2(int listSize, int mainAxisSize, int crossAxisSize) {
            return calculateAppendCount(listSize, mainAxisSize, crossAxisSize) + crossAxisSize;
        }

    }

    static class GridListSorter {

        public static <T> void sort(List<T> data, int column, int row) {
            List<T> tempList = new ArrayList<>(data);
            int pageCount = data.size() / (row * column);
            int next = 0;
            for (int i = 0; i < pageCount; i++) {
                int offset = i * row * column;
                for (int j = 0; j < row; j++) {
                    for (int k = 0; k < column; k++) {
                        int index = (k * row + j) + offset;
                        data.set(index, tempList.get(next));
                        next++;
                    }
                }
            }
        }
    }

}
