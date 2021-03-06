package com.jason.rxjavademo.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jason.rxjavademo.R;
import com.jason.rxjavademo.adapter.CommonAdapter;
import com.jason.rxjavademo.adapter.ViewHolder;
import com.jason.rxjavademo.domian.AppInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

public class AppInfoActivity extends AppCompatActivity {


    @Bind(R.id.list)
    ListView listview;

    private BaseAdapter mAdapter;
    private ArrayList<AppInfo> mData;
    private String type;
    private List<AppInfo> appInfos = new ArrayList<AppInfo>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("type")) {
            type = intent.getStringExtra("type");
        }
        getApplicationList();
        initViews();
    }

    @OnClick({
            R.id.fab
    })
    public void onClick(View view) {


        /**
         * omit on by one
         */
        if (type.equals("create")) {
            ArrayList<AppInfo> copyOfData = new ArrayList<>();
            copyOfData.addAll(mData);
            mData.clear();
            mAdapter.notifyDataSetChanged();

            getApps().subscribe(new Observer<AppInfo>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(AppInfo appInfo) {
                    mData.add(appInfo);
                    mAdapter.notifyDataSetChanged();
                    Log.d("RxDemo", "consume a app info");
                }
            });
        } else if (type.equals("filter")) {
            ArrayList<AppInfo> copyOfData = new ArrayList<>();
            copyOfData.addAll(mData);
            mData.clear();
            mAdapter.notifyDataSetChanged();

            loadByList().take(2).subscribe(new Subscriber<AppInfo>() {
                @Override
                public void onCompleted() {
                    Toast.makeText(AppInfoActivity.this, "take(2)", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(AppInfo appInfo) {
                    mData.add(appInfo);
                    mAdapter.notifyDataSetChanged();
                    Log.d("RxDemo", "consume a app info");
                }
            });

        }

    }

    private void initViews() {

        mData = new ArrayList<>();
        mAdapter = new CommonAdapter<AppInfo>(this, mData, R.layout.item_app_info) {
            @Override
            public void convert(ViewHolder helper, AppInfo item, int position) {
                helper.setText(R.id.app_name, item.getName());
                helper.setImageDrawable(R.id.app_icon, item.getIconDra());
            }
        };
        listview.setAdapter(mAdapter);

        if (type.equals("create")) {
            refreshList();
        } else if (type.equals("filter")) {
            Observable<AppInfo> filterObserver = loadByList();
            if (filterObserver != null) {
                filterObserver.subscribe(new Observer<AppInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AppInfo appInfo) {
                        mData.add(appInfo);
                        mAdapter.notifyDataSetChanged();
                        Log.d("RxDemo", "consume a app info");
                    }
                });
            }
        }
    }

    /**
     * omit all (toSortedList)
     */
    private void refreshList() {
        getApps().toSortedList()
                .subscribe(new Observer<List<AppInfo>>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(AppInfoActivity.this, "load finished", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(AppInfoActivity.this, "load error", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<AppInfo> appInfos) {
                        Toast.makeText(AppInfoActivity.this, "onNext", Toast.LENGTH_SHORT).show();
                        mData.addAll(appInfos);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    private Observable<AppInfo> getApps() {
        return Observable.create(new Observable.OnSubscribe<AppInfo>() {
            @Override
            public void call(Subscriber<? super AppInfo> subscriber) {

                // emit
                for (AppInfo info : appInfos) {
                    if (subscriber.isUnsubscribed()){
                        return;
                    }

                    subscriber.onNext(new AppInfo(info.getName(), info.getIconDra()));
                }

                if (!subscriber.isUnsubscribed()){
                    subscriber.onCompleted();
                }
            }
        });
    }

    private Observable<AppInfo> loadByList() {
        if (!appInfos.isEmpty()) {
            return Observable.from(appInfos).filter(new Func1<AppInfo, Boolean>() {
                @Override
                public Boolean call(AppInfo appInfo) {
                    return appInfo.getName().startsWith("A");
                }
            });
        }

        return null;
    }

    private void getApplicationList() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> packageinfos = pm.getInstalledPackages(0);

        for (PackageInfo pak : packageinfos) {
            AppInfo appInfo = new AppInfo();
            appInfo.setName((String) pm.getApplicationLabel(pak.applicationInfo));
            appInfo.setIconDra(pm.getApplicationIcon(pak.applicationInfo));
            appInfos.add(appInfo);
        }
    }

}
