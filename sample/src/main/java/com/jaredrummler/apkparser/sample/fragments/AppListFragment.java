/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jaredrummler.apkparser.sample.fragments;

import android.app.ListFragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.jaredrummler.apkparser.sample.adapters.AppListAdapter;
import com.jaredrummler.apkparser.sample.dialogs.AppDialog;
import com.jaredrummler.apkparser.sample.util.AppNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private final ArrayList<PackageInfo> installedPackages = new ArrayList<>();
    private Parcelable listState;
    private AppListAdapter mListAdapter;
    private PackageManager mPackageManager;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<PackageInfo> packages = savedInstanceState.getParcelableArrayList("packages");
            if (packages != null && !packages.isEmpty()) {
                installedPackages.addAll(packages);
            }
            listState = savedInstanceState.getParcelable("state");
        } else {
            boolean userApp = getArguments().getBoolean("user_app");
            mPackageManager = getActivity().getPackageManager();
            new AppLoader(userApp).execute(mPackageManager);
        }
        getListView().setOnItemClickListener(this);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("packages", installedPackages);
        outState.putParcelable("state", getListView().onSaveInstanceState());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppDialog.show(getActivity(), installedPackages.get(position));
    }

    /**
     * 搜索
     *
     * @param s
     */
    public void onSearchTextChange(CharSequence s) {
        List<PackageInfo> infoList = new ArrayList<>();
        for (PackageInfo info : installedPackages) {
            if (info.packageName.contains(s) || AppNames.getLabel(mPackageManager, info).contains(s)) {
                infoList.add(info);
            }
        }
        if (!infoList.isEmpty()) {
            mListAdapter.setApps(infoList);
        }
    }

    private final class AppLoader extends AsyncTask<PackageManager, Void, List<PackageInfo>> {

        private final boolean mUserApp;

        public AppLoader(boolean userApp) {
            mUserApp = userApp;
        }

        @Override
        protected List<PackageInfo> doInBackground(PackageManager... params) {
            if (installedPackages.isEmpty()) {
                try {
                    Log.d("AppLoader", "System.currentTimeMillis()0:" + System.currentTimeMillis());
                    final PackageManager pm = params[0];
                    List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
                    for (PackageInfo packageInfo : installedPackages) {
                        if (mUserApp) {
                            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                                AppListFragment.this.installedPackages.add(packageInfo);
                            }
                        } else {
                            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                AppListFragment.this.installedPackages.add(packageInfo);
                            }
                        }
                    }
                    Log.d("AppLoader", "System.currentTimeMillis()1:" + System.currentTimeMillis());
                    Collections.sort(AppListFragment.this.installedPackages, (lhs, rhs) -> AppNames.getLabel(pm, lhs).compareToIgnoreCase(AppNames.getLabel(pm, rhs)));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("AppLoader", "e:" + e);
                }
            }
            return installedPackages;
        }

        @Override
        protected void onPostExecute(List<PackageInfo> apps) {
            setListAdapter(new AppListAdapter(getActivity(), apps));
            if (listState != null) {
                getListView().onRestoreInstanceState(listState);
                listState = null;
            }
            mListAdapter = (AppListAdapter) getListAdapter();
        }
    }

}
